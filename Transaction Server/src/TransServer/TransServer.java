package TransServer;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entry point of transaction server.
 * 
 * @author Chuan Yun Loe
 *
 */
public class TransServer {
	private static int numberOfThreads;
	private final static int numberOfWriters = 1;
	private static volatile boolean shutdown;
	private ConcurrentHashMap<String, userComd> userAction;
	private static BlockingQueue<String>[] instructions;
	private static Thread[] threads;
	private static Thread[] writers;

	/**
	 * Server constructor.
	 */
	@SuppressWarnings("unchecked")
	TransServer(int num) {
		numberOfThreads = num;
		userAction = new ConcurrentHashMap<String, userComd>(100000,0.75F,numberOfThreads+numberOfWriters);
		instructions = new BlockingQueue[256];
		for(int i = 0; i < numberOfThreads; i++) {
			instructions[i] = new ArrayBlockingQueue<String>(1000000,true);
		}
		shutdown = false;
		threads = new Thread[numberOfThreads];
		writers = new Thread[numberOfWriters];
	}
	
	/**
	 * Starts the server threads.
	 */
	public void startServer() {
		for(int i = 0; i < numberOfWriters; i++) {
			writers[i] = new Thread(new instructionListener());
			writers[i].start();
		}
		for(int i = 0; i < numberOfThreads; i++) {
			threads[i] = new Thread(new serverThread(i));
			threads[i].start();
		}
	}
	
	/**
	 * Stores initial command of paired commands.
	 * 
	 * @author Doraemon
	 *
	 */
	private class userComd {
		private boolean buy;
		private boolean sell;
		private boolean setBuyAmt;
		private boolean setSellAmt;
		private String buyInfo;
		private String sellInfo;
		private String setSellAmtInfo;
		
		userComd() {
			this.buy = false;
			this.sell = false;
			this.setBuyAmt = false;
			this.setSellAmt = false;
			this.buyInfo = null;
			this.sellInfo = null;
			this.setSellAmtInfo = null;
		}
		
		public boolean isBuy() {
			return buy;
		}

		public void setBuy(boolean buy) {
			this.buy = buy;
		}

		public boolean isSell() {
			return sell;
		}

		public void setSell(boolean sell) {
			this.sell = sell;
		}

		public boolean isSetBuyAmt() {
			return setBuyAmt;
		}

		public void setSetBuyAmt(boolean setBuyAmt) {
			this.setBuyAmt = setBuyAmt;
		}

		public boolean isSetSellAmt() {
			return setSellAmt;
		}

		public void setSetSellAmt(boolean setSellAmt) {
			this.setSellAmt = setSellAmt;
		}

		public String getBuyInfo() {
			return buyInfo;
		}

		public void setBuyInfo(String buyInfo) {
			this.buyInfo = buyInfo;
		}

		public String getSellInfo() {
			return sellInfo;
		}

		public void setSellInfo(String sellInfo) {
			this.sellInfo = sellInfo;
		}

		public String getSetSellAmtInfo() {
			return setSellAmtInfo;
		}

		public void setSetSellAmtInfo(String setSellAmtInfo) {
			this.setSellAmtInfo = setSellAmtInfo;
		}
	}
	
	/**
	 * Server thread for processing instructions. Each thread has its own instruction queue.
	 * 
	 * @author Chuan Yun Loe
	 *
	 */
	private class serverThread implements Runnable {
		private int threadNo;
		serverThread(int threadNo) {
			this.threadNo = threadNo;
		}
		
