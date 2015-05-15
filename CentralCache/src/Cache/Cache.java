package Cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;


public class Cache {
	private ConcurrentHashMap<String, element> cache;
	private ConcurrentHashMap<String,Boolean> isUpdating;
	private static boolean shutdown;
	
	Cache() {
		cache = new ConcurrentHashMap<String, element>(50000,0.75F,16);
		isUpdating = new ConcurrentHashMap<String, Boolean>(50000,0.75F,16);
	}
	
	/**
	 * "Element" of hashmap.
	 * 
	 * @author Chuan Yun Loe
	 *
	 */
	private class element {
		private String quote;
		private long timeStamp;

		/**
		 * Constructor for element.
		 * 
		 * @param symbol
		 * @param quote
		 * @param timeStamp
		 */
		element(String quote, long timeStamp) {
			this.quote = quote;
			this.timeStamp = timeStamp;
		}
		
		/**
		 * Get quote for element.
		 * 
		 * @return quote
		 */
		public String getQuote() {
			return quote;
		}

		/**
		 * Get time stamp.
		 * 
		 * @return time stamp
		 */
		public long getTimeStamp() {
			return timeStamp;
		}		
	}
	
	private class worker implements Runnable {
		private BufferedInputStream input;
		private BufferedReader inputreader;
		private BufferedOutputStream output;
		private OutputStreamWriter outputwriter;
		private Socket connection;
		
		worker(Socket connection) {
			try {
				this.connection = connection;
				this.input = new BufferedInputStream(connection.getInputStream());
				this.inputreader = new BufferedReader(new InputStreamReader(input));
				this.output = new BufferedOutputStream(connection.getOutputStream());
				this.outputwriter = new OutputStreamWriter(output, "US-ASCII");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		public void run() {
			try {
				String[] quoteQuery = inputreader.readLine().split(",");
				element e= cache.get(quoteQuery[1]);
				
				while(e == null || System.currentTimeMillis()-e.getTimeStamp() > 40000) {
					if(!isUpdating.containsKey(quoteQuery[1]) || !isUpdating.get(quoteQuery[1])) {
						isUpdating.put(quoteQuery[1], true);
					}
					else {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						e= cache.get(quoteQuery[1]);
						continue;
					}
					Quote newQuote = new Quote();
					String[] temp = (newQuote.requestQuote(quoteQuery[1], quoteQuery[0], quoteQuery[2])).split(",");
					element newElement = new element(temp[0],Long.parseLong(temp[3]));
					cache.put(temp[1], newElement);
					isUpdating.put(quoteQuery[1], false);
					
					outputwriter.write(temp[0]+","+temp[3]);
					outputwriter.flush();
					inputreader.close();
					outputwriter.close();
					connection.close();
					return;
				}
				
				Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CENTRALCACHE", 
						quoteQuery[2], "QUOTE", quoteQuery[0], quoteQuery[1], null, e.getQuote(), 
						null, null, null, null, null, null);
				outputwriter.write(e.getQuote()+","+e.getTimeStamp());
				outputwriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					inputreader.close();
					outputwriter.close();
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		System.out.println("Central cache Started...");
		Cache cache = new Cache();
		ServerSocket socketlisten = null;
		try {
			socketlisten = new ServerSocket(44455,Integer.MAX_VALUE);
			while(!shutdown) {
				Socket socket = socketlisten.accept();
				Thread t = new Thread(cache.new worker(socket));
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socketlisten.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
