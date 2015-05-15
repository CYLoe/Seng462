package TransServer;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Cache the quotes from quote server.
 * 
 * @author Chuan Yun Loe
 *
 */
public enum Cache {
	INSTANCE;
	
	private ConcurrentHashMap<String, element> cache;
	private ConcurrentHashMap<String,Boolean> isUpdating;
	
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
	
	/**
	 * Resolves users' buy and sell triggers.
	 * 
	 * @author Doraemon
	 *
	 */
	private class triggerResolution implements Runnable {
		private String stockSymbol;
		private String quote;
		private String transNum;
		triggerResolution(String stockSymbol, String quote, String transNum) {
			this.stockSymbol = stockSymbol;
			this.quote = quote;
			this.transNum = transNum;
		}
		public void run() {
			Database.INSTANCE.triggerResolution(stockSymbol, quote, transNum);
		}
	}
	
	/**
	 * Constructor for Cache.
	 */
	Cache() {
		cache = new ConcurrentHashMap<String, element>(50000,0.75F,16);
		isUpdating = new ConcurrentHashMap<String, Boolean>(50000,0.75F,16);
	}
	
	/**
	 * Gets quote for given stock symbol. If quote is older than 15 seconds,
	 * this function will get new quote from quote server.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param transNum
	 * @return quote
	 */
	public String quote(String userID, String stockSymbol, String transNum) {
		element e= cache.get(stockSymbol);
 
		while(e == null || System.currentTimeMillis()-e.getTimeStamp() > 40000) {
			if(!isUpdating.containsKey(stockSymbol) || !isUpdating.get(stockSymbol)) {
				isUpdating.put(stockSymbol, true);
			}
			else {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				e= cache.get(stockSymbol);
				continue;
			}
			CentralCache newQuote = new CentralCache();
			newQuote.toCC(userID+","+stockSymbol+","+transNum);
			String[] temp = (newQuote.fromCC()).split(",");
			element newElement = new element(temp[0],Long.parseLong(temp[1]));
			cache.put(stockSymbol, newElement);
			isUpdating.put(stockSymbol, false);
			Thread t = new Thread(new triggerResolution(stockSymbol,temp[0], transNum));
			t.run();
			return temp[0];	
		}
		
		Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CACHE", 
				transNum, "QUOTE", userID, stockSymbol, null, e.getQuote(), 
				null, null, null, null, null, null);
		return e.getQuote();
	}
}
