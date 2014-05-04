package in.ac.iitk.cse.mti.aritra.amrs;

import java.io.File;

public class Home {
	private final DataLoader DL;

	public Home() {
		String baseLocation = File.separatorChar + "home" + File.separatorChar
				+ "aritra" + File.separatorChar + "Development"
				+ File.separatorChar + "data" + File.separatorChar + "amrs";
		DL = new DataLoader(baseLocation);
	}
	
	private void start(File usersFile) {
		DL.loadData(usersFile);
	}
	
	public static void main(String... args) {
		if (args.length > 0) {
			File usersFile = new File(args[0]);
			
			Home obj = new Home();
			obj.start(usersFile);
		} else {
			throw new IllegalArgumentException();
		}
	}

}
