package WorkLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileParser {
	private String fileName;
	private int splitNo;
	
	FileParser(String fileName, int splitNo) {
		this.fileName = fileName;
		this.splitNo = splitNo;
	}
	
	public void parseFile() {
    	BufferedReader br = null;
    	int numOfKnownUser = 0;
    	String[] userIndex = new String[100001];
    	int userTransNum = 0;
    	int totalUser = 0;
    	String input;
    	Database db = new Database();
        try {
        	br = new BufferedReader(new FileReader("./" + fileName));
            while((input=br.readLine()) != null) {
            	String[] removedInitialSpace = input.split("\\s");
            	String[] extractName = removedInitialSpace[1].split(",");
            	boolean inList = false;
            	int i = 0;
            	for(i = 0; i < numOfKnownUser; i++) {
            		if(userIndex[i].equals(extractName[1])) {
            			inList = true;
            			break;
            		}
            	}
            	
            	if(!inList) {
            		if(!extractName[1].startsWith("./")) {
            			db.insertUser(extractName[1]);
            			totalUser++;
            		}
            		userIndex[i] = extractName[1];
            		numOfKnownUser++;
            		inList = false;
            	}
            	
            	userTransNum++;
            	File file=null;
            	for(int j = 0; j < 100; j++) {
	            	if(i >= splitNo*j && i < splitNo*(j+1)){
	            		int fileNo = i%splitNo;
		            	file = new File("./users"+j+"/" + fileNo + ".txt");
		            	if (!file.exists()) {
		    				file.createNewFile();
		    			}
	            	}
            	}
            	
            	FileWriter fw = new FileWriter(file,true);
            	BufferedWriter bw = new BufferedWriter(fw);
            	bw.write(String.valueOf(userTransNum)+","+removedInitialSpace[1]);
            	bw.newLine();
            	bw.flush();
            	bw.close();
            }
            System.out.println(totalUser);
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
