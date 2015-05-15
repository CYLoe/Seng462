package TransServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket connection to web server.
 * @author Doraemon
 *
 */
public enum WebServer {
	INSTANCE;
	
	private ServerSocket socketlisten;
	protected final static int port = 44451;
	private Socket connection;
	private BufferedInputStream input;
	private BufferedReader inputreader;
	private BufferedOutputStream output;
	private OutputStreamWriter outputwriter;
		
	WebServer() {
		try {
			socketlisten = new ServerSocket(port,Integer.MAX_VALUE);
			connection = socketlisten.accept();
			connection.setKeepAlive(true);
			input = new BufferedInputStream(connection.getInputStream());
			inputreader = new BufferedReader(new InputStreamReader(input));
			output = new BufferedOutputStream(connection.getOutputStream());
			outputwriter = new OutputStreamWriter(output, "US-ASCII");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves data from web server.
	 * @return formatted string.
	 */
	public String fromWS() {
		try {
			return inputreader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Should not reach here.
		return null;
	}
	
	/**
	 * Sends data to web server.
	 * @param response
	 */
	public void toWS(String response) {
		try {
			outputwriter.write(response);
			outputwriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			inputreader.close();
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
