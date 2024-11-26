package deque;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ArrayDequeTest {
    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct*/
    public void testIsEmptySize() {
        ArrayDeque<String> deque = new ArrayDeque<>();
        assertTrue("A newly initialized ArrayDeque should be empty", deque.isEmpty());

        deque.addFirst("front");

        assertEquals(1, deque.size());
        assertFalse("deque should now contain 1 item", deque.isEmpty());

        deque.addLast("middle");
        assertEquals(2, deque.size());

        deque.addLast("back");
        assertEquals(3, deque.size());

        assertEquals("front middle back", deque.toString());
    }


    @Test
    /** Test add and remove function
     *
     */
    public void testAddRemove() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        assertTrue("A newly initialized ArrayDeque should be empty", deque.isEmpty());

        deque.addFirst(-1);
        assertEquals("-1", deque.toString());

        deque.addLast(0);
        deque.addLast(1);
        assertEquals("-1 0 1", deque.toString());

        int item = deque.removeFirst();
        assertEquals(-1, item);
        assertEquals("0 1", deque.toString());

        item = deque.removeLast();
        assertEquals(1, item);
        assertEquals("0", deque.toString());
    }

    @Test
    /** Tests removing from an empty deque */
    public void testRemoveEmpty() {
        ArrayDeque<String> deque = new ArrayDeque<>();
        deque.addFirst("Grey");
        deque.addLast("Qian");

        deque.removeLast();
        deque.removeFirst();
        deque.removeLast();
        deque.removeFirst();

        assertEquals(0, deque.size());
    }


    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void testMultipleParam() {
        ArrayDeque<String> lld1 = new ArrayDeque<String>();
        ArrayDeque<Double> lld2 = new ArrayDeque<Double>();

        ArrayDeque<Boolean> lld3 = new ArrayDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());


    }

    @Test
    /** Test weather can shrink the size. */
    public void testShrinkSize() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        // 8 -> 16 -> 32 -> 64 -> 128
        for (int i = 0; i < 100; i++) {
            deque.addLast(i);
        }
        for (int i = 0; i < 85; i++) {
            int item = deque.removeFirst();
            assertEquals(i, item);
        }
        assertEquals(15, deque.size());
        // 128 -> 64( > 4 * 15) -> 32
        assertEquals(32, deque.getCapacity());

        for (int i = 99; i > 85; i--) {
            int item = deque.removeLast();
            assertEquals(i, item);
        }
        assertEquals(1, deque.size());
        // only >= 16 can shrink, if less than 16 ,do not shrink any more.
        assertEquals(8, deque.getCapacity());
        assertEquals("85", deque.toString());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }


    }
}
