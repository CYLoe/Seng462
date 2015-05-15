
public class AutomatedLoading {
	
	AutomatedLoading() {}
	
	public static void main(String[] args) {
		int testAmount = Integer.parseInt(args[0]);
		Logger l = new Logger();
		Workloader[] wl = new Workloader[4];
		wl[0] = new Workloader("b133.seng.uvic.ca");
		wl[1] = new Workloader("b135.seng.uvic.ca");
		wl[2] = new Workloader("b142.seng.uvic.ca");
		wl[3] = new Workloader("b143.seng.uvic.ca");
		//wl[4] = new Workloader("b144.seng.uvic.ca");
		for(int i = 0; i < testAmount; i++) {
			String s = l.fromLog();
			while(!s.equals("done")) {
				s = l.fromLog();
			}
			Database db = new Database();
			db.updateDB();
			db.closeConnection();
			
			for(int j = 0; j < wl.length; j++) {
				wl[j].toWL("restart");
			}
		}
		for(int j = 0; j < wl.length; j++) {
			wl[j].toWL("shutdown");
			wl[j].close();
		}
		l.close();
		
	}
}
