package in.ac.iitk.cse.mti.aritra.amrs;

import in.ac.iitk.cse.mti.aritra.amrs.utils.DataLoader;
import in.ac.iitk.cse.mti.aritra.amrs.utils.MillionSongDataset;

import java.io.File;

public class Home {
	private final DataLoader DL;
	private final MillionSongDataset msd;

	public Home() {
		String baseLocation = File.separatorChar + "home" + File.separatorChar
				+ "aritra" + File.separatorChar + "Development"
				+ File.separatorChar + "data" + File.separatorChar + "amrs";
		msd = new MillionSongDataset(baseLocation + File.separatorChar + "MillionSong");
		DL = new DataLoader(baseLocation, msd);
	}
	
	private void start(File usersFile) {
		DL.loadData(usersFile);
	}
	
	public static void main(String... args) {
		File usersFile;
		if (args.length > 0) {
			usersFile = new File(args[0]);
		} else {
			usersFile = new File("/home/aritra/Development/data/users");
		}
		Home obj = new Home();
		obj.start(usersFile);
	}

}
