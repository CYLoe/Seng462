package TransServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class CentralCache {
	private Socket kkSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	CentralCache() {
		try {
			kkSocket = new Socket("b145.seng.uvic.ca",44455);
			kkSocket.setSoTimeout(0);
			out = new PrintWriter(kkSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String fromCC() {
		try {
			return in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Should not reach here.
		return null;
	}
	
	public void toCC(String msg) {
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
