package hashmap;

import static org.junit.Assert.*;

import jh61b.junit.In;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Tests of optional parts of lab 8. */
public class TestMyHashMapExtra {

    @Test
    public void testRemove() {
        MyHashMap<String, String> q = new MyHashMap<>();
        q.put("c", "a");
        q.put("b", "a");
        q.put("a", "a");
        q.put("d", "a");
        q.put("e", "a"); // a b c d e
        assertTrue(null != q.remove("c"));
        assertFalse(q.containsKey("c"));
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("d"));
        assertTrue(q.containsKey("e"));
    }

    /** 
     * Remove Test 2
     * Test the 3 different cases of remove
     */
    @Test
    public void testRemoveThreeCases() {
        MyHashMap<String, String> q = new MyHashMap<>();
        q.put("c", "a");
        q.put("b", "a");
        q.put("a", "a");
        q.put("d", "a");
        q.put("e", "a");                         // a b c d e
        assertTrue(null != q.remove("e"));      // a b c d
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("c"));
        assertTrue(q.containsKey("d"));
        assertTrue(null != q.remove("c"));      // a b d
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("d"));
        q.put("f", "a");                         // a b d f
        assertTrue(null != q.remove("d"));      // a b f
        assertTrue(q.containsKey("a"));
        assertTrue(q.containsKey("b"));
        assertTrue(q.containsKey("f"));
    }

    @Test
    public void testKeySet() {
        MyHashMap<String, String> q = new MyHashMap<>();
        q.put("c", "a");
        q.put("b", "a");
        q.put("a", "a");
        q.put("d", "a");
        q.put("e", "a");

        Set<String> keys = q.keySet();
        assertTrue(null != keys);
        assertTrue(keys.contains("c"));
        assertTrue(keys.contains("b"));
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("d"));
        assertTrue(keys.contains("e"));
        assertFalse(keys.contains("f"));
    }

    @Test
    public void testIterator() {
        MyHashMap<String, String> q = new MyHashMap<>();
        q.put("c", "a");
        q.put("b", "a");
        q.put("a", "a");

        Iterator<String> keyIterator = q.iterator();
        Set<String> set = new HashSet<>();
        while (keyIterator.hasNext()) {
            set.add(keyIterator.next());
        }
        assertTrue(null != set);
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
        assertFalse(set.contains("d"));
    }

    @Test
    public void testIteratorForLargeSet() {
        MyHashMap<Integer, Integer> q = new MyHashMap<>();
        for (int i = 0; i < 455; i++) {
            q.put(i, i);
        }

        Iterator<Integer> keyIterator = q.iterator();
        Set<Integer> set = new HashSet<>();
        while (keyIterator.hasNext()) {
            set.add(keyIterator.next());
        }
        assertTrue(null != set);
        for (int i = 0; i < 455; i++) {
            assertTrue(set.contains(i));
        }
    }
}
