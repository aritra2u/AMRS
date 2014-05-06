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
		msd = new MillionSongDataset(baseLocation + File.separatorChar + "MillionSong", "172.27.2.7");
		DL = new DataLoader(baseLocation, msd);
	}
	
	private void start(File usersFile) {
		System.out.println(msd.getTrackFeatures("TRHWMGX128F932A8A2"));
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
