package Cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Dump log into XML.
 * 
 * @author Chuan Yun Loe
 *
 */
public enum Dumplog {
	INSTANCE;
	private Socket kkSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	/**
	 * Constructor
	 */
	Dumplog() {
		try {
			kkSocket = new Socket("b130.seng.uvic.ca",44459);
			kkSocket.setKeepAlive(true);
			kkSocket.setSoTimeout(0);
			out = new PrintWriter(kkSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
            System.err.println("Don't know about host: b130.seng.uvic.ca");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection Server likely down.");
            System.exit(1);
        }
	}
	
	/**
	 * Dump information into log accordingly. Leave parameter/s as null if not required.
	 * Must include: logging type, time stamp, server, transaction number, user ID.
	 * 
	 * @param type
	 * @param timeStamp
	 * @param server
	 * @param transNum
	 * @param comd
	 * @param userName
	 * @param stockSymbol
	 * @param fileName
	 * @param funds
	 * @param price
	 * @param quoteServerTime
	 * @param crytoKey
	 * @param action
	 * @param errorMsg
	 * @param debugMsg
	 */
	public synchronized void dump(String type, String timeStamp, String server, String transNum, String comd, String userName,
			String stockSymbol, String fileName, String funds, String price, String quoteServerTime, String crytoKey,
			String action, String errorMsg, String debugMsg) {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(",");
		sb.append(timeStamp);
		sb.append(",");
		sb.append(server);
		sb.append(",");
		sb.append(transNum);
		sb.append(",");
		
		if(comd!=null) {
			sb.append(comd);
		} else {
			sb.append("null");
		}
		sb.append(",");
		
		if(userName!=null) {
			sb.append(userName);
		} else {
			sb.append("null");
		}
		sb.append(",");
		
		if(stockSymbol!=null) {
			sb.append(stockSymbol);
		} else {
			sb.append("null");
		}
		sb.append(",");
		
		if(fileName!=null) {
			sb.append(fileName);
		} else {
			sb.append("null");
		}
		sb.append(",");
		
		if(funds!=null) {
			sb.append(funds);
		} else {
			sb.append("null");
		}
		sb.append(",");

		if(price!=null) {
			sb.append(price);
		} else {
			sb.append("null");
		}
		sb.append(",");

		if(quoteServerTime!=null) {
			sb.append(quoteServerTime);
		} else {
			sb.append("null");
		}
		sb.append(",");

		if(crytoKey!=null) {
			sb.append(crytoKey);
		} else {
			sb.append("null");
		}
		sb.append(",");

		if(action!=null) {
			sb.append(action);
		} else {
			sb.append("null");
		}
		sb.append(",");

		if(errorMsg!=null) {
			sb.append(errorMsg);
		} else {
			sb.append("null");
		}
		sb.append(",");

		if(debugMsg!=null) {
			sb.append(debugMsg);
		} else {
			sb.append("null");
		}
		
		out.println(sb.toString());
		this.notifyAll();
	}
	
	public String fromDL() {
		try {
			return in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Should not reach here.
		return null;
	}
	
	public void toDL(String msg) {
		out.println(msg);
	}
	
	public void close() {
		try {
			out.close();
			in.close();
			kkSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
