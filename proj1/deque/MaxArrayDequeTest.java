package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MaxArrayDequeTest {
    private class IntegerComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(o1, o2);
        }
    }

    private class StringLengthComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return Integer.compare(o1.length(), o2.length());
        }
    }

    private class AsciiValueComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    @Test
    public void testMaxWithAbsoluteValueComparator() {
        Comparator<Integer> intComparator = new IntegerComparator();
        MaxArrayDeque<Integer> deque = new MaxArrayDeque<>(intComparator);

        deque.addLast(-10);
        deque.addLast(5);
        deque.addLast(3);

        assertEquals("Max value is 5", 5, (int) deque.max());
    }

    @Test
    public void testMaxWithStringLengthComparator() {
        Comparator<String> lengthComparator = new StringLengthComparator();
        MaxArrayDeque<String> deque = new MaxArrayDeque<>(lengthComparator);

        deque.addLast("apple");
        deque.addLast("banana");
        deque.addLast("pear");

        assertEquals("Max length is banana", "banana", deque.max());
    }

    @Test
    public void testMaxWithCustomComparatorWhenEmpty() {
        Comparator<Integer> integerComparator = new IntegerComparator();
        MaxArrayDeque<Integer> deque = new MaxArrayDeque<>(integerComparator);

        assertNull(deque.max());
    }

    @Test
    public void testMaxWithSingleElementUsingCustomComparator() {
        Comparator<String> lengthComparator = new StringLengthComparator();
        MaxArrayDeque<String> deque = new MaxArrayDeque<>(lengthComparator);

        deque.addLast("singleton");

        assertEquals("singleton", deque.max());
    }

    @Test
    public void testMaxWithAsciiValueComparator() {
        Comparator<String> stringComparator = new AsciiValueComparator();
        MaxArrayDeque<String> deque = new MaxArrayDeque<>(stringComparator);

        deque.addLast("singleton");
        deque.addFirst("apple");
        deque.addFirst("zoom");
        deque.addLast("grey");

        assertEquals("zoom", deque.max());
    }

}
