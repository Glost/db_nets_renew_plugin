package de.renew.engine.searchqueue;

import de.renew.engine.searcher.Searchable;
import de.renew.engine.simulator.SimulationThreadPool;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * A random search queue returns possibly enabled searchable
 * objects in a random order. This is useful for running many
 * simulations that explore the possible behaviour of a net.
 *
 * The implementation is based on an array. During the
 * extraction, a random element is removed and the
 * void position is filled in with the very last element.
 *
 * @author Olaf Kummer
 **/
class RandomSearchQueue implements SearchQueueData {
    private final double time;
    private RandomQueueNode[] elements;
    private int size;
    private Hashtable<Searchable, RandomQueueNode> lookup;

    RandomSearchQueue(double time) {
        this.time = time;
        elements = new RandomQueueNode[8];
        lookup = new Hashtable<Searchable, RandomQueueNode>();
        size = 0;
    }

    public double getTime() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return time;
    }

    private void setCapacity(int capacity) {
        RandomQueueNode[] newElements = new RandomQueueNode[capacity];
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
        // It makes no sense to resize very small arrays,
        // because the creation of a new array is very expensive.
        if (size >= 8 && size * 4 < elements.length) {
            setCapacity(size * 2);
        }
    }

    public void include(Searchable searchable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (!lookup.containsKey(searchable)) {
            ensureCapacity(size + 1);
            RandomQueueNode node = new RandomQueueNode(size, searchable);
            elements[size] = node;
            size++;
            lookup.put(searchable, node);
        }
    }

    private void discard(RandomQueueNode node) {
        // Pull in last element.
        int pos = node.pos;
        elements[pos] = elements[size - 1];
        elements[pos].pos = pos;


        // Null the field to allow garbage collection.
        elements[size - 1] = null;
        size--;
        limitCapacity();

        lookup.remove(node.searchable);
    }

    public void exclude(Searchable searchable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        RandomQueueNode node = lookup.get(searchable);
        if (node != null) {
            discard(node);
        }
    }

    public Searchable extract() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (size == 0) {
            return null;
        }

        // Choose a random element.
        int pos = (int) (Math.random() * size);

        // Guard against rounding errors.
        if (pos >= size) {
            pos = size - 1;
        }

        // Remember element.
        RandomQueueNode node = elements[pos];


        // Discard element from data structure.
        discard(node);

        return node.searchable;
    }

    public Enumeration<Searchable> elements() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return lookup.keys();
    }

    public int size() {
        return size;
    }
}