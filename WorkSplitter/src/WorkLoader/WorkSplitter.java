package WorkLoader;

public class WorkSplitter {

	public static void main(String[] args) {
		FileParser fp = new FileParser(args[0],Integer.parseInt(args[1]));
		fp.parseFile();
	}

}
