package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * An IDRegistry assigns persistent IDs to objects and counts
 * references to the IDs. IDs are discarded when reference counter
 * reaches zero.
 *
 * <p>
 * Since reference counting is not only used during simulation but
 * also during garbage collection, this class does not assume that
 * all methods are called within simulation threads. However,
 * serialization and de-serialization must occur within simulation
 * threads.
 * </p>
 *
 * @author Kummer
 *
 */
public class IDRegistry implements java.io.Serializable {

    /** The single well-known instance of the IDRegistry. */
    private static IDRegistry _instance = null;
    private transient Map<Object, Object> table;

    public IDRegistry() {
        table = new HashMap<Object, Object>();
    }

    /**
     * Get the single well-known instance of the IDRegistry class.
     * Although other instances are possible, this instance is
     * specifically created to be used whenever a local
     * IDRegistry is not available.
     */
    public static synchronized IDRegistry getInstance() {
        if (_instance == null) {
            _instance = new IDRegistry();
        }
        return _instance;
    }

    // Needn't be synchronized, because only called from
    // synchronized methods.
    private IDCounter register(Object elem) {
        IDCounter counter = (IDCounter) table.get(elem);
        if (counter == null) {
            counter = new IDCounter(IDSource.createID());
            table.put(elem, counter);
        }
        return counter;
    }

    public synchronized String getID(Object elem) {
        return register(elem).getID();
    }

    public synchronized void reserve(Object elem) {
        register(elem).reserve();
    }

    /**
     * Reduce the reservation count by 1.
     * If the reservation count reaches 0, the current ID
     * of the object is discarded.
     *
     * @param elem the object to be unreserved.
     */
    public synchronized void unreserve(Object elem) {
        IDCounter counter = register(elem);
        counter.unreserve();
        if (counter.isDiscardable()) {
            table.remove(elem);
        }
    }

    public synchronized void setAndReserveID(Object elem, String id) {
        IDCounter counter = (IDCounter) table.get(elem);
        if (counter == null) {
            counter = new IDCounter(id);
            table.put(elem, counter);
        } else if (!counter.getID().equals(id)) {
            throw new RuntimeException("Token already in use.");
        }
        counter.reserve();
    }

    private synchronized void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        in.defaultReadObject();
        table = new HashMap<Object, Object>();
        boolean finished = false;
        do {
            Object key = in.readObject();
            if (key == null) {
                finished = true;
            } else {
                Object counter = in.readObject();
                table.put(key, counter);
            }
        } while (!finished);
    }

    private synchronized void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        RenewObjectOutputStream rOut = null;
        if (out instanceof RenewObjectOutputStream) {
            rOut = (RenewObjectOutputStream) out;
        }
        if (rOut != null) {
            rOut.beginDomain(this);
        }
        out.defaultWriteObject();
        Iterator<Entry<Object, Object>> iterator = table.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, Object> entry = iterator.next();
            Object key = entry.getKey();
            Object counter = entry.getValue();
            out.writeObject(key);
            out.writeObject(counter);
        }
        out.writeObject(null);
        if (rOut != null) {
            rOut.endDomain(this);
        }
    }

    public synchronized static void reset() {
        _instance = null;
    }

    public synchronized static void save(java.io.ObjectOutput out)
            throws IOException {
        out.writeObject(_instance);
    }

    public synchronized static void load(java.io.ObjectInput in)
            throws IOException, ClassNotFoundException {
        _instance = (IDRegistry) in.readObject();
    }
}