package Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
//import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;





//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

//import javax.xml.xpath.XPath;
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;

public class Logger {
	private static AtomicInteger currentTransAmt;
	HashMap<String,Boolean> hm;
	private static boolean shutdown;
	private static ServerSocket socketlisten = null;
	//private static ExecutorService service;
	//private XPathFactory xPathfactory;
	//private XPath xpath;
	private int currentLog;
	private XMLOutputFactory outputFactory;
	private XMLEventWriter eventWriter;
	private XMLEventFactory eventFactory;
	private XMLEvent end;
	private StartDocument startDocument;
	
	Logger() {
		currentLog = 0;
		currentTransAmt = new AtomicInteger();
		shutdown = false;
		//service = Executors.newCachedThreadPool();
	    try {
	    	outputFactory = XMLOutputFactory.newInstance();
			eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream("./testLog0"));
			eventFactory = XMLEventFactory.newInstance();
		    end = eventFactory.createDTD("\n");
		    startDocument = eventFactory.createStartDocument();
		    eventWriter.add(startDocument);
		    StartElement logStartElement = eventFactory.createStartElement("","", "log");
		    eventWriter.add(logStartElement);
		    eventWriter.add(end);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		//xPathfactory = XPathFactory.newInstance();
		//xpath = xPathfactory.newXPath();
		hm = new HashMap<String,Boolean>(10000000);
	}
	
	public void newLog() {
		currentLog++;
		currentTransAmt = new AtomicInteger();
		try {
	    	outputFactory = XMLOutputFactory.newInstance();
			eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream("./testLog"+currentLog));
			eventFactory = XMLEventFactory.newInstance();
		    end = eventFactory.createDTD("\n");
		    startDocument = eventFactory.createStartDocument();
		    eventWriter.add(startDocument);
		    StartElement logStartElement = eventFactory.createStartElement("","", "log");
		    eventWriter.add(logStartElement);
		    eventWriter.add(end);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		hm = new HashMap<String,Boolean>(10000000);
	}
	
