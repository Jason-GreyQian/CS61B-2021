package deque;

import java.util.Iterator;
import java.util.StringJoiner;

public class ArrayDeque<T> implements deque.Deque<T>, Iterable<T> {
    /**
     * Class for create iterator.
     */
    private class ArrayDequeIterator implements Iterator<T> {
        private int pos;

        ArrayDequeIterator() {
            pos = begin + 1;
        }

        @Override
        public boolean hasNext() {
            return pos < end;
        }

        @Override
        public T next() {
            int idx = posCalculation(pos, capacity);
            T item = array[idx];
            pos += 1;
            return item;
        }
    }

    private int capacity;
    private int size;
    private int begin;
    private int end;
    private T[] array;
    private double increaseFactor = 2.0;
    private double decreaseFactor = 0.5;

    public ArrayDeque() {
        // The starting size of array should be 8.
        capacity = 8;
        size = 0;
        array = (T[]) new Object[capacity];
        begin = -1;     // point the first item that should insert
        end = 0;        // point the last item that should insert
    }

    /**
     * Resize the ArrayDeque .
     */
    private void resize(double factor) {
        int newCapacity = (int) (capacity * factor);
        T[] newArray = (T[]) new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            int idx = posCalculation(begin + i + 1, capacity);
            newArray[i] = array[idx];
        }
        begin = -1;
        end = size;
        capacity = newCapacity;
        array = newArray;

    }

    /**
     * Calculate position
     */
    private int posCalculation(int offset, int tmpCapacity) {
        return Math.floorMod(offset, tmpCapacity);
    }

    /**
     * Add items to the front place.
     */
    @Override
    public void addFirst(T item) {
        if (size >= capacity) {
            resize(increaseFactor);
        }
        size += 1;
        int idx = posCalculation(begin, capacity);
        array[idx] = item;
        begin -= 1;
    }

    /**
     * Add an item to  the back of the deque.
     */
    @Override
    public void addLast(T item) {
        if (size >= capacity) {
            resize(increaseFactor);
        }
        size += 1;
        int idx = posCalculation(end, capacity);
        array[idx] = item;
        end += 1;
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
        if (size == 0) {
            return null;
        }
        size -= 1;
        int firstIdx = posCalculation(begin + 1, capacity);
        T item = array[firstIdx];
        array[firstIdx] = null;
        begin += 1;
        if (shouldShrink()) {
            resize(decreaseFactor);
        }
        return item;
    }

    /**
     * Judge weather should shrink array.
     */
    private boolean shouldShrink() {
        return capacity >= 16 && 4 * size < capacity;
    }

    /**
     * Removes and returns the item at the back of the deque.If no such item exists, returns null.
     */
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        size -= 1;
        int lastIdx = posCalculation(end - 1, capacity);
        T item = array[lastIdx];
        array[lastIdx] = null;
        end -= 1;
        if (shouldShrink()) {
            resize(decreaseFactor);
        }
        return item;
    }

    @Override
    public T get(int index) {
        int idx = posCalculation(index + begin + 1, capacity);
        return array[idx];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    /**
     * Rewrite toString Function.
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" ", "", "");
        for (int i = 0; i < size(); i++) {
            joiner.add(get(i).toString());
        }
        return joiner.toString();
    }

    /**
     * Get capacity, just for test
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns whether or not the parameter o is equal to the Deque.
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

    @Override
    public void printDeque() {
        System.out.println(this);
    }
}
