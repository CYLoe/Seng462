package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TransServer {
	private Socket kkSocket;
	private PrintWriter out;
	private BufferedReader in;

	TransServer() {
		String address = "b137.seng.uvic.ca"; //"b131.seng.uvic.ca"|"b132.seng.uvic.ca"|"b134.seng.uvic.ca"|"b137.seng.uvic.ca"|"b140.seng.uvic.ca"
		try {
			kkSocket = new Socket(address,44451);
			kkSocket.setKeepAlive(true);
			kkSocket.setSoTimeout(0);
			out = new PrintWriter(kkSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+address);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection Server likely down.");
            System.exit(1);
        }
	}
	
	public String fromTS() {
		try {
			return in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Should not reach here.
		return null;
	}
	
	public void toTS(String msg) {
		out.println(msg);
	}
	
	public boolean isClosed() {
		return kkSocket.isClosed();
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
