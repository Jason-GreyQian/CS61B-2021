package flik;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestFilk {
    @Test
    public void testSmallNumber() {
        for (int i = 0; i < 128; i++) {
            assertTrue(Flik.isSameNumber(i, i));
        }
    }

    @Test
    public void testBigNumber() {
        for (int i = 128; i < 1000; i++) {
            assertTrue(Flik.isSameNumber(i, i));
        }
    }
}
