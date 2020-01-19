package de.renew.util;



/**
 * A random bag is a simple data structure that can take
 * an arbitrary number of objects and return them in a random order.
 *
 * The implementation is based on an array. During the
 * extraction, a random element is removed and the
 * void position is filled in with the very last element.
 *
 * Each operation runs in constant amortized time.
 **/
public class RandomBag {
    private Object[] elements;
    private int size;

    public RandomBag() {
        elements = new Object[8];
        size = 0;
    }

    public int size() {
        return size;
    }

    private void setCapacity(int capacity) {
        Object[] newElements = new Object[capacity];
        System.arraycopy(elements, 0, newElements, 0, size);
        elements = newElements;
    }

    private void ensureCapacity(int capacity) {
        if (capacity > elements.length) {
            // Ensure that the capacity is at least doubled
            // each time.
            if (capacity < 2 * elements.length) {
                capacity = 2 * elements.length;
            }
            setCapacity(capacity);
        }
    }

    private void limitCapacity() {
        // Ensure that the capacity can be at least halved
        // each time. Also, there must remain enough elements
        // that no enlargement is required in the near future.
        // It makes no sense to resize very small array,
        // because the creation of a new array is very expensive.
        if (size >= 8 && size * 4 < elements.length) {
            setCapacity(size * 2);
        }
    }

    public void insert(Object elem) {
        ensureCapacity(size + 1);
        elements[size++] = elem;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Object extract() {
        if (isEmpty()) {
            return null;
        }
        Object elem = elements[size - 1];


        // Null the field to allow garbage collection.
        elements[size - 1] = null;
        size = size - 1;
        limitCapacity();

        return elem;
    }
}