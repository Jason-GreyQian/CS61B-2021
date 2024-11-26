package bstmap;


import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private class Node {
        K key;
        V value;
        Node left, right;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            left = right = null;
        }
    }

    private Node root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(root, key) != null;
    }

    @Override
    public V get(K key) {
        Node node = get(root, key);
        if (node == null) {     // don't get the node
            return null;
        }
        return node.value;
    }

    private Node get(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node put(Node node, K key, V value) {
        if (node == null) {
            size++;
            return new Node(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else {
            node.value = value;  // update value for the existing key
        }
        return node;
    }

    // printInOrder()
    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node node) {
        if (node != null) {
            printInOrder(node.left);
            System.out.println(node.key + ": " + node.value);
            printInOrder(node.right);
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new LinkedHashSet<>();
        addKey(root, keySet);
        return keySet;
    }

    private void addKey(Node node, Set<K> keys) {
        if (node == null) {
            return;
        }
        addKey(node.left, keys);
        keys.add(node.key);
        addKey(node.right, keys);
    }

    @Override
    public V remove(K key) {
        V returnValue = get(key);
        if (returnValue != null) {
            root = remove(root, key);
            size -= 1;
        }
        return returnValue;
    }

    @Override
    public V remove(K key, V value) {
        V returnValue = get(key);
        if (returnValue == value) {
            root = remove(root, key);
            size -= 1;
        }
        return returnValue;
    }

    private Node remove(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            // key found, now remove it
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            // both children exist, perform Hibbard deletion
            Node minNode = min(node.right);
            node.key = minNode.key;
            node.value = minNode.value;
            node.right = remove(node.right, minNode.key);
        }
        return node;
    }

    private Node min(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    @Override
    public Iterator<K> iterator() {
        Set<K> keySet = keySet();
        return keySet.iterator();
    }
}
