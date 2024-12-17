package hashmap;

import java.util.*;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
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
    private static final int DEFAULT_CAPACITY = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;
    private static final int REFACTOR = 2;
    private int size;
    private int tableSize;
    private double loadFactor;

    /**
     * Constructors
     */
    public MyHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        loadFactor = maxLoad;
        tableSize = initialSize;
        size = 0;
        buckets = createTable(tableSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        for (int i = 0; i < tableSize; i++) {
            buckets[i].clear();
        }
        size = 0;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key
     */
    @Override
    public boolean containsKey(K key) {
        Node node = getNode(key);
        return node != null;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     *
     * @param key
     */
    @Override
    public V get(K key) {
        Node node = getNode(key);
        if (node != null) {
            return node.value;
        }
        return null;
    }

    private Node getNode(K key) {
        int bucketIndex = calBucketNumbers(key);
        for (Node node : buckets[bucketIndex]) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
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
        Node node = getNode(key);
        if (node != null) {
            node.value = value;
            return;
        } else {
            size += 1;
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
        Set<K> keySet = new HashSet<>();
        for (int i = 0; i < tableSize; i++) {
            for (Node node : buckets[i]) {
                keySet.add(node.key);
            }
        }
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
        Node node = getNode(key);
        if (node == null) {
            return null;
        }
        size -= 1;
        int bucketIndex = calBucketNumbers(key);
        buckets[bucketIndex].remove(node);
        return node.value;
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
        Node node = getNode(key);
        if (node != null || !node.value.equals(value)) {
            return null;
        }
        size -= 1;
        int bucketIndex = calBucketNumbers(key);
        buckets[bucketIndex].remove(node);
        return node.value;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashIterator();
    }

    private class MyHashIterator implements Iterator<K> {
        private int bucketIndex;
        private int iterateNums;
        private Iterator<Node> iteratorTrack;

        public MyHashIterator() {
            bucketIndex = 0;
            iterateNums = 0;
            iteratorTrack = buckets[bucketIndex].iterator();
        }

        @Override
        public boolean hasNext() {
            return iterateNums < size;
        }

        @Override
        public K next() {
            iterateNums += 1;
            K key = null;
            while (bucketIndex < tableSize - 1) {
                if (iteratorTrack.hasNext()) {
                    key = iteratorTrack.next().key;
                    break;
                } else {
                    iteratorTrack = buckets[bucketIndex + 1].iterator();
                    bucketIndex += 1;
                }
            }
            return key;
        }
    }

    /**
     * Calculate the bucket index based the key.
     */
    private int calBucketNumbers(K key) {
        int hashCode = key.hashCode();
        return Math.floorMod(hashCode, tableSize);
    }

    /**
     * if overload should resize the bucket.
     */
    private void increaseBucketSize() {
        // init the new bucket
        tableSize *= REFACTOR;
        Collection<Node>[] newBuckets = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
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

    /**
     * Check weather overloads.
     */
    private boolean isOverload() {
        double loadFactorNow = 1.0 * size / tableSize;
        return loadFactorNow > loadFactor;
    }


}
