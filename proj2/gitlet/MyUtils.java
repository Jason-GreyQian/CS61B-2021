package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

public class MyUtils {
    /**
     *  Print the error message and exit with code 0.
     */
    public static void exit(String log) {
        System.out.println(log);
        System.exit(0);
    }

    /** Check the opratations weather has the correct operands. */
    public static void validateOperands(String[] args, int operandsNumber) {
        if (args.length != operandsNumber) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Help to create files easyly. */
    public static void createFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Get the file's sha1 hashcode based it's content. */
    public static String getFileHash(File file) {
        byte[] content = readContents(file);
        return sha1(content);
    }

    /** Save the blobs files, and return its hashcode. */
    public static String saveBlobFile(File file) {
        byte[] content = readContents(file);
        String hash = sha1(content);
        File blobFile = join(Repository.BLOBS, hash);
        createFile(blobFile);
        writeContents(file, content);
        return hash;
    }
}
