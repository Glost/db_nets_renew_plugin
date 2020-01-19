package de.renew.engine.searchqueue;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;


/**
 * The class search queue keeps track of the transitions
 * instances that must be searched for activated bindings.
 * This is done statically, so that a net instance
 * can register its transitions as possibly activated even if it
 * does not know of any specific searcher.
 * A simulator can ask the search queue for
 * possibly activated transitions.
 *
 * @author Olaf Kummer
 **/
public class SearchQueue {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SearchQueue.class);

    // This field designates the time instance when
    // the most recently extracted searchable object
    // might be successfully searched.
    private static double time = 0;

    // An RB map allows a relatively fast extraction
    // of the element with the least index. However,
    // a priority queue would be preferable.
    private static SortedMap<Object, SearchQueueData> queueByTime = new TreeMap<Object, SearchQueueData>();
    private static Hashtable<Searchable, SearchQueueData> queueBySearchable = new Hashtable<Searchable, SearchQueueData>();

    // This factory creates subqueues that handle entries
    // for a single instant of time.
    private static SearchQueueFactory factory = new RandomQueueFactory();

    // These are the listeners that are interested in new entries.
    private static List<SearchQueueListener> listeners = new ArrayList<SearchQueueListener>();

    // These are the listeners that are interested in time updates
    private static List<TimeListener> timeListeners = new ArrayList<TimeListener>();

    // This class is completely static.
    private SearchQueue() {
    }

    // Set the instance of SearchQueueData that manages the
    // queue policy. This enables us to change search queue
    // policies at runtime under the control of the simulator.
    public synchronized static void setQueueFactory(SearchQueueFactory fac) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        factory = fac;


        // We have to regenerate the lookup for searchables.
        queueBySearchable = new Hashtable<Searchable, SearchQueueData>();

        Iterator<Object> times = queueByTime.keySet().iterator();
        while (times.hasNext()) {
            Object key = times.next();
            SearchQueueData oldQueue = queueByTime.get(key);
            SearchQueueData newQueue = factory.makeQueue(oldQueue.getTime());


            // Silently transfer the searchables to the new queue.
            Enumeration<Searchable> enumeration = oldQueue.elements();
            while (enumeration.hasMoreElements()) {
                Searchable searchable = enumeration.nextElement();
                newQueue.include(searchable);
                queueBySearchable.put(searchable, newQueue);
            }


            // Register the new queue.
            queueByTime.put(key, newQueue);
        }
    }

    private static void setTime(double newTime) {
        time = newTime;
        notifyTimeListeners();
    }

    // Clear all pending enabled transition instances in the queue.
    // This is required when a new simulation is started. Note that
    // there might be threads running in the background that
    // might reintroduce transition instances into the queue.
    // So be sure to kill everything in sight before calling
    // this method.
    public synchronized static void reset(double startTime) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread"
        + "but instead: " + Thread.currentThread().getThreadGroup();
        queueByTime = new TreeMap<Object, SearchQueueData>();
        queueBySearchable = new Hashtable<Searchable, SearchQueueData>();
        SimulatorEventLogger.log("Setting time to " + startTime);
        setTime(startTime);
    }

    public synchronized static double getTime() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return time;
    }

    private static void advanceTime(double newTime) {
        if (newTime > time) {
            logger.info("Advancing time to " + newTime);
            setTime(newTime);
        }
    }

    private static SearchQueueData takeEarliestQueue() {
        if (queueByTime.isEmpty()) {
            return null;
        } else {
            Object firstKey = queueByTime.firstKey();
            return queueByTime.remove(firstKey);
        }
    }

    // Unless the queue is totally empty, advance the time
    // to the earliest time stamp of a searchable.
    public synchronized static void advanceTime() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        SearchQueueData queue = takeEarliestQueue();
        if (queue != null) {
            advanceTime(queue.getTime());


            // Make sure to put back removed queue.
            queueByTime.put(new Double(queue.getTime()), queue);
        }
    }

    // A cautious customer can use this method to check whether there
    // are search candidates at this time instance without the
    // danger of moving the clock.
    public synchronized static boolean isCurrentlyEmpty() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return !queueByTime.containsKey(new Double(time));
    }

    public synchronized static boolean isTotallyEmpty() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return queueByTime.isEmpty();
    }

    // Register a listener for a single shot notification.
    public synchronized static void insertListener(SearchQueueListener listener) {
        listeners.add(listener);
    }

    // Register a time listener permanently.
    // The listener may not perform actions that affect
    // the search queue, except for deregistering the listener.
    // During notification, the notification thread
    // will own the search queue's synchronization lock.
    public synchronized static void insertTimeListener(TimeListener listener) {
        timeListeners.add(listener);
    }

    // Deregister a time listener permanently.
    public synchronized static void removeTimeListener(TimeListener listener) {
        timeListeners.remove(listener);
    }

    private static void notifyListeners() {
        Iterator<SearchQueueListener> enumeration = listeners.iterator();
        while (enumeration.hasNext()) {
            (enumeration.next()).searchQueueNonempty();
        }
        listeners.clear();
    }

    private static void notifyTimeListeners() {
        // Make sure to allow modifications of the listeners.
        List<TimeListener> temp = new ArrayList<TimeListener>(timeListeners);
        Iterator<TimeListener> enumeration = temp.iterator();
        while (enumeration.hasNext()) {
            (enumeration.next()).timeAdvanced();
        }
    }

    public synchronized static void includeNow(Searchable searchable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        include(searchable, time);
    }

    // This method should only be called with a future time
    // when the searchable has been searched and the first
    // possible binding was at that time or later.
    public synchronized static void include(Searchable searchable,
                                            double targetTime) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Make sure the time does not run backwards.
        if (targetTime < time) {
            targetTime = time;
        }


        // Notify a searchable that its enabledness will be detected
        // by a future search. This has to be done regardless
        // of the policy of the search queue, but only if the
        // searchable is supposed to be inserted into the queue
        // for an immediate search. If the search is supposed
        // to happen sometimes in the future, there might be new
        // trigger events that shorten the delay.
        if (targetTime == time) {
            searchable.triggers().clear();
        }

        // Get the appropriate queue.
        SearchQueueData queue = queueBySearchable.get(searchable);

        if (queue != null) {
            if (targetTime < queue.getTime()) {
                // Remove from current queue before putting into new queue.
                queue.exclude(searchable);


                // Remove searchable.
                queueBySearchable.remove(searchable);
                // Discard queue if empty.
                if (queue.size() == 0) {
                    queueByTime.remove(new Double(queue.getTime()));
                }


                // Take a note that the searchable is not
                // properly registered.
                queue = null;
            }
        }

        // Do we need to insert the searchable?
        if (queue == null) {
            Object key = new Double(targetTime);
            if (queueByTime.containsKey(key)) {
                queue = queueByTime.get(key);
            } else {
                queue = factory.makeQueue(targetTime);
                queueByTime.put(key, queue);
            }


            // Put into new or existing queue.
            queue.include(searchable);
            queueBySearchable.put(searchable, queue);
        }


        // Does anybody need a searchable?
        notifyListeners();
    }

    public synchronized static Searchable extract() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // This call take the object with the lowest key,
        // i.e. the search queue with the earliest time stamp.
        // This removes the queue from the map.
        SearchQueueData queue = takeEarliestQueue();

        // Is there a searchable in the queue?
        if (queue == null) {
            return null;
        }


        // Make sure the time gets advanced when the time of
        // the earliest queue exceeds the current time.
        advanceTime(queue.getTime());

        Searchable searchable = queue.extract();
        queueBySearchable.remove(searchable);

        // Unless the queue is empty, we must put it back into the map.
        if (queue.size() > 0) {
            queueByTime.put(new Double(queue.getTime()), queue);
        }

        return searchable;
    }

    /**
     * Writes all entries currently in the queue
     * (and all their associated field data)
     * to the given stream. The written information
     * should describe the complete current simulation
     * state.
     * But this information does not neccessarily include all
     * nets possibly required by future simulation steps.
     * <p>
     * If the given <code>ObjectOutput</code> is a <code><b>
     * de.renew.util.RenewObjectOutputStream</b></code>, its
     * feature of cutting down the recursion depth by delaying
     * the serialization of some fields will be used.
     * </p><p>
     * <b>Caution:</b> In order to get consistent data written
     * to the stream, you have to ensure that there are no
     * concurrent modifications of the simulation state.
     * This method is not able to lock the simulation.
     * </p>
     *
     * Added Feb 29 2000  Michael Duvigneau
     *
     * @param output target stream (see note about RenewObjectOutputStream)
     * @see de.renew.util.RenewObjectOutputStream
     **/
    public synchronized static void saveQueue(ObjectOutput output)
            throws IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        RenewObjectOutputStream rOut = null;
        if (output instanceof RenewObjectOutputStream) {
            rOut = (RenewObjectOutputStream) output;
        }

        if (rOut != null) {
            rOut.beginDomain(SearchQueue.class);
        }


        // Save all entries currently in the Queue.
        output.writeInt(queueByTime.size());
        Iterator<Object> times = queueByTime.keySet().iterator();
        while (times.hasNext()) {
            Double key = (Double) times.next();
            output.writeDouble(key.doubleValue());
            SearchQueueData data = queueByTime.get(key);
            output.writeInt(data.size());
            Enumeration<Searchable> elements = data.elements();
            while (elements.hasMoreElements()) {
                output.writeObject(elements.nextElement());
            }
        }


        // If a RenewObjectOutputStream is used, write
        // all delayed fields NOW.
        if (rOut != null) {
            rOut.writeDelayedObjects();
        }

        if (rOut != null) {
            rOut.endDomain(SearchQueue.class);
        }
    }

    /**
     * Restores queue elements saved by <code>saveQueue()</code>.
     * Adds all stored elements to the queue.
     * <p>
     * If the given <code>ObjectInput</code> is a <code>
     * de.renew.util.RenewObjectInputStream</code>, the
     * neccessary steps to cover delayed serialization will
     * be made.
     * </p><p>
     * The object input stream will be read using
     * <code>de.renew.util.ClassSource</code> to provide
     * its ability of reloading all user defined classes.
     * </p>
     *
     * Added Apr 11 2000  Michael Duvigneau
     *
     * @param input source stream (see note about RenewObjectInputStream above)
     * @see de.renew.util.ClassSource
     * @see de.renew.util.RenewObjectInputStream
     **/
    public synchronized static void loadQueue(ObjectInput input)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";


        // Read all entries into a Vector first.
        Vector<Object> allEntries = new Vector<Object>();
        Vector<Double> allTimes = new Vector<Double>();

        int count = input.readInt();
        try {
            for (int i = 0; i < count; i++) {
                Double time = new Double(input.readDouble());
                int size = input.readInt();
                for (int j = 0; j < size; j++) {
                    allEntries.addElement(input.readObject());
                    allTimes.addElement(time);
                }
            }
        } catch (ClassCastException e) {
            logger.debug(e.getMessage(), e);
            throw new StreamCorruptedException("Object other than Searchable found "
                                               + "when looking for SearchQueue elements: "
                                               + e.getMessage());
        }


        // If a RenewObjectInputStream is used, read
        // all delayed fields NOW.
        if (input instanceof RenewObjectInputStream) {
            ((RenewObjectInputStream) input).readDelayedObjects();
        }

        // Insert all entries into the queue.
        for (int i = 0; i < allEntries.size(); i++) {
            include((Searchable) allEntries.elementAt(i),
                    allTimes.elementAt(i).doubleValue());
        }
    }
}