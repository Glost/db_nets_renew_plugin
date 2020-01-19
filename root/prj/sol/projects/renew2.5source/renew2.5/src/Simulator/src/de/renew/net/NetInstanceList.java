/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.net;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.WeakHashMap;


/**
 * The net instance list class keeps track of all well known net instances.
 * Nets are configured to register their instance automatically during
 * creation confirmation.
 *
 * Currently, no special structure is given to this list.
 *
 * Accesses to the net list are synchronized to forbid concurrent updates.
 * No special actions must be taken on the caller side.
 * No deadlocks are possible.
 */
public class NetInstanceList {
    private static WeakHashMap<NetInstance, Object> netInstances = new WeakHashMap<NetInstance, Object>();

    /**
     * This is an entirely static class.
     * No instance creation is allowed.
     */
    private NetInstanceList() {
    }

    /**
     * Register a net instance. If the net instance has already
     * been registered, ignore.
     *
     * @param instance the net instance to be registered.
     */
    public static synchronized void add(NetInstance instance) {
        netInstances.put(instance, null);
    }

    /**
     * Create a snapshot of the list of registered net instances.
     * The net instances are returned in a arbitrary order.
     *
     * @return an array that references all well-known net instances
     */
    public static synchronized NetInstance[] getAll() {
        do {
            try {
                Iterator<NetInstance> iterator = netInstances.keySet().iterator();
                NetInstance[] result = new NetInstance[netInstances.size()];
                int i = 0;

                while (iterator.hasNext()) {
                    NetInstance instance = iterator.next();
                    result[i++] = instance;
                }
                return result;
            } catch (ConcurrentModificationException e) {
                // The net instances have changed due to garbage collection.
                // Rebuild the list.
            }
        } while (true);
    }

    /**
     * Looks for a netInstance with the given Id.
     *
     * @param netInstanceId The Id for the netInstance, for example "agent[123]".
     *
     * @return a NetInstance Object, if this netInstances still exist, "null" if there is (no longer) a NetInstance.
     */
    public static synchronized NetInstance getNetInstance(String netInstanceId) {
        do {
            try {
                for (NetInstance netInstance : netInstances.keySet()) {
                    if (netInstance.getID().equals(netInstanceId)) {
                        return netInstance;
                    }
                }
                return null; // not found
            } catch (ConcurrentModificationException e) {
                // The net instances have changed due to garbage collection.
                // Rebuild the list.
            }
        } while (true);
    }
}