	/**
	 * Dump information into log accordingly. Leave parameter/s as null if not required.
	 * Must include: logging type, time stamp, server, transaction number, user ID.
	 * 
	 * @param type
	 * @param timeStamp
	 * @param server
	 * @param transNum
	 * @param comd
	 * @param userName
	 * @param stockSymbol
	 * @param fileName
	 * @param funds
	 * @param price
	 * @param quoteServerTime
	 * @param crytoKey
	 * @param action
	 * @param errorMsg
	 * @param debugMsg
	 */
	private synchronized void dump(String type, String timeStamp, String server, String transNum, String comd, String userName,
			String stockSymbol, String fileName, String funds, String price, String quoteServerTime, String crytoKey,
			String action, String errorMsg, String debugMsg) {
		XMLEvent tab = eventFactory.createDTD("\t");
		XMLEvent dtab = eventFactory.createDTD("\t\t");
		try {
			// create Start node
		    StartElement sElement = eventFactory.createStartElement("", "", type);
		    eventWriter.add(tab);
		    eventWriter.add(sElement);
		    eventWriter.add(end);
		    
			sElement = eventFactory.createStartElement("", "", "timestamp");
			eventWriter.add(dtab);
		    eventWriter.add(sElement);
		    Characters characters = eventFactory.createCharacters(timeStamp);
		    eventWriter.add(characters);
		    EndElement eElement = eventFactory.createEndElement("", "", "timestamp");
		    eventWriter.add(eElement);
		    eventWriter.add(end);
		    
			sElement = eventFactory.createStartElement("", "", "server");
			eventWriter.add(dtab);
		    eventWriter.add(sElement);
		    characters = eventFactory.createCharacters(server);
		    eventWriter.add(characters);
		    eElement = eventFactory.createEndElement("", "", "server");
		    eventWriter.add(eElement);
		    eventWriter.add(end);
			
			sElement = eventFactory.createStartElement("", "", "transactionNum");
			eventWriter.add(dtab);
		    eventWriter.add(sElement);
		    characters = eventFactory.createCharacters(transNum);
		    eventWriter.add(characters);
		    eElement = eventFactory.createEndElement("", "", "transactionNum");
		    eventWriter.add(eElement);
		    eventWriter.add(end);
			
			if (!comd.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "command");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(comd);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "command");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!userName.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "username");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(userName);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "username");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!stockSymbol.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "stockSymbol");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(stockSymbol);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "stockSymbol");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!fileName.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "filename");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(fileName);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "filename");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!funds.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "funds");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(funds);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "funds");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!price.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "price");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(price);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "price");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!quoteServerTime.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "quoteServerTime");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(quoteServerTime);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "quoteServerTime");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!crytoKey.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "cryptokey");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(crytoKey);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "cryptokey");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!action.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "action");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(action);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "action");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!errorMsg.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "errorMessage");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(errorMsg);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "errorMessage");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			if (!debugMsg.equals("null")) {
				sElement = eventFactory.createStartElement("", "", "debugMessage");
				eventWriter.add(dtab);
			    eventWriter.add(sElement);
			    characters = eventFactory.createCharacters(debugMsg);
			    eventWriter.add(characters);
			    eElement = eventFactory.createEndElement("", "", "debugMessage");
			    eventWriter.add(eElement);
			    eventWriter.add(end);
			}
			
			// create End node
		    eElement = eventFactory.createEndElement("", "", type);
		    eventWriter.add(tab);
		    eventWriter.add(eElement);
		    eventWriter.add(end);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		this.notifyAll();
	}
	/**
	 * Reads all history of a user.
	 * 
	 * @param userName
	 * @return XML document.
	 */
	/*@SuppressWarnings("unused")
	private Document read(String userName) {
		Document userDoc = docBuilder.newDocument();
		Element userLog = userDoc.createElement("log");
		userDoc.appendChild(userLog);
		
		try {
			NodeList nl = (NodeList) xpath.compile("/log/[username="+userName+"]").evaluate(doc, XPathConstants.NODESET);
			for (int i = 0;null!=nl && i < nl.getLength(); i++) {
                userLog.appendChild(nl.item(i));
            }
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return userDoc;
	}*/
	
	/**
	 * Dump log for administrator use only.
	 */
	private void print(String fileName) {
		try {
			eventWriter.add(eventFactory.createEndElement("", "", "log"));
			eventWriter.add(end);
		    eventWriter.add(eventFactory.createEndDocument());
		    eventWriter.close();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class FinalPrint implements Runnable {
		private int totalTransaction;
		private String fileName = null;
		FinalPrint(int totalTransaction, String fileName) {
			this.totalTransaction = totalTransaction;
			this.fileName = fileName;
		}
		
		public void run() {
			while(currentTransAmt.get() < totalTransaction) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			print(fileName);
			System.out.println("Done");
			newLog();
			AutoLoad.INSTANCE.toAL("done");
		}	
	}
	
	private class worker implements Runnable {
		private BufferedInputStream input;
		private BufferedReader inputreader;

		worker(Socket connection) {
			try {
				connection.setKeepAlive(true);
				input = new BufferedInputStream(connection.getInputStream());
				inputreader = new BufferedReader(new InputStreamReader(input));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		public void run() {
			while(!shutdown) {
				try {
					String[] parts = inputreader.readLine().split(",");
					if(parts[0].equals("DUMPLOG")) {
						Thread t = new Thread(new FinalPrint(Integer.parseInt(parts[2]),parts[1]));
						t.start();
					}
					/*else if(parts[0].equals("shutdown")) {
						service.shutdownNow();
						socketlisten.close();
						break;
					}*/
					else {
						if(!hm.containsKey(parts[3])) {
							hm.put(parts[3], true);
							currentTransAmt.incrementAndGet();
						}
						dump(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], 
								parts[6], parts[7], parts[8], parts[9], parts[10], parts[11], 
								parts[12], parts[13], parts[14]);
					}
					parts=null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Logger Started...");
		Logger log = new Logger();
		
		try {
			socketlisten = new ServerSocket(44459,Integer.MAX_VALUE);
			while(!shutdown) {
				Socket socket = socketlisten.accept();
				//@SuppressWarnings("unused")
				//Future<?> future = service.submit(log.new worker(socket));
				Thread t = new Thread(log.new worker(socket));
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socketlisten.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
