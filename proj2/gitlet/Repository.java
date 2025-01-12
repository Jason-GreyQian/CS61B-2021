package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 * complete all the commands of gitlet
 *
 * @author GreyQian
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * A map which store the file and its hashcode of stage add files.
     */
    public static final File STAGE_ADD = join(GITLET_DIR, "Stage_Add");
    /**
     * A map which store the file and its hashcode of stage removal files.
     */
    public static final File STAGE_REMOVAL = join(GITLET_DIR, "Stage_REMOVAL");
    /**
     * The folder that store the commits.
     */
    public static final File COMMITS = join(GITLET_DIR, "Commits");
    /**
     * The folder that store the file, file name is hashcode based the file content is.
     */
    public static final File BLOBS = join(GITLET_DIR, "Blobs");
    /**
     * The file that store the latest commit id and the current branch's name.
     */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /**
     * A map which used for store the branch and its latest commit.
     */
    public static final File BRANCHES = join(GITLET_DIR, "Branches");

    /**
     * Default branch name.
     */
    private static final String DEFAULT_BRANCH = "master";
    /**
     * The map of stage add.
     */
    private static TreeMap<String, String> stageAdd = null;
    /**
     * The set of stage removal.
     */
    private static HashSet<String> stageRemoval = null;
    /**
     * The map of branched.
     */
    private static TreeMap<String, String> branches = null;


    // The rest is the function of gitlet

    /**
     * Initialize a repository at the current working directory.
     * <p>
     * The struct as below:
     * .gitlet
     * |----Stage_Add
     * |----Stage_Removal
     * |----HEAD
     * |----Branches
     * |----Commits
     * |----the commit file obj
     * |----Blobs
     * |----the blobs obj store in it
     */
    public static void init() {
        // If there is already a Gitlet version-control system in the current directory, it should abort.
        if (GITLET_DIR.exists()) {
            MyUtils.exit("A Gitlet version-control system already exists in the current directory.");
        }

        // setup the files and folders
        initRepository();

        // create a default initial commit and default branch master
        Commit initialCommit = new Commit();
        String initialCommitID = initialCommit.saveCommit();
        branches.put(DEFAULT_BRANCH, initialCommitID);

        // serializable the files
        updateHEAD(DEFAULT_BRANCH, initialCommitID);
        saveInfoMaps();
    }

    /**
     * Add function.
     * Add a copy of files to the staging area
     * Special cases:
     * 1. the content is same as the current commit's content, don't need to add, if the file in the add area remove it
     * 2. stage an already-staged file overwrites the previous entry in the staging area with the new contents.
     * 3. if the file is stage_rm ,the file will no longer be staged for removal
     * 4. If the file does not exist, print the error message File does not exist.
     */
    public static void add(String fileName) {
        // If the file does not exist, print the error message File does not exist.
        File file = join(CWD, fileName);
        if (!file.exists()) {
            MyUtils.exit("File does not exist.");
        }

        getInfoMaps();
        String currentCommitID = getCurrentCommit();
        Commit currentCommit = Commit.getCommit(currentCommitID);

        if (currentCommit.isSameFile(fileName, file)) {         // if the file's content is same as the current commit's, don't stage it
            stageAdd.remove(fileName);
        } else {                                                // else add the file and save the copy of it
            String fileHash = MyUtils.saveBlobFile(file);
            stageAdd.put(fileName, fileHash);
        }

        if (stageRemoval.contains(fileName)) {
            stageRemoval.remove(fileName);
        }

        saveInfoMaps();
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time
     * Create a new commit, By default, each commit’s snapshot of files will as same as its parent commit’s snapshot of files;
     * Update the stage add area to the commit
     * Untrack the file which is been stage removal
     * Special case:
     * 1. commit message is blank, print the error message
     * 2. the stage area is empty , print the error message
     */
    public static void commit(String message) {
        if (message.isEmpty()) {
            MyUtils.exit("Please enter a commit message.");
        }

        getInfoMaps();
        if (stageAdd.isEmpty() && stageRemoval.isEmpty()) {
            MyUtils.exit("No changes added to the commit.");
        }

        // create a new commit and update its maps
        String parentCommitID = getCurrentCommit();
        Commit newCommit = new Commit(message, parentCommitID);
        newCommit.updateTrackMaps(stageAdd, stageRemoval);
        String newCommitID = newCommit.saveCommit();

        // empty the stage area
        stageAdd.clear();
        stageRemoval.clear();

        // update the files
        String currentBranch = getCurrentBranch();
        branches.put(currentBranch, newCommitID);
        updateHEAD(currentBranch, newCommitID);
        saveInfoMaps();
    }

    /**
     * Unstage the file.
     * if file is currently staged for addition unstaged it
     * If the file is tracked in the current commit, stage it for removal and remove the file from the working directory
     * if the user has not already done so (do not remove it unless it is tracked in the current commit).
     * If the file is neither staged nor tracked by the head commit, print the error message No reason to remove the file.
     *
     * @param fileName
     */
    public static void rm(String fileName) {
        getInfoMaps();

        String currentCommitID = getCurrentCommit();
        Commit currentCommit = Commit.getCommit(currentCommitID);

        // if file is currently staged for addition unstaged it
        if (stageAdd.containsKey(fileName)) {               // if file is currently staged for addition unstaged it
            stageAdd.remove(fileName);
            return;
        } else if (currentCommit.isTrackedFile(fileName)) { // If the file is tracked in the current commit
            File file = join(CWD, fileName);
            // stage if for removal
            stageRemoval.add(fileName);
            // remove the file if user has not already done so
            if (file.exists()) {
                restrictedDelete(file);
            }
        } else {                                            // If the file is neither staged nor tracked by the head commit, print the error message.
            MyUtils.exit("No reason to remove the file.");
        }

        saveInfoMaps();
    }

    /**
     * Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit.
     */
    public static void log() {
        String commitID = getCurrentCommit();

        while (!commitID.equals(Commit.FIRSTCOMMITPID)) {
            Commit commit = Commit.getCommit(commitID);
            System.out.println(commit.toString());
            commitID = commit.getDirectParentID();
        }
    }

    /**
     * Global log.
     * Like log, except displays information about all commits ever made. The order of the commits does not matter.
     * Iterate the cimmits folder
     */
    public static void globalLog() {
        List<String> commitsID = Utils.plainFilenamesIn(COMMITS);
        for (String commitID : commitsID) {
            Commit commit = Commit.getCommit(commitID);
            System.out.println(commit.toString());
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line.
     * If no such commit exists, prints the error message Found no commit with that message.
     */
    public static void find(String message) {
        List<String> commitsID = Utils.plainFilenamesIn(COMMITS);
        boolean found = false;

        for (String commitID : commitsID) {
            Commit commit = Commit.getCommit(commitID);
            String commitMessage = commit.getMessage();
            if (message.equals(commitMessage)) {
                System.out.println(commitID);
                found = true;
            }
        }

        if (!found) {
            MyUtils.exit("Found no commit with that message.");
        }

    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     * The example as below:
     * === Branches ===
     * *master
     * other-branch
     * <p>
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     * <p>
     * === Removed Files ===
     * goodbye.txt
     * <p>
     * === Modifications Not Staged For Commit ===
     * junk.txt (deleted)
     * wug3.txt (modified)
     * <p>
     * === Untracked Files ===
     * random.stuff
     */
    public static void status() {
        getInfoMaps();
        String currentBranch = getCurrentBranch();

        // add the current branches
        System.out.println("=== Branches ===");
        System.out.println("*" + currentBranch);
        for (String branch : branches.keySet()) {
            if (!branch.equals(currentBranch)) {
                System.out.println(branch);
            }
        }
        System.out.println();

        // add the staged add files
        System.out.println("=== Staged Files ===");
        MyUtils.printFiles(stageAdd.keySet(), "");
        System.out.println();

        // add the removal files
        System.out.println("=== Removed Files ===");
        MyUtils.printFiles(stageRemoval, "");
        System.out.println();

        Set<String> modifiedFiles = new HashSet<>();
        Set<String> deletedFiles = new HashSet<>();
        Set<String> untrackedFiles = new HashSet<>();
        getFileStatus(modifiedFiles, deletedFiles, untrackedFiles);

        // Modifications but Not Staged
        System.out.println("=== Modifications Not Staged For Commit ===");
        MyUtils.printFiles(deletedFiles, " (deleted)");
        MyUtils.printFiles(modifiedFiles, " (modified)");
        System.out.println();

        // Untracked Files
        System.out.println("=== Untracked Files ===");
        MyUtils.printFiles(untrackedFiles, "");
        System.out.println();
    }


    // ================================================================================================================
    // This below is the Helper function
    // ================================================================================================================

    /**
     * Helper function for setup files and the maps.
     */
    public static void initRepository() {
        GITLET_DIR.mkdirs();
        COMMITS.mkdirs();
        BLOBS.mkdirs();

        MyUtils.createFile(STAGE_ADD);
        MyUtils.createFile(STAGE_REMOVAL);
        MyUtils.createFile(BRANCHES);
        MyUtils.createFile(HEAD);

        branches = new TreeMap<>();
        stageAdd = new TreeMap<>();
        stageRemoval = new HashSet<>();
    }

    /**
     * Helper function for get the maps we need.
     * the stage maps, branch maps
     */
    @SuppressWarnings("unchecked")
    private static void getInfoMaps() {
        branches = readObject(BRANCHES, TreeMap.class);
        stageAdd = readObject(STAGE_ADD, TreeMap.class);
        stageRemoval = readObject(STAGE_REMOVAL, HashSet.class);
    }

    /**
     * Helper function for save the maps we need.
     */
    private static void saveInfoMaps() {
        writeObject(BRANCHES, branches);
        writeObject(STAGE_ADD, stageAdd);
        writeObject(STAGE_REMOVAL, stageRemoval);
    }

    /**
     * Helper function for update the HEAD content.
     * The HEAD is current branch name + '\n' + latest commit of the current branch
     */
    private static void updateHEAD(String branch, String commitID) {
        writeContents(HEAD, branch + "\n" + commitID);
    }

    /**
     * Helper function for get the current branch.
     * From HEAD file to get.
     */
    private static String getCurrentBranch() {
        String content = readContentsAsString(HEAD);
        return content.split("\n")[0];
    }

    /**
     * Helper function for get the current commit.
     * From HEAD file to get.
     */
    private static String getCurrentCommit() {
        String content = readContentsAsString(HEAD);
        return content.split("\n")[1];
    }

    /**
     * A user inputs a command that requires being in an initialized Gitlet working directory
     * (i.e., one containing a .gitlet subdirectory), but is not in such a directory,
     * print the message Not in an initialized Gitlet directory.
     */
    public static void checkWorkingDirectory() {
        if (!GITLET_DIR.exists()) {
            MyUtils.exit("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * Get the file status.
     * Untracked files : exist in working space but not add and not commit(include the rm files)
     * Modified files :
     * 1. commit but the content is change and the change is not add
     * 2. added but the content is different of the working space content
     * Deleted Files:
     * 1. added but not in the working space
     * 2. not in stage removal but commit it and not in the current working space
     */
    private static void getFileStatus(Set<String> modifiedFiles, Set<String> deletedFiles, Set<String> untrackedFiles) {
        List<String> workingSpace = Utils.plainFilenamesIn(CWD);
        Commit currentCommit = Commit.getCommit(getCurrentCommit());

        for (String fileName : workingSpace) {
            if (!stageAdd.containsKey(fileName) && !currentCommit.isTrackedFile(fileName)) {
                untrackedFiles.add(fileName);
            }
        }

        for (Map.Entry<String, String> entry : currentCommit.getTrackedFilesMap().entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();

            // commit and in the working space
            if (workingSpace.contains(fileName)) {
                String workingFileHash = MyUtils.getFileHash(join(CWD, fileName));
                if (!fileHash.equals(workingFileHash) && !stageAdd.containsKey(fileName)) {
                    // content is not same and not in stage add area
                    modifiedFiles.add(fileName);
                }
            } else {            // commit but not in the working space
                if (!stageRemoval.contains(fileName)) {
                    deletedFiles.add(fileName);
                }
            }
        }

        for (Map.Entry<String, String> entry : stageAdd.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            // added and in the working space
            if (workingSpace.contains(fileName)) {
                String workingFileHash = MyUtils.getFileHash(join(CWD, fileName));
                if (!fileHash.equals(workingFileHash)) {
                    modifiedFiles.add(fileName);
                }
            } else {
                // added but not in the working space
                deletedFiles.add(fileName);
            }
        }
    }

}
