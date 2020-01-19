package de.renew.engine.searchqueue;

import de.renew.engine.searcher.Searchable;
import de.renew.engine.simulator.SimulationThreadPool;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * A deterministic search queue simply provides searchable
 * objects in the same order in which they arrived at the queue.
 * This is useful for debugging purposes.
 *
 * The caller is responsible for excluding concurrent access
 * to the queue.
 *
 * @author Olaf Kummer
 **/
class DeterministicSearchQueue implements SearchQueueData {
    private final double time;
    private DSQListNode list;
    private Hashtable<Searchable, DSQListNode> lookup;
    private int size = 0;

    DeterministicSearchQueue(double time) {
        this.time = time;
        list = null;
        lookup = new Hashtable<Searchable, DSQListNode>();
    }

    public double getTime() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return time;
    }

    public void include(Searchable searchable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        DSQListNode node = lookup.get(searchable);
        if (node == null) {
            size++;
            node = new DSQListNode(searchable);
            lookup.put(searchable, node);
            if (list == null) {
                list = node;
            } else {
                node.prev = list.prev;
                node.next = list;
                list.prev.next = node;
                list.prev = node;
            }
        }
    }

    private void discard(DSQListNode node) {
        size--;
        lookup.remove(node.elem);
        if (node == node.next) {
            // Remove the last entry.
            list = null;
        } else {
            // Remove the list from the cycle.
            node.next.prev = node.prev;
            node.prev.next = node.next;

            // Possibly change the initial node.
            if (list == node) {
                list = node.next;
            }
        }
    }

    public void exclude(Searchable searchable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        DSQListNode node = lookup.get(searchable);
        if (node != null) {
            discard(node);
        }
    }

    public Searchable extract() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Get a single searchable from the list of searchables.
        // It will be checked soon, so it will either turn out
        // to be not enabled, or we must move it to the end
        // of the list to get fairness. Remove it from the list.
        if (list == null) {
            return null;
        } else {
            Searchable result = list.elem;
            discard(list);
            return result;
        }
    }

    public Enumeration<Searchable> elements() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new DSQEnumeration(list);
    }

    public int size() {
        return size;
    }
}