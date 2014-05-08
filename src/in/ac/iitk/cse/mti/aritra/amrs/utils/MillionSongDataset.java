package in.ac.iitk.cse.mti.aritra.amrs.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ncsa.hdf.object.h5.H5File;
import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MillionSongDataset {
	private final String msdHome;
	private final Gson gson;
	
	private final ArrayList<Map<String, String>> trackInformations;
	
	public MillionSongDataset(String msdHome) {
		this.msdHome = msdHome;
		this.gson = new Gson();
		
		trackInformations = new ArrayList<Map<String, String>>();
	}
	
	public Map<String, Object> getTrackFeatures(String trackId, Jedis dbServer) {
		String json = dbServer.get(trackId);
		if (json == null) {
			json = saveTrackFeatures(trackId, dbServer);
		}
		Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
		return gson.fromJson(json, mapType);
	}
	
	private String saveTrackFeatures(String trackId, Jedis dbServer) {
		H5File track = hdf5_getters.hdf5_open_readonly(getHDF5Path(trackId));
		try {
			Map<String, Object> features = new HashMap<String, Object>();
			
			String artistId = hdf5_getters.get_artist_id(track);
			features.put("artist_id", artistId);
			
			String artistName = hdf5_getters.get_artist_name(track);
			features.put("artist_name", artistName);
			
			double energy = hdf5_getters.get_energy(track);
			features.put("energy", energy);
			
			double loudness = hdf5_getters.get_loudness(track);
			features.put("loudness", loudness);
			
			double songHotness = hdf5_getters.get_song_hotttnesss(track);
			songHotness = Double.isNaN(songHotness) ? 0.0 : songHotness;
			features.put("song_hotttnesss", songHotness);
			
			double tempo = hdf5_getters.get_tempo(track);
			features.put("tempo", tempo);
			
			String title = hdf5_getters.get_title(track);
			features.put("title", title);
			
			dbServer.set(trackId, gson.toJson(features));
		} catch (Exception e) {
			System.err.println("Failed to obtain track information: " + trackId);
			e.printStackTrace();
		}
		hdf5_getters.hdf5_close(track);
		return dbServer.get(trackId);
	}
	
	private String getHDF5Path(String trackId) {
		return msdHome + File.separatorChar + "data" + File.separatorChar
				+ trackId.charAt(2) + File.separatorChar + trackId.charAt(3)
				+ File.separatorChar + trackId.charAt(4) + File.separatorChar
				+ trackId + ".h5";
	}
	
	private ArrayList<Map<String, String>> loadTracks() {
		String tracksFileURL = msdHome + File.separatorChar + "AdditionalFiles" + File.separatorChar + "unique_tracks.txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(tracksFileURL)));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] data = line.split("<SEP>");
				if (data.length == 4) {
					Map<String, String> trackInformation = new HashMap<String, String>();
					trackInformation.put("track_id", data[0]);
					trackInformation.put("song_id", data[1]);
					trackInformation.put("artist_name", data[2]);
					trackInformation.put("title", data[3]);
					trackInformations.add(trackInformation);
				}
			}
			br.close();
		} catch (IOException ioe) {
			System.err.println("Error in loading track informations");
		}
		return trackInformations;
	}
	
	public ArrayList<Map<String, String>> getTrackInformations() {
		if (trackInformations.size() == 0) {
			loadTracks();
		}
		return trackInformations;
	}
}
