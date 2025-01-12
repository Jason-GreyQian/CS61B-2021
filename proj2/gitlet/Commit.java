package gitlet;


import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 * does at a high level.
 *
 * @author GreyQian
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;
    /**
     * The timestamp of this Commit.
     */
    private Date timestamp;
    /**
     * The parent commit id.
     */
    private String directParentID;
    private String otherParentID = null;
    /**
     * The content this commit hava.
     */
    private TreeMap<String, String> trackedMaps;
    /**
     * The first commit's parent .
     */
    public static final String FIRSTCOMMITPID = "commit-1";


    // Constructor

    /**
     * The first commit.
     */
    public Commit() {
        message = "initial commit";
        directParentID = FIRSTCOMMITPID;
        trackedMaps = new TreeMap<>();
        timestamp = new Date(0);
    }

    public Commit(String message, String parentId) {
        this.message = message;
        this.directParentID = parentId;
        this.timestamp = new Date();
        Commit directParentCommit = getCommit(parentId);
        if (directParentCommit != null) {
            this.trackedMaps = directParentCommit.trackedMaps;
        } else {
            this.trackedMaps = new TreeMap<>();
        }
    }

    // Getter and Setter

    public String getDirectParentID() {
        return directParentID;
    }

    public String getMessage() {
        return message;
    }

    /** Get the Tracked File maps. */
    public Map<String, String> getTrackedFilesMap() {
        return trackedMaps;
    }

    // Some useful function

    /**
     * Get the commit based its id.
     */
    public static Commit getCommit(String commitID) {
        File commitFile = join(Repository.COMMITS, commitID);
        if (commitFile != null && commitFile.exists()) {
            return readObject(commitFile, Commit.class);
        }
        return null;
    }

    /**
     * Save the commit and return the commit id.
     */
    public String saveCommit() {
        String commitID = getCommitID();
        File commitFile = join(Repository.COMMITS, commitID);
        MyUtils.createFile(commitFile);
        writeObject(commitFile, this);
        return commitID;
    }

    /**
     * Get the commit sha1 id.
     */
    public String getCommitID() {
        return sha1(message, timestamp.toString(), directParentID, trackedMaps.toString());
    }

    /**
     * Check in the commit weather has a file that as same as the given file.
     * Use file hashCode to compare weather the content is same
     */
    public boolean isSameFile(String fileName, File file) {
        if (!trackedMaps.containsKey(fileName)) {
            return false;
        }
        String fileHash = MyUtils.getFileHash(file);
        return fileHash.equals(trackedMaps.get(fileName));
    }

    /**
     * Update the track file map based the stage add and removal.
     */
    public void updateTrackMaps(Map<String, String> add, Set<String> remove) {
        for (Map.Entry<String, String> entry : add.entrySet()) {
            trackedMaps.put(entry.getKey(), entry.getValue());
        }

        for (String fileName : remove) {
            trackedMaps.remove(fileName);
        }
    }

    /**
     * Check weather this commit tracked the file.
     */
    public boolean isTrackedFile(String fileName) {
        return trackedMaps.containsKey(fileName);
    }

    /**
     * The format is as below.
     * ===
     * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff(this is commitID)
     * Merge: 4975af1(first parent's 7th commitID) 2c1ead1(second parent's 7th commitID) if just has one parent delete this line
     * Date: Sat Nov 11 12:30:00 2017 -0800
     * Merged development into master. (commit log)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("===").append("\n");
        String commitID = getCommitID();
        sb.append("commit ").append(commitID).append("\n");
        if (otherParentID != null) {
            String firstParent = directParentID.substring(0, 7);
            String secondParent = otherParentID.substring(0, 7);
            sb.append("Merge: ").append(firstParent).append(' ').append(secondParent).append("\n");
        }
        sb.append("Date: ").append(getFormatDate()).append("\n");
        sb.append(message).append("\n");
        return sb.toString();
    }

    /**
     * Get the format date.
     * for example: Thu Nov 9 20:00:05 2017 -0800
     */
    private String getFormatDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
        return formatter.format(timestamp);
    }

}
