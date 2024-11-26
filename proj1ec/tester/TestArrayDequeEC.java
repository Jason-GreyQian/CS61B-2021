package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void randomTest() {
        StudentArrayDeque<Integer> buggy = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> correct = new ArrayDequeSolution<>();

        int N = 1000;
        String log = "";
        for (int i = 0; i < N; i++) {
            int optionNumber = StdRandom.uniform(6);
            if (optionNumber == 0) {
                log += "addLast(" + i + ")\n";
                buggy.addLast(i);
                correct.addLast(i);
            } else if (optionNumber == 1) {
                log += "addFirst(" + i + ")\n";
                buggy.addFirst(i);
                correct.addFirst(i);
            } else if (optionNumber == 2) {
                log += "size()\n";
                int actualSize = buggy.size();
                int expectedSize = correct.size();
                assertEquals(log, expectedSize, actualSize);
            } else if (buggy.size() == 0 || correct.size() == 0) {
                // empty list don't remove and get
            } else if (optionNumber == 3) {
                log += "removeFirst()\n";
                Integer actualFirstItem = buggy.removeFirst();
                Integer expectedFirstItem = correct.removeFirst();
                assertEquals(log, expectedFirstItem, actualFirstItem);
            } else if (optionNumber == 4) {
                log += "removeLast()\n";
                Integer actualLastItem = buggy.removeLast();
                Integer expectedLastItem = correct.removeLast();
                assertEquals(log, expectedLastItem, actualLastItem);
            } else if (optionNumber == 5) {
                int randomIndex = StdRandom.uniform(correct.size());
                log += "get(" + randomIndex + ")\n";
                Integer actualItem = correct.get(randomIndex);
                Integer expectedItem = buggy.get(randomIndex);
                assertEquals(log, expectedItem, actualItem);
            }
        }
    }
}
