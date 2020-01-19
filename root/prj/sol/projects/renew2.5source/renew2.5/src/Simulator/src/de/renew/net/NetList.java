/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.net;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.WeakHashMap;


/**
 * The net list class keeps track of all well known nets.
 *
 * Accesses to the net list are synchronized to forbid concurrent updates.
 * No special actions must be taken on the caller side.
 * No deadlocks are possible.
 */
public class NetList {
    private static WeakHashMap<Net, Object> nets = new WeakHashMap<Net, Object>();

    /**
     * This is an entirely static class.
     * No instance creation is allowed.
     */
    private NetList() {
    }

    /**
     * Register a net. If the net has already been registered, ignore.
     *
     * @param net the net to be registered.
     */
    public static synchronized void add(Net net) {
        nets.put(net, null);
    }

    /**
     * Create a snapshot of the list of registered net.
     * The nets are returned in a arbitrary order.
     *
     * @return an array that references all well-known nets
     */
    public static synchronized Net[] getAll() {
        do {
            try {
                Iterator<Net> iterator = nets.keySet().iterator();
                Net[] result = new Net[nets.size()];
                int i = 0;

                while (iterator.hasNext()) {
                    Net net = iterator.next();
                    result[i++] = net;
                }
                return result;
            } catch (ConcurrentModificationException e) {
                // The nets have changed due to garbage collection.
                // Rebuild the list.
            }
        } while (true);
    }
}