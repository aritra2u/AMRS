package in.ac.iitk.cse.mti.aritra.amrs.utils;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Track;


public class EchoNestData {
	private final EchoNestAPI echoNest;
	
	public EchoNestData(String apiKey) {
		this.echoNest = new EchoNestAPI(apiKey);
	}
	
	public void getTrackInformation (String trackId) {
		try {
			Track track = echoNest.newTrackByID(trackId);
			
		} catch (EchoNestException ene) {
			ene.printStackTrace();
		}
	}
}
