package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import net.sf.saxon.functions.ConstantFunction;
import org.junit.Test;

import javax.sound.midi.Soundbank;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> alist = new AListNoResizing<Integer>();
        BuggyAList<Integer> buggyAList = new BuggyAList<Integer>();

        // add 4, 5, 6
        alist.addLast(4);
        buggyAList.addLast(4);
        alist.addLast(5);
        buggyAList.addLast(5);
        alist.addLast(6);
        buggyAList.addLast(6);

        //check weather removeLast has same answer
        while (alist.size() > 0) {
            assertEquals(alist.size(), buggyAList.size());
            int expect = alist.removeLast();
            int actual = buggyAList.removeLast();
            assertEquals(expect, actual);
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<Integer>();
        BuggyAList<Integer> buggy = new BuggyAList<Integer>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                buggy.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int expectSize = correct.size();
                int actualSize = buggy.size();
                assertEquals(expectSize, actualSize);
            } else if (correct.size() == 0) {
                // size == 0 can't removeLast and getLast
                assertEquals(correct.size(), buggy.size());
                continue;
            } else if (operationNumber == 2) {
                // getLast
                int expectLastVal = correct.getLast();
                int actualLastVal = buggy.getLast();
                assertEquals(expectLastVal, actualLastVal);
            } else if (operationNumber == 3) {
                // removeLast
                int expectLastVal = correct.removeLast();
                int actualLastVal = buggy.removeLast();
            }
        }
    }
}
