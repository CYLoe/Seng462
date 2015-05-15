package TransServer;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

/**
 * Connect to customer database.
 * 
 * @author Chuan Yun Loe
 *
 */
public enum Database {
	INSTANCE;
	
	private Jdbc3PoolingDataSource source;
	private ConcurrentHashMap<String,Money> account;
	
	/**
	 * Constructor
	 */
	Database() {
		account = new ConcurrentHashMap<String,Money>(3000,0.75F,16);
		try {
			Class.forName("org.postgresql.Driver");
			source = new Jdbc3PoolingDataSource();
			source.setDataSourceName("Customer");
			source.setServerName("b139.seng.uvic.ca:44450");
			source.setDatabaseName("customerDB");
			source.setUser("root");
			source.setPassword("password1");
			source.setInitialConnections(128);
			source.setMaxConnections(200);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Passes sql update string to customer database.
	 * @param s
	 * @return 1 means successful update; else failed.
	 */
	private int[] updateDB(String[] s) {
		int[] val = null;
		Connection connection = null;
		try {
			connection = source.getConnection();
			Statement st = connection.createStatement();
			for(int i = 0; i < s.length; i++) {
				st.addBatch(s[i]);
			}
			val = st.executeBatch();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				try {connection.close();} catch (SQLException e) {}
			}
		}
		return val;		
	}
	
	/**
	 * Passes sql query string to customer database.
	 * Parse result accordingly.
	 * 
	 * @param s
	 * @return ResultSet object containing query result.
	 */
	private ResultSet queryDB(String s) {
		ResultSet res = null;
		Connection connection = null;
		try {
			connection = source.getConnection();
			Statement st = connection.createStatement();
			res = st.executeQuery(s);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				try {connection.close();} catch (SQLException e) {}
			}
		}
		return res;
	}
	
	/**
	 * Resets DB for test each time.
	 * Closes connection to customer database.
	 * Run this method before the end of program run.
	 */
	public void closeConnection() {
		try {
			source.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private class Money {
		private int dollar;
		private int cent;
		Money(int dollar, int cent) {
			this.dollar = dollar;
			this.cent = cent;
		}
		public int getDollar() {
			return dollar;
		}
		public int getCent() {
			return cent;
		}
		@Override public String toString() {
			String s;
			if(cent < 10) {
				s = String.valueOf(dollar)+".0"+String.valueOf(cent);
			}
			else {
				s = String.valueOf(dollar)+"."+String.valueOf(cent);
			}
			return s;
		}
	}
	
	private void accUpdate(String userID, int dollar, int cent) {
		Money m = new Money(dollar,cent);
		account.replace(userID, m);
	}
	
	private Money accCache(String userID) {
		if(account.containsKey(userID)) {
			return account.get(userID);
		}
		else {
			Money m = null;
			ResultSet res = queryDB("SELECT Dollar, Cent"
								 + " FROM Account"
								 + " WHERE UserID='" + userID + "'");
			try {
				if(res.next()) {
					m = new Money(res.getInt("Dollar"),res.getInt("Cent"));
					account.put(userID, m);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return m;
		}
	}
	
	/**
	 * Add funds to a user's account in the database.
	 * Input amount normally. This function will split dollars
	 * and cents before updating to database.
	 * 
	 * @param userid
	 * @param amount
	 * @return 1 means added successfully; else failed.
	 */
	public int add(String userID, String amount, String transNum) {
		String[] parts = amount.split("\\.");
		Money m = accCache(userID);
		int dollar = 0;
		int cent = 0;

		if(m!=null) {
			dollar = m.getDollar() + Integer.parseInt(parts[0]);
			cent = m.getCent() + Integer.parseInt(parts[1]);
			if(cent >= 100) {
				cent = cent - 100;
				dollar = dollar + 1;
			}
		}
		accUpdate(userID, dollar, cent);
		String[] update = {"UPDATE Account"
						+ " SET Dollar=" + m.getDollar()
						+ ",Cent=" + m.getCent()
						+ " WHERE UserID='" + userID + "'"};
		int[] val = updateDB(update);
		if(val[0] == 1)
			Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, null, userID, null, null, m.toString(), null, null, null, "ADD", null, null);
		else
			Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "ADD", userID, null, null, null, null, null, null, null, "Database update error.", null);
		return val[0];
	}
	
	/**
	 * Compares requested buying amount to user's account.
	 * Note: input amount normally, dollars and cents will be split automatically.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param amount
	 * @param transNum
	 * @return 1 if enough funds; -1 means not enough money in account; else failed.
	 */
	public int buy(String userID, String stockSymbol, String amount, String transNum) {
		String[] parts = amount.split("\\.");

		Money m = accCache(userID);
	
		// Check whether enough funds in account.
		if(m!=null) {
			int userDollar = m.getDollar();
			int userCent = m.getCent();
			int buyDollar = Integer.parseInt(parts[0]);
			int buyCent = Integer.parseInt(parts[1]);
			
			if(userDollar < buyDollar) {
				Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
						transNum, "BUY", userID, stockSymbol, null, amount, null, null, null, null, "Insufficient funds.", null);
				return -1;
			}
			else if(userDollar == buyDollar)
				if(userCent < buyCent) {
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "BUY", userID, stockSymbol, null, amount, null, null, null, null, "Insufficient funds.", null);
					return -1;
				}
			Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "BUY", userID, stockSymbol, null, amount, null, null, null, null, null, null);
			return 1;
		}
		
