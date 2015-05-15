package Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public enum AutoLoad {
	INSTANCE;
	private Socket kkSocket;
	private PrintWriter out;
	
	AutoLoad() {
		try {
			kkSocket = new Socket("b146.seng.uvic.ca",44457);
			kkSocket.setKeepAlive(true);
			kkSocket.setSoTimeout(0);
			out = new PrintWriter(kkSocket.getOutputStream(),true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toAL(String msg) {
		out.println(msg);
	}
	
	public void close() {
		try {
			out.close();
			kkSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
