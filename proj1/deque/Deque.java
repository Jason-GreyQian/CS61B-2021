package deque;

import java.util.Iterator;

// 接口中的方法默认为public，不需要使用public来做修饰符。
public interface Deque<T> {
    /**
     * Add items to the front place.
     */
    void addFirst(T item);

    /**
     * Add an item to  the back of the deque.
     */
    void addLast(T item);

    /**
     * Returns true if deque is empty, false otherwise.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns how much items the deque has.
     */
    int size();

    /**
     * Print the items of the deque , separate by space , when finish print, print a newline.
     * Test need not use default.
     */
    void printDeque();

    /**
     * Removes and returns the item at the front of the deque.If no such item exists, returns null.
     */
    T removeFirst();

    /**
     * Removes and returns the item at the back of the deque.If no such item exists, returns null.
     */
    T removeLast();

    /**
     * Gets the item at the given index,  If no such item exists, returns null.
     */
    T get(int index);

    /**
     * Create a iterator.
     */
    Iterator<T> iterator();

    /**
     * Returns whether or not the parameter o is equal to the Deque.
     * iff they are the same object or they contain the same contents.
     */
    boolean equals(Object o);

}
