package IntList;

import static org.junit.Assert.*;

import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    /**
     * Test the prime which has no primes
     */
    @Test
    public void testSquarePrimesNoPrimes() {
        IntList lst = IntList.of(4, 6, 8, 10);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 6 -> 8 -> 10", lst.toString());
        assertFalse(changed);
    }

    /**
     * Test 2 weather is primes
     */
    @Test
    public void testSquarePrimesIncludeTwo() {
        IntList lst = IntList.of(2, 3, 6, 7, 10);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 9 -> 6 -> 49 -> 10", lst.toString());
        assertTrue(changed);
    }

    /**
     * Test the Intlist which all is prime.
     */
    @Test
    public void testSquarePrimesAllIsPrime() {
        IntList lst = IntList.of(2, 3, 5, 7);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 9 -> 25 -> 49", lst.toString());
        assertTrue(changed);
    }
}