		public void run() {
			userComd uc = null;
			boolean correct = true;
			while(!shutdown) {
				try {
					String[] command = instructions[threadNo].take().split(",");
					if(command[1].equals("ADD") || command[1].equals("QUOTE") || command[1].equals("BUY")
							|| command[1].equals("COMMIT_BUY") || command[1].equals("CANCEL_BUY") || command[1].equals("SELL")
							|| command[1].equals("COMMIT_SELL") || command[1].equals("CANCEL_SELL") || command[1].equals("SET_BUY_AMOUNT")
							|| command[1].equals("CANCEL_SET_BUY") || command[1].equals("SET_BUY_TRIGGER") || command[1].equals("SET_SELL_AMOUNT")
							|| command[1].equals("SET_SELL_TRIGGER") || command[1].equals("CANCEL_SET_SELL") || command[1].equals("DISPLAY_SUMMARY")
							|| command[1].equals("DUMPLOG")) {
						if(command.length==3) {
							correct = true;
						}
						else {
							if(command[3].matches("[A-Z]{1,3}")) {
								if(command.length==5) {
									if(command[4].matches("\\d+\\.\\d{2}") && Double.parseDouble(command[4]) > 0.0){
										correct = true;
									}
									else {
										correct = false;
									}
								}
								else {
									correct = true;
								}
							}
							else {
								if(command[3].matches("\\d+\\.\\d{2}") && Double.parseDouble(command[3]) > 0.0){
									correct = true;
								}
								else {
									correct = false;
								}
							}
						}
					}
					else if(command[1].equals("shutdown")) {
						break;
					}
					else {
						correct = false;
					}
					if(correct) {
						switch(command[1]) {
						case "ADD": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   	command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
									int add = Database.INSTANCE.add(command[2], command[3], command[0]);
									if(add == 1) {
										// Send message back to web server: Successful.
									}
									else {
										// Send message back to web server: failed.
									}
									break;
						case "QUOTE": 	Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   		command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
										Cache.INSTANCE.quote(command[2], command[3], command[0]);
										// send quote to web server.
										break;
						case "BUY": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   	command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
									int buy = Database.INSTANCE.buy(command[2], command[3], command[4], command[0]);
									if(buy == 1) {
										if(userAction.containsKey(command[2])) {
											uc = userAction.remove(command[2]);
										}
										else {
											uc = new userComd();
										}
										uc.setBuy(true);
										uc.setBuyInfo(command[3]+","+command[4]);
										userAction.put(command[2], uc);
										// Send message back to web server: confirm buy.
									}
									else if(buy == -1) {
										if(userAction.containsKey(command[2])) {
											uc = userAction.remove(command[2]);
											uc.setBuy(false);
											userAction.put(command[2], uc);
										}
										// Send message back to web server: insufficient funds.
										
									}
									else {
										if(userAction.containsKey(command[2])) {
											uc = userAction.remove(command[2]);
											uc.setBuy(false);
											userAction.put(command[2], uc);
										}
										// Send message back to web server: error.
										
									}
									break;
						case "COMMIT_BUY": 	Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   			command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
											if(userAction.containsKey(command[2])) { 	
											   if(userAction.get(command[2]).isBuy()) {
													uc = userAction.remove(command[2]);
													int commit = Database.INSTANCE.commitBuy(command[2], uc.getBuyInfo(), command[0]);
													if(commit == 1) {
														// Send message back to web server: success.
													}
													else {
														// Send message back to web server: failed.
													}
													uc.setBuy(false);
													userAction.put(command[2], uc);
												}
											   else {
												   // Send message back to web server: invoke buy first.
												   Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke buy first.", null);
											   }
											} 
											else {
												 Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke buy first.", null);
											}
											break;
						case "CANCEL_BUY": 	Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   			command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
											if(userAction.containsKey(command[2])) { 	
											   if(userAction.get(command[2]).isBuy()) {
													uc = userAction.remove(command[2]);
													uc.setBuy(false);
													userAction.put(command[2], uc);
													Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
															command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												}
											   else {
												   // Send message back to web server: invoke buy first.
												   Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke buy first.", null);
											   }
											} 
											else {
												// Send message back to web server: invoke buy first.
												 Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke buy first.", null);
											}
											break;
						case "SELL": 	Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   		command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
										int sell = Database.INSTANCE.sell(command[2], command[3], command[4], command[0]);
										if(sell == 1) {										
											if(userAction.containsKey(command[2])) {
												uc = userAction.remove(command[2]);
											}
											else {
												uc = new userComd();
											}
											uc.setSell(true);
											uc.setSellInfo(command[3]+","+command[4]);
											userAction.put(command[2], uc);
											// Send message back to web server: confirm.
										}
										else if(sell == -1) {
											// Send message back to web server: insufficient stocks.
											if(userAction.containsKey(command[2])) {
												uc = userAction.remove(command[2]);
												uc.setSell(false);
												userAction.put(command[2], uc);
											}
										}
										else if(sell == -2) {
											// Send message back to web server: user don't have such stock.
											if(userAction.containsKey(command[2])) {
												uc = userAction.remove(command[2]);
												uc.setSell(false);
												userAction.put(command[2], uc);
											}
										}
										else {
											// Send message back to web server: error.
											if(userAction.containsKey(command[2])) {
												uc = userAction.remove(command[2]);
												uc.setSell(false);
												userAction.put(command[2], uc);
											}
										}
										break;
						case "COMMIT_SELL": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   			command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
											if(userAction.containsKey(command[2])) { 
												if(userAction.get(command[2]).isSell()) {
													uc = userAction.remove(command[2]);
													int commit = Database.INSTANCE.commitSell(command[2], uc.getSellInfo(), command[0]);
													if(commit == 1) {
														// Send message back to web server: success.
													}
													else {
														// Send message back to web server: failed.
													}
													uc.setSell(false);
													userAction.put(command[2], uc);
												}
												else {
													// Send message back to web server: invoke sell first.
													Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
															   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke sell first.", null);
												}
											}
											else {
												// Send message back to web server: invoke sell first.
												Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke sell first.", null);
											}
											break;
						case "CANCEL_SELL": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   			command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
											if(userAction.containsKey(command[2])) { 	
											   if(userAction.get(command[2]).isSell()) {
													uc = userAction.remove(command[2]);
													uc.setSell(false);
													userAction.put(command[2], uc);
													Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
															command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												}
											   else {
												// Send message back to web server: invoke sell first.
												   Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke sell first.", null);
											   }
											} 
											else {
												// Send message back to web server: invoke sell first.
												Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke sell first.", null);
											}
											break;
						case "SET_BUY_AMOUNT": 	Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   				command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												int setBuy = Database.INSTANCE.setBuyAmt(command[2], command[3], command[4], command[0]);
												if(setBuy == 1) {
													if(userAction.containsKey(command[2])) {
														uc = userAction.remove(command[2]);
													}
													else {
														uc = new userComd();
													}
													uc.setSetBuyAmt(true);
													userAction.put(command[2], uc);
													// Send message back to web server: confirm sell.
												}
												else if(setBuy == -1) {
													// Send message back to web server: insufficient funds.
													if(userAction.containsKey(command[2])) {
														uc = userAction.remove(command[2]);
														uc.setSetBuyAmt(false);
														userAction.put(command[2], uc);
													}
												}
												else {
													// Send message back to web server: error.
													if(userAction.containsKey(command[2])) {
														uc = userAction.remove(command[2]);
														uc.setSetBuyAmt(false);
														userAction.put(command[2], uc);
													}
												}
												break;
						case "SET_BUY_TRIGGER": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   				command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												if(userAction.containsKey(command[2])) { 
													if(userAction.get(command[2]).isSetBuyAmt()) {
														uc = userAction.remove(command[2]);
														int setBuyTrig = Database.INSTANCE.setBuyTrigger(command[2], command[3], command[4], command[0]);
														if(setBuyTrig == 1) {
															// Send message back to web server: success.
														}
														else {
															// Send message back to web server: failed.
														}
														uc.setSetBuyAmt(false);
														userAction.put(command[2], uc);
													}
													else {
														// Send message back to web server: invoke set_buy_amount first.
														Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
																   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke set_buy_amount first.", null);
													}
												}
												else {
													// Send message back to web server: invoke set_buy_amount first.
													Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
															   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke set_buy_amount first.", null);
												}
												break;
						case "CANCEL_SET_BUY": 	Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   				command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												int cancelSetBuy = Database.INSTANCE.cancelSetBuy(command[2], command[3], command[0]);
												if(cancelSetBuy == 1) {
													// Send message back to web server: success.
												}
												else if(cancelSetBuy == -1) {
													// Send message back to web server: no such trigger.
												}
												else {
													// Send message back to web server: failed.
												}
												break;
						case "SET_SELL_AMOUNT": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   				command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												// int setSell = Database.INSTANCE.setSellAmt(command[2], command[3], command[4], command[0]);
												//if(setSell == 1) {
												if(userAction.containsKey(command[2])) {
													uc = userAction.remove(command[2]);
												}
												else {
													uc = new userComd();
												}
												uc.setSetSellAmt(true);
												uc.setSetSellAmtInfo(command[3] + "," + command[4]);
												userAction.put(command[2], uc);
												Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														command[0], command[1], command[2], command[3], null, command[4], null, null, null, null, null, null);
													// Send message back to web server: confirm.
												/*}
												else if(setSell == -1) {
													// Send message back to web server: insufficient stocks.
													if(userAction.containsKey(command[2]))
														userAction.remove(command[2]);
												}
												else {
													// Send message back to web server: error.
													if(userAction.containsKey(command[2]))
														userAction.remove(command[2]);
												}*/
												break;
						case "SET_SELL_TRIGGER":Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   				command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												if(userAction.containsKey(command[2])) { 
													if(userAction.get(command[2]).isSetSellAmt()) {
														uc = userAction.remove(command[2]);
														String[] parts = uc.getSetSellAmtInfo().split(",");
														if(!parts[0].equals(command[3])) {
															// Send message back to web server: unmatched stock.
															break;
														}
														int setSellTrig = Database.INSTANCE.setSellTrigger(command[2], command[3], command[4], parts[1], command[0]);
														if(setSellTrig == 1) {
															// Send message back to web server: success.
														}
														else if(setSellTrig == -2) {
															// Send message back to web server: insufficient stocks.
														}
														else if(setSellTrig == -3) {
															// Send message back to web server: no such stocks.
														}
														else {
															// Send message back to web server: failed.
														}
														uc.setSetSellAmt(false);
														userAction.put(command[2], uc);
													}
													else {
														// Send message back to web server: invoke set_sell_amount first.
														Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
																   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke set_sell_amount first.", null);
													}
												}
												else {
													// Send message back to web server: invoke set_sell_amount first.
													Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
															   command[0], command[1], command[2], null, null, null, null, null, null, null, "Invoke set_sell_amount first.", null);
												}
												break;
						case "CANCEL_SET_SELL": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   				command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												int cancelSetSell = Database.INSTANCE.cancelSetSell(command[2], command[3], command[0]);
												if(cancelSetSell == 1) {
													// Send message back to web server: success.
												}
												else if(cancelSetSell == -1) {
													// Send message back to web server: no such trigger.
												}
												else {
													// Send message back to web server: failed.
												}
												break;
						case "DUMPLOG": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   		command[0], command[1], null, null, null, null, null, null, null, null, null, null);
										if(command[2].startsWith("./")) {
											Dumplog.INSTANCE.toDL("DUMPLOG,"+command[2]+","+command[0]);
											System.out.println("Dumped");
										}
										else {
											//Dumplog.INSTANCE.read(command[2]);
											Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
													command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
										}
										break; // Send return value to web server.
						case "DISPLAY_SUMMARY": Dumplog.INSTANCE.dump("userCommand", String.valueOf(System.currentTimeMillis()), "CLT",
								   				command[0], command[1], null, null, null, null, null, null, null, null, null, null);
												//Database.INSTANCE.displaySummary(command[2], command[0]);
												//Dumplog.INSTANCE.read(command[2]);
												Dumplog.INSTANCE.dump("systemEvent", String.valueOf(System.currentTimeMillis()), "CLT",
														command[0], command[1], command[2], null, null, null, null, null, null, null, null, null);
												break;
						case "shutdown": Dumplog.INSTANCE.toDL("shutdown"); break;
						default: // Send message back to web server: command error.
							Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
									   command[0], command[1], command[2], null, null, null, null, null, null, null, "Unknown command.", null);
								break;
						}
					}
					else {
						Dumplog.INSTANCE.dump("errorEvent", String.valueOf(System.currentTimeMillis()), "CLT",
								   command[0], command[1], command[2], null, null, null, null, null, null, null, "Error command.", null);
					}
				
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * Thread for retrieving instructions from web server.
	 * 
	 * @author Chuan Yun Loe
	 *
	 */
	private class instructionListener implements Runnable {
		private HashMap<String,Integer> userThread;
		private String instruction;
		private int threadAssign;
		
