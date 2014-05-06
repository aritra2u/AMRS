/**
 * 
 */
package in.ac.iitk.cse.mti.aritra.amrs.utils;

import in.ac.iitk.cse.mti.aritra.amrs.datamodels.Trie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

/**
 * @author aritra
 * 
 */
public class DataLoader {
	private Trie msd;

	private final String apiKey;
	private final int historyLimit;
	private final String msdHome;
	private final String dataLocation;
	private final String userHistoryLocation;
	private final String userTagsLocation;
	private final ArrayList<String> tags;
	private final MillionSongDataset msdCache;

	public DataLoader(String dataLocation, MillionSongDataset msdCache) {
		msd = new Trie();
		this.msdCache = msdCache;
		tags = new ArrayList<String>();
		apiKey = "d6a137eb39bc7831b26610a9d8885253";
		historyLimit = 200;
		this.dataLocation = dataLocation;
		msdHome = dataLocation + File.separatorChar + "MillionSong";
		userHistoryLocation = dataLocation + File.separatorChar + "userhistory";
		userTagsLocation = dataLocation + File.separatorChar + "usertags";
	}
	
	public void loadData(File usersFile) {
		loadTags();
		loadTracks();
		loadUsers(usersFile);
	}
	
	private void loadUsers(File usersFile) {
		ArrayList<String> users = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(usersFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String user = line.replaceAll("\r\n", "");
				users.add(user);
			}
			br.close();
		} catch (IOException ioe) {
		}
		
		int threads = 150;
		for (int i = 0; i < threads; i++) {
			new LoadUser(users, i, threads).start();
		}
	}

	private void loadTags() {
		String tags_file_location = dataLocation + File.separatorChar + "tags";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(tags_file_location)));
			String line = null;
			while ((line = br.readLine()) != null) {
				tags.add(line.toLowerCase().replaceAll("\r\n", ""));
			}
			br.close();
		} catch (IOException ioe) {
		}
	}

	private void loadTracks() {
		String tracks_file_location = msdHome + File.separatorChar + "AdditionalFiles" + File.separatorChar + "unique_tracks.txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(tracks_file_location)));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] data = line.split("<SEP>");
				if (data.length == 4) {
					String title = data[3].toLowerCase().replaceAll(
							"[^a-z0-9]+", "");
					String track_id = data[0];
					msd.insert(title, track_id);
				}
			}
			br.close();
		} catch (IOException ioe) {
		}
		System.out.println("Total Nodes: " + msd.getNodeCount());
		System.out.println("Total Words: " + msd.getWordCount());
	}

	private void fetchUserHistory(String user) {
		System.out.println("Fetching user history: " + user);
		int count = 0;
		int[] userTags = new int[tags.size()];
		File historyFile = new File(userHistoryLocation + File.separatorChar + user);
		File tagsFile = new File(userTagsLocation + File.separatorChar + user);
		
		try {
			if (!historyFile.exists()) {
				historyFile.createNewFile();
			}
			if (!tagsFile.exists()) {
				tagsFile.createNewFile();
			}
			
			BufferedWriter bw_hist = new BufferedWriter(new FileWriter(historyFile.getAbsoluteFile()));
			BufferedWriter bw_tags = new BufferedWriter(new FileWriter(tagsFile.getAbsoluteFile()));
			
			PaginatedResult<Track> tracks = User.getRecentTracks(user, 0, historyLimit, apiKey);
			for (Track track : tracks) {
				String trackId = getMSDTrackId(track.getName());
				if (trackId != null) {
					Map<String, Object> features = msdCache.getTrackFeatures(trackId);
					try {
						String title = (String) features.get("title");
						String artistName = (String) features.get("artist_name");
						Collection<Tag> trackTags = Track.getTopTags(artistName, title, apiKey);
						pooltags(userTags, trackTags);
						bw_hist.write(track.getPlayedWhen().getTime() + ":" + trackId + "\n");
						count++;
					} catch (Exception e) {
						System.err.println("Failed: " + trackId);
					}
				} else {
					System.err.println("Track not found: " + track.getName());
				}
			}
			
			userTags[89] -= userTags[59];
			for (int tagCount : userTags) {
				bw_tags.write(tagCount + "\n");
			}
			
			bw_hist.close();
			bw_tags.close();
		} catch (Exception e) {
			System.err.println("Failed to write history: " + user);
		}
		System.out.println("Tracks analysed: " + count + "(" + user + ")");
	}
	
	private void pooltags(int[] userTags, Collection<Tag> trackTags) {
		for (String oTag : tags) {
			for (Tag trackTag : trackTags) {
				String tTag = trackTag.getName().toLowerCase().replaceAll("\r\n", "");
				if (tTag.contains(oTag)) {
					userTags[tags.indexOf(oTag)]++;
					break;
				}
			}
		}
	}

	private String getMSDTrackId(String rawTitle) {
		String title = rawTitle.toLowerCase().replaceAll("[^a-z0-9]+", "");
		char[] charArray = title.toCharArray();
		ArrayList<Character> charList = new ArrayList<Character>();
		for (char letter : charArray) {
			charList.add(letter);
		}
		return msd.search(charList);
	}
	
	class LoadUser extends Thread {
		private final int index;
		private final int threads;
		private final ArrayList<String> users;
		
		public LoadUser(ArrayList<String> users, int index, int threads) {
			this.users = users;
			this.index = index;
			this.threads = threads;
		}
		
		public void run() {
			for (int i = 0; i < users.size(); i++) {
				if (i % threads == index) {
					String user = users.get(i);
					fetchUserHistory(user);
				}
			}
		}
	}
}
