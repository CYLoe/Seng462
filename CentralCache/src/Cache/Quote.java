package Cache;
import java.io.*;
import java.net.*;


/**
 * Connects to quote server and get quote.
 * 
 * @author Chuan Yun Loe
 *
 */
public class Quote {
	private Socket kkSocket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private String fromServer;
    private String fromUser;
    
    /**
     * Empty constructor.
     */
	Quote() {}
	
	/**
	 * Gets quote from quote server. Returns a string that requires parsing.
	 * Do not get quote from here. Instead, get quote from Cache.INSTANCE.quote(...);
	 * 
	 * @param stockSymbol
	 * @param userID
	 * @param transNum
	 * @return quote,sym,userid,timestamp,cryptokey
	 */
	public String requestQuote(String stockSymbol, String userID, String transNum) {
		boolean correctQuote = false;
		String[] parts = null;
		while (!correctQuote) {
			try {
				kkSocket = new Socket("quoteserve.seng.uvic.ca", 4445);
				out = new PrintWriter(kkSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host: quoteserve.seng.uvic.ca");
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection Project Quote Server likely down.");
				System.exit(1);
			}
			fromUser = stockSymbol + ", " + userID;
			out.println(fromUser);
			try {
				// Returns: quote,sym,userid,timestamp,cryptokey
				fromServer = in.readLine();
				out.close();
				in.close();
				kkSocket.close();
				
				fromServer.replaceAll("\\s", "");
				parts = fromServer.split(",");
				if(parts[0].matches("\\d+\\.\\d{2}") && Double.parseDouble(parts[0]) != 0.0 && parts[1].equals(stockSymbol) && parts[2].equals(userID) && 
						Long.parseLong(parts[3]) > 1058860973300L && Long.parseLong(parts[3]) < 1401656133000L)
					correctQuote = true;
				else
					correctQuote = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Dumplog.INSTANCE.dump("quoteServer", String.valueOf(System.currentTimeMillis()), "QSRV", 
				transNum, null, userID, stockSymbol, null, null, 
				parts[0], parts[3], parts[4], null, null, null);
		return fromServer;
	}
}
