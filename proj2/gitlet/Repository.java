package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;


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
     * |----|----the commit file obj
     * |----Blobs
     * |----|----the blobs obj store in it
     */
    public static void init() {
        // If there is already a Gitlet version-control system
        // in the current directory, it should abort.
        if (GITLET_DIR.exists()) {
            MyUtils.exit("A Gitlet version-control system "
                    + "already exists in the current directory.");
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
     * 1. the content is same as the current commit's content,
     * don't need to add, if the file in the add area remove it
     * 2. stage an already-staged file overwrites the previous entry
     * in the staging area with the new contents.
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

        if (currentCommit.isSameFile(fileName, file)) {
            // if the file's content is same as the current commit's, don't stage it
            stageAdd.remove(fileName);
        } else {
            // if the file's content is same as the current commit's, don't stage it
            String fileHash = MyUtils.saveBlobFile(file);
            stageAdd.put(fileName, fileHash);
        }

        if (stageRemoval.contains(fileName)) {
            stageRemoval.remove(fileName);
        }

        saveInfoMaps();
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time
     * Create a new commit, By default, each commit’s snapshot of files will
     * as same as its parent commit’s snapshot of files;
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
     * If the file is tracked in the current commit,stage it for removal
     * and remove the file from the working directory
     * if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * If the file is neither staged nor tracked by the head commit,
     * print the error message No reason to remove the file.
     *
     * @param fileName
     */
    public static void rm(String fileName) {
        getInfoMaps();

        String currentCommitID = getCurrentCommit();
        Commit currentCommit = Commit.getCommit(currentCommitID);

        // if file is currently staged for addition unstaged it
        if (stageAdd.containsKey(fileName)) {
            // if file is currently staged for addition unstaged it
            stageAdd.remove(fileName);
        } else if (currentCommit.isTrackedFile(fileName)) {
            // If the file is tracked in the current commit
            File file = join(CWD, fileName);
            // stage if for removal
            stageRemoval.add(fileName);
            // remove the file if user has not already done so
            if (file.exists()) {
                restrictedDelete(file);
            }
        } else {
            // If the file is neither staged nor tracked by the head commit, print the error message.
            MyUtils.exit("No reason to remove the file.");
        }

        saveInfoMaps();
    }

    /**
     * Starting at the current head commit, display information about each commit backwards
     * along the commit tree until the initial commit.
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
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
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
     * If a branch with the given name already exists,
     * print the error message A branch with that name already exists.
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
     * it does not mean to delete all commits that were created
     * under the branch, or anything like that.
     * If a branch with the given name does not exist, aborts.
     * Print the error message A branch with that name does not exist.
     * If you try to remove the branch you’re currently on, aborts,
     * printing the error message Cannot remove the current branch.
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
    public static void reset(String resetCommitID) {
        getInfoMaps();

        resetCommitID = getFullCommitID(resetCommitID);
        List<String> commitsID = Utils.plainFilenamesIn(COMMITS);
        if (!commitsID.contains(resetCommitID)) {
            MyUtils.exit("No commit with that id exists.");
        }


        String currentCommitID = getCurrentCommit();
        String currentBranch = getCurrentBranch();

        checkUntrackedFile(resetCommitID);

        checkoutCommit(currentCommitID, resetCommitID);

        // clear the stage area
        stageAdd.clear();
        stageRemoval.clear();
        branches.put(currentBranch, resetCommitID);
        saveInfoMaps();

        // update the HEAD
        updateHEAD(currentBranch, resetCommitID);
    }

    /**
     * merge function.
     * Processing steps:
     * 1.Any files that have been modified in the given branch since the split point,
     * but not modified in the current branch since
     * the split point should be changed to their versions
     * in the given branch (checked out from the commit at the front of the given branch).
     * These files should then all be automatically staged.
     * 2.Any files that have been modified in the current branch but not in the given branch
     * since the split point should stay as they are.
     * <p>
     * Special cases not need merge.
     * 1.If the split point is the same commit as the given branch, then we do nothing;
     * the merge is complete,
     * and the operation ends with the message Given branch is
     * an ancestor of the current branch.
     * 2.If the split point is the current branch, then the effect is to
     * check out the given branch, and the operation ends after
     * printing the message Current branch fast-forwarded.
     * <p>
     * Failure cases:
     * 1.If there are staged additions or removals present, print the error message
     * You have uncommitted changes.
     * 2.If a branch with the given name does not exist,
     * print the error message A branch with that name does not exist.
     * 3.If attempting to merge a branch with itself, print the error message
     * Cannot merge a branch with itself.
     * 4.If an untracked file in the current commit would be overwritten or deleted by the merge,
     * print There is an untracked file in the way; delete it, or add and commit it first. and exit;
     */
    public static void merge(String branchName) {
        getInfoMaps();

        // Failure cases
        if (!stageAdd.isEmpty() || !stageRemoval.isEmpty()) {
            MyUtils.exit("You have uncommitted changes.");
        }
        if (!branches.containsKey(branchName)) {
            MyUtils.exit("A branch with that name does not exist.");
        }
        if (branchName.equals(getCurrentBranch())) {
            MyUtils.exit("Cannot merge a branch with itself.");
        }

        String currentBranch = getCurrentBranch();
        String currentCommitID = getCurrentCommit();
        String givenBranchCommitID = branches.get(branchName);
        Commit currentCommit = Commit.getCommit(currentCommitID);
        Commit givenCommit = Commit.getCommit(givenBranchCommitID);
        checkUntrackedFile(givenBranchCommitID);

        // Special case not need merge
        String splitPoint = getSplitPoint(branchName);
        Commit splitCommit = Commit.getCommit(splitPoint);
        if (splitPoint.equals(givenBranchCommitID)) {
            // do nothing
            MyUtils.exit("Given branch is an ancestor of the current branch.");

        }
        if (splitPoint.equals(currentCommitID)) {
            // fast forwarded
            checkoutCommit(currentCommitID, givenBranchCommitID);
            branches.put(currentBranch, givenBranchCommitID);
            updateHEAD(currentBranch, givenBranchCommitID);
            saveInfoMaps();
            MyUtils.exit("Current branch fast-forwarded.");
        }

        // Merge steps:
        Set<String> currentModified = new HashSet<>();
        Set<String> givenModified = new HashSet<>();
        Set<String> currentAdded = new HashSet<>();
        Set<String> givenAdded = new HashSet<>();
        Set<String> currentRemoved = new HashSet<>();
        Set<String> givenRemoved = new HashSet<>();
        Set<String> currentChanged = new HashSet<>();
        Set<String> givenChanged = new HashSet<>();
        Set<String> currentUnModified = new HashSet<>();
        Set<String> givenUnModified = new HashSet<>();
        splitModified(splitCommit, currentCommit, currentModified, currentUnModified,
                currentAdded, currentRemoved, currentChanged);
        splitModified(splitCommit, givenCommit, givenModified, givenUnModified,
                givenAdded, givenRemoved, givenChanged);

        // 给定分支修改了，但是当前分支没有修改过的文件需要更改给定分支的版本，并且add
        // 1.given branch changed content, but current branch doesn't modified,
        // checked out from the commit at the front of the given branch version, stage it
        for (String fileName : givenChanged) {
            if (currentUnModified.contains(fileName)) {
                checkoutFile(fileName, givenBranchCommitID);
                stageAdd.put(fileName, givenCommit.getFileHash(fileName));
            }
        }

        // 在当前分支中修改过但在给定分支中没有修改的文件，这些文件将保持不变，保留当前分支的内容。
        // 2.current branch changed content but given branch unmodified, stay it

        // 在当前分支和给定分支中修改相同文件的内容，且修改方式相同
        // （例如都修改了内容或者都删除了文件），这些文件在合并时将不会被修改，保持当前的状态。
        // 3.both modified and the way modified is same, stay it

        // 在分叉点时不存在的文件，且只在当前分支中存在的文件，这些文件保持不变。
        // 4.the current branch added, and not in the given branch

        // 在分叉点时不存在的文件，且只在给定分支中存在的文件，这些文件将被检出并加入暂存区。
        // 5.given branch added, but not in current branch, checkout it, and stage it
        for (String fileName : givenAdded) {
            if (!currentCommit.isTrackedFile(fileName)) {
                checkoutFile(fileName, givenBranchCommitID);
                stageAdd.put(fileName, givenCommit.getFileHash(fileName));
                // saveInfoMaps();
            }
        }
        saveInfoMaps();

        // 在分叉点时存在但未被当前分支修改的文件，在给定分支中缺失的文件，这些文件将被删除，并且不再被跟踪。
        // 6.given branch deleted, and the file is unmodified in current branch,
        // delete it and untracked
        for (String fileName : givenRemoved) {
            if (currentUnModified.contains(fileName)) {
                // this will overwrite the stageAdd funtion
                rm(fileName);
            }
        }

        // 在分叉点时存在但未被给定分支修改的文件，在当前分支中缺失的文件，这些文件将保持缺失状态。
        // 7.given branch unmodified but current branch deleted, stay deleted

        //8.Conflict
        //如果当前分支和给定分支对同一文件做出了不同的修改（即修改方式不同），则发生冲突
        //a. 两个分支对同一个文件内容的修改不同。
        //b. 一个分支修改了文件，而另一个分支删除了该文件。
        //c. 文件在分叉点时不存在，但当前分支和给定分支都有不同的内容。
        Set<String> conflictFiles = new HashSet<>();
        for (String fileName : currentChanged) {
            // 8.a both changed and content is not same
            if (givenChanged.contains(fileName)) {
                if (!givenCommit.getFileHash(fileName).equals(
                        currentCommit.getFileHash(fileName))) {
                    conflictFiles.add(fileName);
                }
            }
            // 8.b changed and other branch removed
            if (givenRemoved.contains(fileName)) {
                conflictFiles.add(fileName);
            }
        }
        for (String fileName : givenChanged) {
            if (currentChanged.contains(fileName)) {
                if (!givenCommit.getFileHash(fileName).equals(
                        currentCommit.getFileHash(fileName))) {
                    conflictFiles.add(fileName);
                }
            }
            if (currentRemoved.contains(fileName)) {
                conflictFiles.add(fileName);
            }
        }
        // 8.3 both added and content is not same.
        for (String fileName : givenAdded) {
            if (currentAdded.contains(fileName)) {
                if (!givenCommit.getFileHash(fileName).equals(
                        currentCommit.getFileHash(fileName))) {
                    conflictFiles.add(fileName);
                }
            }
        }

        // saveInfoMaps();

        // replace the content files content
        replaceConflictsContents(conflictFiles, currentCommit, givenCommit);
        if (!conflictFiles.isEmpty()) {
            System.out.println("Encountered a merge conflict.");
        }
        String message = "Merged " + branchName + " into " + getCurrentBranch() + ".";
        commit(message);
        Commit newCommit = Commit.getCommit(getCurrentCommit());
        newCommit.setOtherParentID(givenBranchCommitID);
        newCommit.saveCommit();
    }


    // ===============================================================
    // This below is the Helper function
    // ===============================================================

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
    private static void getFileStatus(Set<String> modifiedFiles,
                                      Set<String> deletedFiles, Set<String> untrackedFiles) {
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
     * 1. Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * 2. The given branch will now be considered the current branch (HEAD).
     * 3. Any files that are tracked in the current branch but are not present
     * in the checked-out branch are deleted.
     * 4. The staging area is cleared
     * Some spacial cases:
     * If no branch with that name exists, print No such branch exists.
     * If that branch is the current branch, print No need to checkout the current branch.
     * If a working file is untracked in the current branch
     * and would be overwritten by the checkout,
     * print There is an untracked file in the way; delete it,
     * or add and commit it first. and exit;
     */
    private static void checkoutBranch(String branch) {
        getInfoMaps();
        String currentBranch = getCurrentBranch();
        String currentCommitID = getCurrentCommit();
        //Commit currentCommit = Commit.getCommit(currentCommitID);

        if (!branches.containsKey(branch)) {
            MyUtils.exit("No such branch exists.");
        }

        if (currentBranch.equals(branch)) {
            MyUtils.exit("No need to checkout the current branch.");
        }

        // check weather the untracked file would been overwrite
        String checkoutCommitID = branches.get(branch);
        checkUntrackedFile(checkoutCommitID);

        // checkout to identify commitID
        checkoutCommit(currentCommitID, checkoutCommitID);

        // update the breach and commit
        updateHEAD(branch, checkoutCommitID);

        // clear the stageing area
        stageAdd.clear();
        stageRemoval.clear();
        saveInfoMaps();

    }

    /**
     * Takes all files in the given commit, and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * Any files that are tracked in the current commit but are not present
     * in the checked-out branch are deleted.
     */
    private static void checkoutCommit(String currentCommitID, String checkoutCommitID) {
        Commit checkoutCommit = Commit.getCommit(checkoutCommitID);
        Commit currentCommit = Commit.getCommit(currentCommitID);

        for (String fileName : checkoutCommit.getTrackedFilesMap().keySet()) {
            checkoutFile(fileName, checkoutCommitID);
        }

        for (String fileName : currentCommit.getTrackedFilesMap().keySet()) {
            if (!checkoutCommit.isTrackedFile(fileName)) {
                File file = join(CWD, fileName);
                restrictedDelete(file);
            }

        }
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
     * Takes the version of the file as it exists in the commit id
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     */
    private static void checkoutFile(String fileName, String commitID) {

        commitID = getFullCommitID(commitID);

        Commit commit = Commit.getCommit(commitID);

        if (commit == null) {
            MyUtils.exit("No commit with that id exists.");
        }

        // get the file's content in the commit
        File file = join(BLOBS, commit.getFileHash(fileName));
        String content = readContentsAsString(file);

        // rewrite the content
        File workingSpaceFile = join(CWD, fileName);
        // if the content is same no need to rewrite
        if (workingSpaceFile.exists() && commit.isSameFile(fileName, workingSpaceFile)) {
            return;
        }
        MyUtils.createFile(workingSpaceFile);
        writeContents(workingSpaceFile, content);
    }

    /**
     * Get the untracked files.
     * Untracked files : exist in working space but not add and not commit(include the rm files)
     */
    private static Set<String> getUntrackedFiles() {
        List<String> workingSpace = Utils.plainFilenamesIn(CWD);
        Commit commit = Commit.getCommit(getCurrentCommit());
        Set<String> untrackedFiles = new HashSet<>();

        for (String fileName : workingSpace) {
            if (!stageAdd.containsKey(fileName) && !commit.isTrackedFile(fileName)) {
                untrackedFiles.add(fileName);
            }
        }
        return untrackedFiles;
    }

    /**
     * Get the tracked files.
     * The tracked file is the file been added or commited in the working dir
     * The rm will remove the file in working space and add it to stage_removal
     * So tracked file = add + commit - stage_removal's file
     * or tracked file = working space's file which been added or been commited
     */
    private static Set<String> getTrackedFiles() {
        Set<String> trackedFiles = new HashSet<>();
        Commit commit = Commit.getCommit(getCurrentCommit());
        for (String fileName : commit.getTrackedFilesMap().keySet()) {
            trackedFiles.add(fileName);
        }
        for (String fileName : stageAdd.keySet()) {
            trackedFiles.add(fileName);
        }
        for (String fileName : stageRemoval) {
            trackedFiles.remove(fileName);
        }
        return trackedFiles;
    }

    /**
     * Get the full CommitID given a short commitID.
     * If there are not just one exist, just return the first commit id match in the COMMITS folder.
     */
    private static String getFullCommitID(String commitID) {
        int length = commitID.length();
        if (length == 40) {
            return commitID;
        }
        List<String> commitsID = plainFilenamesIn(COMMITS);
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
     * Check untracked files weather can be overwrite, if done so
     * print There is an untracked file in the way; delete it, or add and commit it first.
     */
    private static void checkUntrackedFile(String checkoutCommitID) {
        Set<String> untrackedFiles = getUntrackedFiles();
        Commit checkoutCommit = Commit.getCommit(checkoutCommitID);
        for (String fileName : untrackedFiles) {
            if (checkoutCommit.isTrackedFile(fileName)) {
                // check weather would be overwrite
                File file = join(CWD, fileName);
                if (!checkoutCommit.isSameFile(fileName, file)) {
                    // content is not same, can overwrite
                    MyUtils.exit("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                }
            }
        }
    }


    /**
     * Get Split point commit.
     */
    private static String getSplitPoint(String branchName) {
        String splitPoint = "";
        String currentCommitID = getCurrentCommit();
        Commit currentBranchCommit = Commit.getCommit(currentCommitID);
        Queue<String> currentBranchCommitSet =
                currentBranchCommit.getAllCommitsIDs(currentCommitID);
        String otherBranchCommitID = branches.get(branchName);
        Commit commit = Commit.getCommit(branches.get(branchName));
        Queue<String> otherBranchCommitSet = commit.getAllCommitsIDs(otherBranchCommitID);

        while (!otherBranchCommitSet.isEmpty()) {
            String commitID = otherBranchCommitSet.poll();
            if (currentBranchCommitSet.contains(commitID)) {
                splitPoint = commitID;
                break;
            }
        }


        return splitPoint;
    }

    /**
     * Split the modified files and unmodified files between two commits
     * The modified can be add, rm or changed
     */
    private static void splitModified(Commit splitCommit, Commit commit, Set<String> modified,
                                      Set<String> unModified, Set<String> add,
                                      Set<String> rm, Set<String> change) {

        for (String fileName : commit.getTrackedFilesMap().keySet()) {
            if (splitCommit.isTrackedFile(fileName)) {
                if (splitCommit.getFileHash(fileName).equals(commit.getFileHash(fileName))) {
                    unModified.add(fileName);
                } else {
                    modified.add(fileName); // content is been modified
                    change.add(fileName);
                }
            } else {
                modified.add(fileName);     // file is been added
                add.add(fileName);
            }
        }

        // file is been deleted
        for (String fileName : splitCommit.getTrackedFilesMap().keySet()) {
            if (!commit.isTrackedFile(fileName)) {
                modified.add(fileName);
                rm.add(fileName);
            }
        }

    }

    /**
     * Replace the conflict content files.
     */
    private static void replaceConflictsContents(Set<String> conflicts,
                                                 Commit currentCommit, Commit givenCommit) {
        getInfoMaps();
        for (String fileName : conflicts) {
            String currentContent = getContentOfFile(currentCommit, fileName);
            String givenContent = getContentOfFile(givenCommit, fileName);
            String content = "<<<<<<< HEAD\n" + currentContent
                    + "\n=======\n" + givenContent + ">>>>>>>";
            File file = join(CWD, fileName);
            MyUtils.createFile(file);
            writeContents(file, content);
            String fileHash = sha1(content);
            MyUtils.saveBlobFile(file);
            stageAdd.put(fileName, fileHash);
        }
        saveInfoMaps();

    }

    /**
     * Get the content of file in the commit, if file is deleted, the file is empty.
     */
    private static String getContentOfFile(Commit commit, String fileName) {
        if (commit.isTrackedFile(fileName)) {
            File file = join(BLOBS, commit.getFileHash(fileName));
            return readContentsAsString(file);
        }
        return "";
    }
}
