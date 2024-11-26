package deque;

import java.util.Iterator;
import java.util.StringJoiner;

public class LinkedListDeque<T> implements Iterable<T>, deque.Deque<T> {
    private class Node {
        // items to restore value
        T item;
        // front and back pointer
        Node prev;
        Node next;

        /* 私有类的构造函数不需要使用public */
        Node(T value) {
            item = value;
            prev = null;
            next = null;
        }

        Node(T value, Node p, Node n) {
            item = value;
            prev = p;
            next = n;
        }
    }

    // private class for creating iterator
    private class LinkListDequeIterator implements Iterator<T> {
        private Node positionNode;

        LinkListDequeIterator() {
            positionNode = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return positionNode != sentinel;
        }

        @Override
        public T next() {
            T value = positionNode.item;
            positionNode = positionNode.next;
            return value;
        }
    }

    private int size;
    /**
     * sentinel.prev point the last node
     * sentinel.next point the first node
     */
    private Node sentinel;

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node(null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    /**
     * Add items to the front place.
     */
    @Override
    public void addFirst(T item) {
        size += 1;
        Node firstNode = new Node(item, sentinel, sentinel.next);
        sentinel.next.prev = firstNode;
        sentinel.next = firstNode;
    }

    /**
     * Add an item to  the back of the deque.
     */
    @Override
    public void addLast(T item) {
        size += 1;
        Node node = new Node(item, sentinel.prev, sentinel);
        sentinel.prev.next = node;
        sentinel.prev = node;
    }

    /**
     * Returns how much items the deque has.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Removes and returns the item at the front of the deque.If no such item exists, returns null.
     */
    @Override
    public T removeFirst() {
        if (size() == 0) {
            return null;
        }
        size -= 1;
        Node firstNode = sentinel.next;
        sentinel.next = firstNode.next;
        firstNode.next.prev = sentinel;
        return firstNode.item;
    }

    /**
     * Removes and returns the item at the back of the deque.If no such item exists, returns null.
     */
    @Override
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        size -= 1;
        Node lastNode = sentinel.prev;
        sentinel.prev = lastNode.prev;
        lastNode.prev.next = sentinel;
        return lastNode.item;
    }

    /**
     * Gets the item at the given index,  If no such item exists, returns null.
     */
    @Override
    public T get(int index) {
        if (index >= 0 && index < size()) {
            Node node = sentinel;
            while (index >= 0) {
                node = node.next;
                index -= 1;
            }
            return node.item;
        }
        return null;
    }

    /**
     * Get the item at the given index, If no such item exists, returns null. Use recursive
     */
    public T getRecursive(int index) {
        if (index >= 0 && index < size()) {
            return getRecursiveHelper(sentinel.next, index);
        }
        return null;
    }

    /**
     * Helper function to help recursively get the index of item.
     */
    private T getRecursiveHelper(Node node, int index) {
        if (index == 0) {
            return node.item;
        }
        return getRecursiveHelper(node.next, index - 1);
    }

    /**
     * Create a iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return new LinkListDequeIterator();
    }

    /**
     * Returns weather or not the parameter o is equal to the Deque.
     * iff they are the same object or they contain the same contents.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Deque) {
            Deque<T> other = (Deque<T>) o;
            if (this.size() != other.size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i++) {
                if (!this.get(i).equals(other.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;

    }


    /**
     * Override the method toString.
     */
    public String toString() {
        StringJoiner joiner = new StringJoiner(" ", "", "");
        Node node = sentinel.next;
        while (node != sentinel) {
            joiner.add(node.item.toString());
            node = node.next;
        }
        return joiner.toString();
    }

    @Override
    public void printDeque() {
        System.out.println(this);
    }
}
