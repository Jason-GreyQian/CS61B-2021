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

    /**
     * Checkout function.
     * There are 3 usages:
     * 1. java gitlet.Main checkout -- [file name]
     * 2. java gitlet.Main checkout [commit id] -- [file name]
     * 3. java gitlet.Main checkout [branch name]
     */
    public static void checkout(String[] args) {
        if (args.length == 2) {
            // checkout branch
            checkoutBranch(args[1]);
        } else if (args.length == 3 && args[1].equals("--")) {
            //  checkout -- [file name]
            checkoutFile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            // checkout [commit id] -- [file name]
            checkoutFile(args[3], args[1]);
        } else {
            MyUtils.exit("Incorrect operands.");
        }
    }

    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     * If a branch with the given name already exists, print the error message A branch with that name already exists.
     */
    public static void branch(String branchName) {
        getInfoMaps();

        if (branches.containsKey(branchName)) {
            MyUtils.exit("A branch with that name already exists.");
        }

        String currentCommitID = getCurrentCommit();
        branches.put(branchName, currentCommitID);

        saveInfoMaps();
    }

    /**
     * Rm the branch.
     * Deletes the branch with the given name.
     * it does not mean to delete all commits that were created under the branch, or anything like that.
     * If a branch with the given name does not exist, aborts. Print the error message A branch with that name does not exist.
     * If you try to remove the branch you’re currently on, aborts, printing the error message Cannot remove the current branch.
     */
    public static void rmBranch(String branchName) {
        getInfoMaps();

        if (!branches.containsKey(branchName)) {
            MyUtils.exit("A branch with that name does not exist.");
        }
        String currentBranch = getCurrentBranch();
        if (currentBranch.equals(branchName)) {
            MyUtils.exit("Cannot remove the current branch.");
        }
        branches.remove(branchName);

        saveInfoMaps();
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * clear the stage area
     * update the HEAD
     */
    public static void reset(String commitID) {
        getInfoMaps();

        commitID = getFullCommitID(commitID);

        String currentCommitID = getCurrentCommit();
        String currentBranch = getCurrentBranch();
        Commit currentCommit = Commit.getCommit(currentCommitID);
        Commit resetCommit = Commit.getCommit(commitID);

        // If a working file is untracked in the current branch and would be overwritten by the reset,
        // print `There is an untracked file in the way; delete it, or add and commit it first.`
        // TODO: NEED MODIFY?
        checkUntrackedFile(commitID);

        // Checks out all the files tracked by the given commit.
        for (String fileName : resetCommit.getTrackedFilesMap().keySet()) {
            checkoutFile(fileName, commitID);
        }

        // Removes tracked files that are not present in that commit.
        Set<String> trackedFiles = new HashSet<>();
        getTrackedFiles(trackedFiles);
        for (String fileName : trackedFiles) {
            if (resetCommit.getTrackedFilesMap().containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }

        // clear the stage area
        stageAdd.clear();
        stageRemoval.clear();
        saveInfoMaps();

        // update the HEAD
        updateHEAD(currentBranch, commitID);
    }

    /**
     * merge function.
     */
    public static void merge(String branchName) {
        // TODO: NEED DO
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

    /**
     * Checkout branches.
     * 1. Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * 2. The given branch will now be considered the current branch (HEAD).
     * 3. Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * 4. The staging area is cleared
     * Some spacial cases:
     * If no branch with that name exists, print No such branch exists.
     * If that branch is the current branch, print No need to checkout the current branch.
     * If a working file is untracked in the current branch and would be overwritten by the checkout,
     * print There is an untracked file in the way; delete it, or add and commit it first. and exit;
     */
    private static void checkoutBranch(String branch) {
        getInfoMaps();
        String currentBranch = getCurrentBranch();
        String currentCommitID = getCurrentCommit();
        Commit currentCommit = Commit.getCommit(currentCommitID);

        if (!branches.containsKey(branch)) {
            MyUtils.exit("No such branch exists.");
        }

        if (currentBranch.equals(branch)) {
            MyUtils.exit("No need to checkout the current branch.");
        }

        // check weather the untracked file would been overwrite
        // TODO: NEED TO SIMPLFY
        Set<String> untrackedFiles = new HashSet<>();
        getUntrackedFiles(untrackedFiles);
        String checkoutCommitID = branches.get(branch);
        Commit checkoutCommit = Commit.getCommit(checkoutCommitID);
        for (String fileName : untrackedFiles) {
            if (checkoutCommit.isTrackedFile(fileName)) {
                // check weather would be overwrite
                File file = join(CWD, fileName);
                if (!checkoutCommit.isSameFile(fileName, file)) { // content is not same, can overwrite
                    MyUtils.exit("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }

        // Takes all files in the commit at the head of the given branch, and puts them in the working directory
        List<String> workingSpace = Utils.plainFilenamesIn(CWD);
        for (Map.Entry<String, String> entry : checkoutCommit.getTrackedFilesMap().entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            File file = join(CWD, fileName);
            if (workingSpace.contains(fileName) && checkoutCommit.isSameFile(fileName, file)) {
                // working space has the file and content is same don't need rewrite
                continue;
            } else {
                // rewrite the file
                MyUtils.createFile(file);
                String content = readContentsAsString(join(BLOBS, fileHash));
                writeContents(file, content);
            }
        }


        // Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        for (String fileName : currentCommit.getTrackedFilesMap().keySet()) {
            if (!checkoutCommit.isTrackedFile(fileName)) {
                File file = join(CWD, fileName);
                restrictedDelete(file);
            }

        }

        // update the breach and commit
        updateHEAD(branch, checkoutCommitID);

        // clear the stageing area
        stageAdd.clear();
        stageRemoval.clear();
        saveInfoMaps();

    }

    /**
     * Checkout files.
     * The commit is the current commit.
     */
    private static void checkoutFile(String fileName) {
        String currentCommit = getCurrentCommit();
        checkoutFile(fileName, currentCommit);
    }

    /**
     * Checkout files to identify commit id version.
     * TODO: Commit id may less than 40 letters ？
     * Takes the version of the file as it exists in the commit id and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     */
    private static void checkoutFile(String fileName, String commitID) {

        commitID = getFullCommitID(commitID);

        Commit commit = Commit.getCommit(commitID);
        // TODO: IS NO NEED?
//        if (commit == null) {
//            MyUtils.exit("No commit with that id exists.");
//        }

        // get the file's content in the commit
        File file = join(BLOBS, commit.getFileHash(fileName));
        String content = readContentsAsString(file);

        // rewrite the content
        File workingSpaceFile = join(CWD, fileName);
        MyUtils.createFile(workingSpaceFile);
        writeContents(workingSpaceFile, content);
    }

    /**
     * Get the untracked files.
     * Untracked files : exist in working space but not add and not commit(include the rm files)
     */
    private static void getUntrackedFiles(Set<String> untrackedFiles) {
        List<String> workingSpace = Utils.plainFilenamesIn(CWD);
        Commit commit = Commit.getCommit(getCurrentCommit());

        for (String fileName : workingSpace) {
            if (!stageAdd.containsKey(fileName) && !commit.isTrackedFile(fileName)) {
                untrackedFiles.add(fileName);
            }
        }
    }

    /**
     * Get the real CommitID.
     */
    private static String getFullCommitID(String commitID) {
        int length = commitID.length();
        if (length == 40) {
            return commitID;
        }
        List<String> commitsID = plainFilenamesIn(commitID);
        List<String> fullCommitIDs = new ArrayList<>();
        for (String id : commitsID) {
            if (id.substring(0, length).equals(commitID)) {
                fullCommitIDs.add(id);
            }
        }
        if (fullCommitIDs.size() == 0) {
            MyUtils.exit("No commit with that id exists.");
        } else if (fullCommitIDs.size() > 1) {
            MyUtils.exit("Multiple commits with that id exists.");
        }
        return fullCommitIDs.get(0);
    }

    /**
     * Check untracked files.
     */
    private static void checkUntrackedFile(String checkoutCommitID) {
        Set<String> untrackedFiles = new HashSet<>();
        getUntrackedFiles(untrackedFiles);
        Commit checkoutCommit = Commit.getCommit(checkoutCommitID);
        for (String fileName : untrackedFiles) {
            if (checkoutCommit.isTrackedFile(fileName)) {
                // check weather would be overwrite
                File file = join(CWD, fileName);
                if (!checkoutCommit.isSameFile(fileName, file)) { // content is not same, can overwrite
                    MyUtils.exit("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }
    }

    /**
     * Get the tracked files.
     */
    private static void getTrackedFiles(Set<String> trackedFiles) {
        Commit commit = Commit.getCommit(getCurrentCommit());
        for (String fileName : commit.getTrackedFilesMap().keySet()) {
            trackedFiles.add(fileName);
        }
        for (String fileName : stageAdd.keySet()) {
            trackedFiles.add(fileName);
        }
    }

}
