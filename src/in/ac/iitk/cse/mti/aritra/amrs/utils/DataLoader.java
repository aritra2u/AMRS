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

import redis.clients.jedis.Jedis;
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
    private final String dataLocation;
    private final String userHistoryLocation;
    private final String userTagsLocation;
    private final ArrayList<String> tags;
    private final MillionSongDataset msdCache;
    private final static int threadCount = 1;
    private final Jedis[] dbServers;
    
    public DataLoader(String dataLocation, MillionSongDataset msdCache) {
        msd = new Trie();
        this.msdCache = msdCache;
        tags = new ArrayList<String>();
        apiKey = "d6a137eb39bc7831b26610a9d8885253";
        historyLimit = 200;
        this.dataLocation = dataLocation;
        userHistoryLocation = dataLocation + File.separatorChar + "userhistory";
        userTagsLocation = dataLocation + File.separatorChar + "usertags";
        
        dbServers = new Jedis[threadCount];
        for (int i = 0; i < threadCount; i++) {
            Jedis dbServer = new Jedis("127.0.0.1");
            dbServer.connect();
            dbServers[i] = dbServer;
        }
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
            System.err.println("Failed loading user list");
            ioe.printStackTrace();
        }
        
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new LoadUser(users, i);
            threads.add(t);
            t.start();
        }
    }
    
    private void loadTags() {
        String tags_file_location = dataLocation + File.separatorChar + "tags";
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(
                    tags_file_location)));
            String line = null;
            while ((line = br.readLine()) != null) {
                tags.add(line.toLowerCase().replaceAll("\r\n", ""));
            }
            br.close();
        } catch (IOException ioe) {
            System.err.println("Loading Tags failed");
            ioe.printStackTrace();
        }
    }
    
    private void loadTracks() {
        ArrayList<Map<String, String>> trackInformations = msdCache
                .getTrackInformations();
        for (Map<String, String> trackInformation : trackInformations) {
            String title = trackInformation.get("title").toLowerCase()
                    .replaceAll("[^a-z0-9]+", "");
            String trackId = trackInformation.get("track_id");
            msd.insert(title, trackId);
        }
        System.out.println("Total Nodes: " + msd.getNodeCount());
        System.out.println("Total Words: " + msd.getWordCount());
    }
    
    private void fetchUserHistory(String user, int threadId) {
        System.out.println("Fetching user history: " + user);
        int count = 0;
        int[] userTags = new int[tags.size()];
        
        BufferedWriter bw_hist = null;
        BufferedWriter bw_tags = null;
        
        try {
            File historyFile = new File(userHistoryLocation
                    + File.separatorChar + user);
            if (!historyFile.exists()) {
                historyFile.createNewFile();
            }
            bw_hist = new BufferedWriter(new FileWriter(
                    historyFile.getAbsoluteFile()));
            
            File tagsFile = new File(userTagsLocation + File.separatorChar
                    + user);
            if (!tagsFile.exists()) {
                tagsFile.createNewFile();
            }
            bw_tags = new BufferedWriter(new FileWriter(
                    tagsFile.getAbsoluteFile()));
            
            try {
                PaginatedResult<Track> tracks = User.getRecentTracks(user, 0,
                        historyLimit, apiKey);
                for (Track track : tracks) {
                    String trackId = getMSDTrackId(track.getName());
                    if (trackId != null) {
                        Map<String, Object> features = msdCache
                                .getTrackFeatures(trackId, dbServers[threadId]);
                        String title = (String) features.get("title");
                        String artistName = (String) features
                                .get("artist_name");
                        try {
                            bw_hist.write(track.getPlayedWhen().getTime() + ":"
                                    + trackId + "\n");
                            count++;
                        } catch (Exception e) {
                            System.err.println("Failed to write track: "
                                    + trackId);
                            e.printStackTrace();
                        }
                        try {
                            Collection<Tag> trackTags = Track.getTopTags(
                                    artistName, title, apiKey);
                            poolTags(userTags, trackTags);
                        } catch (Exception e) {
                            System.err
                            .println("Failed to fetch tags for title: "
                                    + title + ", artist: " + artistName);
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("Track not found: "
                                + track.getName());
                    }
                }
                userTags[89] -= userTags[59];
                for (int tagCount : userTags) {
                    bw_tags.write(tagCount + "\n");
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch tracks for user: " + user);
                e.printStackTrace();
            }
        } catch (IOException ioe) {
            System.err.println("Files cannot be created for user: " + user);
            ioe.printStackTrace();
        } finally {
            try {
                bw_hist.close();
                bw_tags.close();
            } catch (Exception e) {
                System.err.println("Failed to close file for user: " + user);
                e.printStackTrace();
            }
        }
        System.out.println("Tracks analysed: " + count + " (" + user + ")");
    }
    
    private void poolTags(int[] userTags, Collection<Tag> trackTags) {
        for (String oTag : tags) {
            for (Tag trackTag : trackTags) {
                String tTag = trackTag.getName().toLowerCase()
                        .replaceAll("\r\n", "");
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
        private final ArrayList<String> users;
        
        public LoadUser(ArrayList<String> users, int index) {
            this.users = users;
            this.index = index;
        }
        
        @Override
        public void run() {
            for (int i = 0; i < users.size(); i++) {
                if (i % threadCount == index) {
                    String user = users.get(i);
                    fetchUserHistory(user, index);
                }
            }
        }
    }
}