		instructionListener() {
			userThread = new HashMap<String,Integer>(100000);
			userThread.put("./testLOG",0);
			threadAssign = 0;
		}
		
		public void run(){
			while(true) {
				instruction = WebServer.INSTANCE.fromWS();
				if(instruction.equals("shutdown")) {
					shutdown = true;
					for(int i = 0; i < numberOfThreads; i++) {
						instructions[i].add(String.valueOf(i)+",shutdown");
					}
					break;
				}
				String[] parts = instruction.split(",");
				if(userThread.containsKey(parts[2])) {
					instructions[userThread.get(parts[2])].add(instruction);
				}
				else {
					userThread.put(parts[2], threadAssign);
					instructions[threadAssign].add(instruction);
					threadAssign++;
					if(threadAssign >= numberOfThreads) {
						threadAssign = 0;
					}
				}
			}
			WebServer.INSTANCE.close();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Server Started.");
		TransServer server = new TransServer(Integer.parseInt(args[0]));
		Dumplog.INSTANCE.setServerNo(args[1]);
		server.startServer();
		try {
			for(int i = 0; i < numberOfWriters; i++) {
				writers[i].join();
			}
			for(int i = 0; i < numberOfThreads; i++) {
				threads[i].join();
			}
		} catch(Exception e) {}
		Database.INSTANCE.closeConnection();
		Dumplog.INSTANCE.close();
		System.out.println("Server Ended.");
	}

}
