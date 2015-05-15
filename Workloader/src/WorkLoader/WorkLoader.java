package WorkLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class WorkLoader {
	private static int numberOfThreads;
	private static Thread[] workers;
	private int groupNo;

	WorkLoader(int num, int groupNo) {
		numberOfThreads = num;
		workers = new Thread[numberOfThreads];
		this.groupNo = groupNo;
	}

	public void startWorkLoader(String add) {
		for(int i = 0; i < numberOfThreads; i++) {
			workers[i] = new Thread(new workThread(i, add));
			workers[i].start();
		}
	}
	
	private class workThread implements Runnable {
		private int fileName;
		private WebServer ws;
		workThread(int num, String add) {
			this.fileName = num;
			ws = new WebServer(add);
		}
		
		public void run() {
			BufferedReader br = null;
			String line;
			try {
				br = new BufferedReader(new FileReader("./users"+groupNo+"/" + fileName + ".txt"));
				
				while((line = br.readLine()) != null) {
					ws.sendMsg(line);
				}
				System.out.println("ENDED.");
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
					ws.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		do {		
			WorkLoader wl = new WorkLoader(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
			wl.startWorkLoader(args[3]);
			for(int i = 0; i < numberOfThreads; i++) {
				try {
					workers[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			WebServer ws = null;
			if(args[2].equals("true")) {
				BufferedReader br = null;
				String line;
				ws = new WebServer(args[3]);
				try {
					br = new BufferedReader(new FileReader("./users"+(Integer.parseInt(args[1])+1)+"/0.txt"));
					
					while((line = br.readLine()) != null) {
						ws.sendMsg(line);
					}
					System.out.println("ENDED.");
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				ws.close();
			}
		} while(AutoLoad.INSTANCE.fromLog().equals("restart"));
	}

}
