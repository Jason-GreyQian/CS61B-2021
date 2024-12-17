package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;
    private int bucketSize;
    private double loadFactor;
    private int REFACTOR = 2;
    private Set<K> keySet;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        loadFactor = maxLoad;
        bucketSize = initialSize;
        keySet = new HashSet<K>();
        buckets = new Collection[bucketSize];
        size = 0;
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return null;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i].clear();
        }
        size = 0;
        keySet.clear();
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key
     */
    @Override
    public boolean containsKey(K key) {
        int bucketIndex = calBucketNumbers(key);
        boolean contains = false;
        for (Node node : buckets[bucketIndex]) {
            if (node.key.equals(key)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     *
     * @param key
     */
    @Override
    public V get(K key) {
        int bucketIndex = calBucketNumbers(key);
        V value = null;
        for (Node node : buckets[bucketIndex]) {
            if (node.key.equals(key)) {
                value = node.value;
                break;
            }
        }
        return value;
    }

    /**
     * Returns the number of key-value mappings in this map.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     *
     * @param key
     * @param value
     */
    @Override
    public void put(K key, V value) {
        int bucketIndex = calBucketNumbers(key);
        if (containsKey(key)) {
            for (Node node : buckets[bucketIndex]) {
                if (node.key.equals(key)) {
                    node.value = value;
                }
            }
        } else {
            size += 1;
            keySet.add(key);
            buckets[bucketIndex].add(createNode(key, value));
        }

        if (isOverload()) {
            increaseBucketSize();
        }
    }

    /**
     * Returns a Set view of the keys contained in this map.
     */
    @Override
    public Set<K> keySet() {
        return keySet;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     *
     * @param key
     */
    @Override
    public V remove(K key) {
       if (!containsKey(key)) {
           return null;
       } else {
           size -= 1;
           int bucketIndex = calBucketNumbers(key);
           V value = null;
           for (Node node : buckets[bucketIndex]) {
               if (node.key.equals(key)) {
                   value = node.value;
                   buckets[bucketIndex].remove(node);
                   keySet.remove(key);
                   break;
               }
           }
           return value;
       }
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     *
     * @param key
     * @param value
     */
    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        } else {
            int bucketIndex = calBucketNumbers(key);
            for (Node node : buckets[bucketIndex]) {
                if (node.key.equals(key)) {
                    if (node.value == value) {
                        buckets[bucketIndex].remove(node);
                        size -= 1;
                        keySet.remove(key);
                        return value;
                    } else {
                        break;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    /** Calculate the bucket index based the key. */
    private int calBucketNumbers(K key) {
        int hashCode = key.hashCode();
        return Math.floorMod(hashCode, bucketSize);
    }

    /** if overload should resize the bucket. */
    private void increaseBucketSize() {
        // init the new bucket
        bucketSize *= REFACTOR;
        Collection<Node>[] newBuckets = new Collection[bucketSize];
        for (int i = 0; i < bucketSize; i++) {
            newBuckets[i] = createBucket();
        }

        // move the node to new bucket
        for (int i = 0; i < buckets.length; i++) {
            for (Node node : buckets[i]) {
                int newBucketIndex = calBucketNumbers(node.key);
                newBuckets[newBucketIndex].add(node);
            }
        }

        buckets = newBuckets;
    }

    /** Check weather overloads. */
    private boolean isOverload() {
        double loadFactorNow = 1.0 * size / bucketSize;
        return loadFactorNow > loadFactor;
    }


}