		// Should not reach here at all.
		return 0;
	}
	
	/**
	 * Cancels buy trigger for given stock.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param transNum
	 * @return 1 if successful; -1 if no such trigger; else failed.
	 */
	public int cancelSetBuy(String userID, String stockSymbol, String transNum) {
		ResultSet res = queryDB("SELECT buydollar, buycent"
							 + " FROM Reserve"
							 + " Where UserID='" + userID + "' AND Symbol='" + stockSymbol + "'");
		try {
			if(res.next()) {
				int dollar = 0;
				int cent = 0;
				String amount = dollar+"."+cent;
				
				Money m = accCache(userID);
				if(m!=null) {
					dollar = res.getInt("buydollar") + m.getDollar();
					cent = res.getInt("buycent") + m.getCent();
					if(cent >= 100) {
						cent = cent - 100;
						dollar = dollar + 1;
					}
				}
				
				accUpdate(userID, dollar, cent);
				String[] update = {"UPDATE Account"
								+ " SET Dollar=" + m.getDollar()
								+ ",Cent=" + m.getCent()
								+ " WHERE UserID='" + userID + "'",
								   "DELETE FROM Reserve"
								+ " Where UserID='" + userID + "' AND Symbol='" + stockSymbol + "'"};
				int[] val = updateDB(update);
				if(val[0] == 1)
					Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, null, userID, null, null, amount, null, null, null, "cancel_set_buy", null, null);
				else
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "CANCEL_SET_BUY", userID, stockSymbol, null, null, null, null, null, null, "Database update error.", null);
				
				return val[0];
			}
			else
				return -1;
		}  catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Should not reach here at all.
		return 0;
	}
	
	/**
	 * Cancels sell trigger for given stock.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param transNum
	 * @return 1 if successful; -1 if no such trigger; else failed.
	 */
	public int cancelSetSell(String userID, String stockSymbol, String transNum) {
		ResultSet res = queryDB("SELECT stockamt"
							 + " FROM Stockreserve"
							 + " Where UserID='" + userID + "' AND Symbol='" + stockSymbol + "'");
		try {
			if(res.next()) {
				int amount = res.getInt("stockamt");
				String amt = String.valueOf(amount);
				
				String[] update = {"UPDATE Stocks"
								+ " SET stockamt=stockamt+" + amount
								+ " Where UserID='" + userID + "' AND Symbol='" +stockSymbol + "'",
								   "DELETE FROM Stockreserve"
								+ " Where UserID='" + userID + "' AND Symbol='" + stockSymbol + "'"};
				int[] val = updateDB(update);
				if(val[0] == 1)
					Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, null, userID, null, null, amt, null, null, null, "cancel_set_sell", null, null);
				else
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "CANCEL_SET_SELL", userID, stockSymbol, null, null, null, null, null, null, "Database update error.", null);
				return val[0];
			}
			else
				return -1;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Should not reach here at all.
		return 0;
	}
	
	/**
	 * Updates user's account for funds and stock.
	 * 
	 * @param userID
	 * @param info
	 * @param transNum
	 * @return 1 means successful; -1 if insufficient funds; else failed.
	 */
	public int commitBuy(String userID, String info, String transNum) {
		String[] parts = info.split(",");
		double userFund = Double.parseDouble(parts[1]);
		double stockPrice = Double.parseDouble(Cache.INSTANCE.quote(userID, parts[0], transNum));

		// Calculations for amount to deduct and stock amount to add.
		double temp = userFund/stockPrice;

		double fracPart = temp % 1;
		double intPart = temp - fracPart;
		int stockAmount = (int) intPart;

		String userDeduct = String.valueOf(intPart*stockPrice);

		String[] parts2 = userDeduct.split("\\.");

		Money m = accCache(userID);
		int dollar = 0;
		int cent = 0;
		
		if(m!=null) {
			dollar = m.getDollar() - Integer.parseInt(parts2[0]);
			cent = m.getCent() - Integer.parseInt(parts2[1].substring(0, 1));
			if(cent < 0) {
				cent = cent + 100;
				dollar = dollar - 1;
			}
			if(dollar < 0)
				return -1;
		}

		accUpdate(userID, dollar, cent);
		String[] update = {"UPDATE Account"
						+ " SET Dollar=" + m.getDollar()
						+ ",Cent=" + m.getCent()
						+ " WHERE UserID='" + userID + "'"};
		int[] val = updateDB(update);
		
		@SuppressWarnings("unused")
		ResultSet res = queryDB("SELECT upsertstock('" + userID + "','" + parts[0] + "'," + stockAmount + ")");
		//val = 1; // Modify after figuring out return value from res.

		if(val[0] == 1)
			Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, null, userID, null, null, String.valueOf(stockAmount), null, null, null, "commit_buy", null, null);
		else
			Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "COMMIT_BUY", userID, parts[0], null, null, null, null, null, null, "Database update error.", null);

		return val[0];
	}
	
	/**
	 * Updates user's account for funds and stock.
	 * 
	 * @param userID
	 * @param info
	 * @param transNum
	 * @return 1 means successful; else failed.
	 */
	public int commitSell(String userID, String info, String transNum) {
		String[] parts = info.split(",");
		double sellAmount = Double.parseDouble(parts[1]);
		double stockPrice = Double.parseDouble(Cache.INSTANCE.quote(userID, parts[0], transNum));
		
		// Calculations for amount to add and stock amount to deduct.
		double temp = sellAmount/stockPrice;
		double fracPart = temp % 1;
		double intPart = temp - fracPart;
		int stockAmount = (int)intPart;
		String userAdd = String.valueOf(intPart*stockPrice);
		String[] parts2 = userAdd.split("\\.");

		Money m = accCache(userID);
		int dollar = 0;
		int cent = 0;

		if(m!=null) {
			dollar = m.getDollar() + Integer.parseInt(parts2[0]);
			cent = m.getCent() + Integer.parseInt(parts2[1].substring(0, 1));
			if(cent >= 100) {
				cent = cent - 100;
				dollar = dollar + 1;
			}
		}
		
		accUpdate(userID, dollar, cent);
		String[] update = {"UPDATE Account"
						+ " SET Dollar=" + m.getDollar()
						+ ",Cent=" + m.getCent()
						+ " WHERE UserID='" + userID + "'",
						   "UPDATE Stocks"
						+ " SET stockamt=stockamt-" + stockAmount
						+ " Where UserID='" + userID + "' AND Symbol='" + parts[0] + "'"};
		
		int[] val = updateDB(update);
		
		if(val[0] == 1)
			Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, null, userID, null, null, String.valueOf(stockAmount), null, null, null, "commit_sell", null, null);
		else {
			Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "COMMIT_SELL", userID, parts[0], null, null, null, null, null, null, "Database update error.", null);
			return -1;
		}
		
		return val[0];
	}
	
	/**
	 * Compares requested selling stock amount to user's account.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param amount
	 * @param transNum
	 * @return 1 if enough stocks; -1 if not enough stocks; -2 if no such stock; else failed.
	 */
	public int sell(String userID, String stockSymbol, String amount, String transNum) {
		ResultSet res = queryDB("SELECT stockamt"
							 + " FROM Stocks"
							 + " Where UserID='" + userID + "' AND Symbol='" + stockSymbol + "'");
		
		try {
			if(!res.next()){
				Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
						transNum, "SELL", userID, stockSymbol, null, null, null, null, null, null, "No such stock in account.", null);
				return -2;
			}

			int stockAmount = res.getInt("stockamt");
			double stockPrice = Double.parseDouble(Cache.INSTANCE.quote(userID, stockSymbol, transNum));
			double stockAvail =  stockPrice * stockAmount;
			if(stockAvail < Double.parseDouble(amount)) {
				Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
						transNum, "SELL", userID, stockSymbol, null, null, null, null, null, null, "Insufficient Stock.", null);
				return -1;
			}
			
			Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "SELL", userID, stockSymbol, null, amount, null, null, null, null, null, null);
			return 1;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Should not reach here at all.
		return 0;
	}
	
	/**
	 * Checks whether user has enough fund.
	 * Then puts buy amount into Reserve account.
	 * Then deduct from user's account accordingly.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param amount
	 * @param transNum
	 * @return 1 if successful; -1 if insufficient funds; else failed.
	 */
	public int setBuyAmt(String userID, String stockSymbol, String amount, String transNum) {
		String[] parts = amount.split("\\.");

		Money m = accCache(userID);
		if(Double.parseDouble(m.toString()) < Double.parseDouble(amount)) {
			Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "SELL", userID, stockSymbol, null, m.toString(), null, null, null, null, "Insufficient funds.", null);
			return -1;
		}
		try {
			if(m!=null) {
				int dollar = 0;
				int cent = 0;

				dollar = m.getDollar() - Integer.parseInt(parts[0]);
				cent = m.getCent() - Integer.parseInt(parts[1]);
				if(cent < 0) {
					cent = cent + 100;
					dollar = dollar - 1;
				}

				@SuppressWarnings("unused")
				ResultSet res = queryDB("SELECT upsertreserve('" + userID + "','" + stockSymbol + "'," + parts[0] + "," + parts[1] +")");
				//int val = 1; // Modify after figuring out return value from res.
				accUpdate(userID, dollar, cent);
				String[] update = {"UPDATE Account"
								+ " SET Dollar=" + m.getDollar()
								+ ",Cent=" + m.getCent()
								+ " WHERE UserID='" + userID + "'"};
				int[] val = updateDB(update);
				if(val[0] == 1)
					Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, null, userID, null, null, amount, null, null, null, "set_buy_amount", null, null);
				else
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "SET_BUY_AMOUNT", userID, stockSymbol, null, amount, null, null, null, null, "Database update error.", null);

				
				
				return val[0];
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		// Should not reach here at all.
		return 0;
	}
	
	/**
	 * Checks user's available stocks. DEPRECIATED
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param amount
	 * @param transNum
	 * @return 1 if successful; -1 if insufficient stock; else failed.
	 */
	public int setSellAmt(String userID, String stockSymbol, String amount, String transNum) {
		double requestedAmt = Double.parseDouble(amount);
		ResultSet res = queryDB("SELECT stockamt"
							 + " FROM Stocks"
							 + " Where UserID='" + userID + "' AND Symbol='" + stockSymbol + "'");

		try {
			if(res.next()) {
				int stockAmt = res.getInt("stockamt");
				if(stockAmt < requestedAmt) {
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "SET_SELL_AMOUNT", userID, stockSymbol, null, amount, null, null, null, null, "Insufficient stock.", null);
					return -1;
				}
				Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
						transNum, "SET_SELL_AMOUNT", userID, stockSymbol, null, amount, null, null, null, null, null, null);
				return 1;
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Should not reach here at all.
		return 0;
	}
	
	/**
	 * Sets buy trigger for given stock.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param amount
	 * @param transNum
	 * @return 1 if successful; else failed
	 */
	public int setBuyTrigger(String userID, String stockSymbol, String amount, String transNum) {
		String[] parts = amount.split("\\.");
		String[] update = {"UPDATE Reserve"
						+ " SET trigdollar=" + parts[0] + ", trigcent=" + parts[1]
						+ " WHERE UserID='" + userID + "' AND Symbol='" + stockSymbol + "'"};
		int[] val = updateDB(update);
		if(val[0] == 1)
			Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, null, userID, null, null, amount, null, null, null, "set_buy_trigger", null, null);
		else
			Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "SET_BUY_TRIGGER", userID, stockSymbol, null, amount, null, null, null, null, "Database update error.", null);
		return val[0];
	}
	
	/**
	 * Creates/updates trigger database.
	 * Then deduct from Stocks database.
	 * 
	 * @param userID
	 * @param stockSymbol
	 * @param amount
	 * @param transNum
	 * @return 1 if successful; -2 if insufficient stock; -3 if no such stock; else failed.
	 */
	public int setSellTrigger(String userID, String stockSymbol, String amount, String stockQuantity, String transNum) {
		String[] parts = amount.split("\\.");
		String[] parts2 = stockQuantity.split("\\.");
		// Checks whether enough stock
		int reqAmt = (Integer.parseInt(parts2[0])*100 + Integer.parseInt(parts2[1]))/(Integer.parseInt(parts[0])*100 + Integer.parseInt(parts[1]));	
		
		ResultSet res = queryDB("SELECT stockamt"
							 + " FROM Stocks"
							 + " Where UserID='" + userID + "' AND Symbol='" + stockSymbol + "'");
		try {
			if(res.next()) {
				int stockAmt = res.getInt("stockamt");
				if(stockAmt < reqAmt) {
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "SET_SELL_AMOUNT", userID, stockSymbol, null, amount, null, null, null, null, "Insufficient stock.", null);
					return -2;
				}
			}
			else
				return -3;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		res = queryDB("SELECT upsertstockres('" + userID + "','" + stockSymbol + "'," + reqAmt + "," + parts[0] + "," + parts[1] +")");
		//int[] val; // Modify after figuring out return value from res.
		String[] update = {"UPDATE Stocks"
						+ " SET stockamt=stockamt-" + reqAmt
						+ " WHERE UserID='" + userID + "' AND Symbol='" + stockSymbol + "'"};
		
		int[] val = updateDB(update);
		if(val[0] == 1)
			Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, null, userID, null, null, amount, null, null, null, "set_sell_trigger", null, null);
		else {
			Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
					transNum, "SET_SELL_TRIGGER", userID, String.valueOf(reqAmt), null, amount, null, null, null, null, "Database update error.", null);
			return -1;
		}
		
		return val[0];
	}
	
	/**
	 * Retrieves all user's stocks which fulfills trigger condition and operates on them accordingly.
	 * 
	 * @param stockSymbol
	 * @param quote
	 * @param transNum
	 */
	public void triggerResolution(String stockSymbol, String quote, String transNum) {
		String[] parts = quote.split("\\.");
		int quoteInCents = Integer.parseInt(parts[0])*100 + Integer.parseInt(parts[1]);
		
		// Buy triggers
		ResultSet res = queryDB("SELECT UserID, buydollar, buycent"
							 + " FROM Reserve"
							 + " WHERE Symbol='" + stockSymbol + "' AND trigdollar>=" + parts[0] + " AND trigcent>=" + parts[1]);
		
		try {
			int dollar, cent, refundDollar, refundCent, stocks;
			String userID;
			while(res.next()) {
				userID = res.getString("UserID");
				dollar = res.getInt("buydollar");
				cent = res.getInt("buycent");
				String[] update = {"DELETE FROM Reserve"
								+ " WHERE UserID='" + userID + "' AND Symbol='" + stockSymbol + "'"};
				int[] val = updateDB(update);
				if(val[0] == 1)
					Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, null, userID, null, null, "0", null, null, null, "BUY", null, null);
				else
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "BUY", userID, stockSymbol, null, "0", null, null, null, null, "Database update error.", null);
				
				// Calculate refund & buyable stocks
				stocks = (dollar*100 + cent)/(quoteInCents);
				refundDollar = dollar - (stocks*quoteInCents)/100;
				refundCent = cent - (stocks*quoteInCents)%100;
				if(refundCent < 0) {
					refundCent = refundCent + 100;
					refundDollar = refundDollar - 1;
				}
				Money m = accCache(userID);
				if(m!=null) {
					dollar = m.getDollar() + refundDollar;
					cent = m.getCent() + refundCent;
					if(cent >= 100) {
						cent = cent - 100;
						dollar = dollar + 1;
					}
					accUpdate(userID, dollar, cent);
					String[] update2 = {"UPDATE Account"
									 + " SET Dollar=" + m.getDollar()
									 + ",Cent=" + m.getCent()
									 + " WHERE UserID='" + userID + "'",
									    "UPDATE Stocks"
									 + " SET stockamt=" + stocks
									 + " WHERE UserID='" + userID + "' AND Symbol='" + stockSymbol + "'"};
					
					val = updateDB(update2);
					if(val[0] == 1)
						Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
								transNum, null, userID, null, null, String.valueOf(stocks), null, null, null, "BUY", null, null);
					else
						Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
								transNum, "BUY", userID, stockSymbol, null, String.valueOf(stocks), null, null, null, null, "Database update error.", null);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Sell triggers
		res = queryDB("SELECT UserID, stockamt"
				   + " FROM Stockreserve"
				   + " WHERE Symbol='" + stockSymbol + "' AND trigdollar<=" + parts[0] + " AND trigdollar>=0 AND trigcent<=" + parts[1]);
		try {
			int dollar, cent, soldDollar, soldCent, stocks;
			String userID;
			while(res.next()) {
				userID = res.getString("UserID");
				stocks = res.getInt("stockamt");
				String[] update = {"DELETE FROM Stockreserve"
								+ " WHERE UserID='" + userID + "' AND Symbol='" + stockSymbol + "'"};
				int[] val = updateDB(update);
				if(val[0] == 1)
					Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, null, userID, null, null, "0", null, null, null, "SELL", null, null);
				else
					Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
							transNum, "SELL", userID, stockSymbol, null, "0", null, null, null, null, "Database update error.", null);
				
				// Calculate amount earned from sales of stock
				soldDollar = stocks*Integer.parseInt(parts[0]);
				soldCent = stocks*Integer.parseInt(parts[1]);
				while(soldCent >= 100) {
					soldCent = soldCent - 100;
					soldDollar = soldDollar + 1;
				}
				
				Money m = accCache(userID);
				if(m!=null) {
					dollar = m.getDollar() + soldDollar;
					cent = m.getCent() + soldCent;
					if(cent >= 100) {
						cent = cent - 100;
						dollar = dollar + 1;
					}
					accUpdate(userID, dollar, cent);
					String[] update2 = {"UPDATE Account"
									 + " SET Dollar=" + m.getDollar()
									 + ",Cent=" + m.getCent()
									 + " WHERE UserID='" + userID + "'"};
					val = updateDB(update2);
					if(val[0] == 1)
						Dumplog.INSTANCE.dump("accountTransaction", String.valueOf(System.currentTimeMillis()), "CLT",
								transNum, null, userID, null, null, "0", null, null, null, "SELL", null, null);
					else
						Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
								transNum, "SELL", userID, stockSymbol, null, "0", null, null, null, null, "Database update error.", null);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves all information of the user which are stored in the database. 
	 * 
	 * @param userID
	 * @param transNum
	 * @return Formatted string of user's information.
	 */
	public String displaySummary(String userID, String transNum) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("====================================");
			Money m = accCache(userID);
			sb.append("Total available funds:\n");
			sb.append("Dollar\tCent\n");
			if(m!=null) {
				sb.append(String.valueOf(m.getDollar()));
				sb.append("\t");
				sb.append(String.valueOf(m.getCent()));
				sb.append("\n");
			}
			sb.append("====================================");
			
			ResultSet res = queryDB("SELECT *"
							     + " FROM Stocks"
					    	     + " WHERE UserID='" + userID + "'");
			sb.append("Total Stocks:\n");
			sb.append("Stock\tAmount\n");
			if(res.next()) {
				sb.append(res.getString("Symbol"));
				sb.append("\t");
				sb.append(String.valueOf(res.getInt("stockamt")));
				sb.append("\n");
			}
			sb.append("====================================");
			
			res = queryDB("SELECT *"
					   + " FROM Reserve"
			    	   + " WHERE UserID='" + userID + "'");
			sb.append("Buy Triggers:\n");
			sb.append("Stock\tBuy Amount\tTrigger\n");
			while(res.next()) {
				sb.append(res.getString("Symbol"));
				sb.append("\t");
				sb.append(String.valueOf(res.getInt("buydollar")));
				sb.append(".");
				sb.append(String.valueOf(res.getInt("buycent")));
				sb.append("\t");
				sb.append(String.valueOf(res.getInt("trigdollar")));
				sb.append(".");
				sb.append(String.valueOf(res.getInt("trigcent")));
				sb.append("\n");
			}
			sb.append("====================================");
			
			res = queryDB("SELECT *"
					   + " FROM Stockreserve"
			    	   + " WHERE UserID='" + userID + "'");
			sb.append("Sell Triggers:\n");
			sb.append("Stock\tSell Amount\tTrigger\n");
			while(res.next()) {
				sb.append(res.getString("Symbol"));
				sb.append("\t");
				sb.append(String.valueOf(res.getInt("stockamt")));
				sb.append("\t");
				sb.append(String.valueOf(res.getInt("trigdollar")));
				sb.append(".");
				sb.append(String.valueOf(res.getInt("trigcent")));
				sb.append("\n");
			}
			sb.append("====================================");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
}
