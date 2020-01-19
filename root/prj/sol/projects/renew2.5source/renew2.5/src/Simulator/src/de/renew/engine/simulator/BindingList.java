package de.renew.engine.simulator;

import java.util.HashSet;
import java.util.Set;


/**
 * The binding list class keeps track of all currently firing bindings.
 * All bindings are automatically registered at this list. This is no
 * problem with respect to garbage collection, because active bindings
 * are references by a thread anyway.
 *
 * Currently, no special structure is given to this list.
 *
 * Accesses to the net list are sYnchronized to forbid concurrent updates.
 * No special actions must be taken on the caller side.
 * No deadlocks are possible.
 */
public class BindingList {

    /**
     * This set contains all currently active Bindings.
     */
    private static Set<Binding> bindings = new HashSet<Binding>();

    /**
     * A lock object is used to trigger the waiting threads.
     */
    private static Object LOCK = new Object();

    /**
     * This is an entirely static class.
     * No instance creation is allowed.
     */
    private BindingList() {
    }

    /**
     * Register a binding. If the binding has already
     * been registered, ignore.
     *
     * @param binding the binding to be registered.
     */
    public static void register(Binding binding) {
        synchronized (LOCK) {
            bindings.add(binding);
        }
    }

    /**
     * Remove a binding from the list. If the binding is not
     * contained in the list, ignore.
     *
     * @param binding the binding to be removed.
     */
    public static void remove(Binding binding) {
        synchronized (LOCK) {
            bindings.remove(binding);
            if (bindings.isEmpty()) {
                LOCK.notifyAll();
            }
        }
    }

    /**
     * Create a snapshot of the list of bindings.
     * The bindings are returned in a arbitrary order.
     *
     * @return an array that references all well-known net instances
     */
    public static Binding[] getAll() {
        synchronized (LOCK) {
            return bindings.toArray(new Binding[bindings.size()]);
        }
    }

    /**
     * Wait until all firing transitions have finished.
     *
     * Make sure that no simulator is running while this method is called,
     * otherwise the simulator could start another transition before
     * the result of this method can be processed.
     */
    public static void waitUntilEmpty() {
        synchronized (LOCK) {
            while (!bindings.isEmpty()) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    // This is expected.
                }
            }
        }
    }
}