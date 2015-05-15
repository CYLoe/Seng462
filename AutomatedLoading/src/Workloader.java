
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Workloader {
	private Socket kkSocket;
	private PrintWriter out;
	
	Workloader(String add) {
		try {
			kkSocket = new Socket(add,44456);
			kkSocket.setKeepAlive(true);
			kkSocket.setSoTimeout(0);
			out = new PrintWriter(kkSocket.getOutputStream(),true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toWL(String msg) {
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
