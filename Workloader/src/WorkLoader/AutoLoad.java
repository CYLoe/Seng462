package WorkLoader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public enum AutoLoad {
	INSTANCE;
	private ServerSocket socketlisten;
	protected final static int port = 44456;
	private Socket connection;
	private BufferedInputStream input;
	private BufferedReader inputreader;
		
	AutoLoad() {
		try {
			socketlisten = new ServerSocket(port);
			connection = socketlisten.accept();
			connection.setKeepAlive(true);
			input = new BufferedInputStream(connection.getInputStream());
			inputreader = new BufferedReader(new InputStreamReader(input));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves data from web server.
	 * @return formatted string.
	 */
	public String fromLog() {
		try {
			return inputreader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Should not reach here.
		return null;
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
