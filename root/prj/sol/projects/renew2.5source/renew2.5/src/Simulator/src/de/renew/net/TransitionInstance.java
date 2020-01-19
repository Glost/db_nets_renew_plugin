package de.renew.net;

import de.renew.application.SimulatorPlugin;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.TriggerCollection;
import de.renew.engine.searcher.Triggerable;
import de.renew.engine.searcher.UplinkProvider;
import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.ExecuteFinder;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.engine.simulator.SimulatorHelper;

import de.renew.net.event.FiringEvent;
import de.renew.net.event.TransitionEventListener;
import de.renew.net.event.TransitionEventListenerSet;
import de.renew.net.event.TransitionEventProducer;

import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import de.renew.util.DelayedFieldOwner;
import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * I represent an instance of a transition. Unlike a naked transition
 * I am tied to a net instance. I provide my name to the outside world
 * and keep track of those objects who like to be notified if I become
 * enabled.
 *
 * Other objects will register me as a listener of objects that might
 * enable me. I would like to be registered as a listener with
 * all objects that might enable me, but this is beyond my control.
 * So take a little care when you use me.
 *
 * I used to send out messages in the case that I might become
 * enabled. This functionality was removed, because no one
 * really ever needed it.
 *
 * @author Olaf Kummer
 **/
public class TransitionInstance implements Searchable, Triggerable,
                                           UplinkProvider,
                                           TransitionEventProducer,
                                           Serializable, DelayedFieldOwner {
    static final long serialVersionUID = -2146903280470015837L;

    /**
     * My net instance to which I belong.
     **/
    private NetInstance netInstance;

    /**
     * The transition that serves as a pattern for me.
     **/
    private Transition transition;

    /**
     * The triggers that might notify me.
     * This field is not really transient, but as we want
     * to cut down the serialization recursion depth, it
     * is serialized manually.
     **/
    private transient TriggerCollection triggers = new TriggerCollection(this);

    /**
     * All pending firing events that have started, but not yet finished.
     **/
    private transient Set<FiringEvent> pendingEvents = new HashSet<FiringEvent>();

    /**
     * The listeners that I must notify.
     **/
    private transient TransitionEventListenerSet listeners = new TransitionEventListenerSet();

    /**
     * Temporary storage for actions to be executed when deserialization is
     * complete.  In case of normal {@link ObjectInputStream}s, such
     * actions can be executed immediately in the {@link #readObject}
     * method, but in case of a {@link RenewObjectInputStream}, we have to
     * wait until all delayed fields have been read by {@link #reassignField}.
     **/
    private transient Runnable executeAfterDeserialization = null;

    /**
     * I (a transition instance) am created. I remember
     * the neccessary information.
     *
     * @param netInstance
     *   the net instance that needs to have an instance of some transition
     * @param transition
     *   the transition that must be instantiated
     **/
    TransitionInstance(NetInstance netInstance, Transition transition) {
        this.netInstance = netInstance;
        this.transition = transition;
    }

    /**
     * I will use the name of my net instance plus the name of
     * my transition to build my own name.
     *
     * @return my name
     **/
    public String toString() {
        return netInstance.toString() + "." + transition.toString();
    }

    public NetInstance getNetInstance() {
        return netInstance;
    }

    public Transition getTransition() {
        return transition;
    }

    public TriggerCollection triggers() {
        return triggers;
    }

    public void addTransitionEventListener(TransitionEventListener listener) {
        listeners.addTransitionEventListener(listener);
    }

    public void removeTransitionEventListener(TransitionEventListener listener) {
        listeners.removeTransitionEventListener(listener);
    }

    /**
     * Somebody will call this method to tell me that I might
     * be enabled. I will tell the search queue about it, if I am
     * spontaneous.
     */
    public void proposeSearch() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Notify the searcher, if this transition is
        // spontaneous. Otherwise, the notification was sporadic.
        // Maybe I should report an error instead of silently
        // ignoring the notification.
        if (transition.isSpontaneous()) {
            SearchQueue.includeNow(this);
        }
    }

    /**
     * Informs all TransitionEventListeners and the master transition
     * that this instance has just started firing.
     *
     * This method is called by FiringStartExecutables created
     * by occurrences of this transition instance.
     **/
    synchronized void firingStarted(FiringEvent fe) {
        // Make note for future updates.
        pendingEvents.add(fe);


        // Notify all listeners.
        listeners.firingStarted(fe);


        // Notify the master transition and all its listeners.
        transition.getListenerSet().firingStarted(fe);
    }

    /**
     * Informs all TransitionEventListeners and the master transition
     * that this instance has just completed firing.
     *
     * This method is called by FiringCompleteExecutables created
     * by occurrences of this transition instance.
     **/
    synchronized void firingComplete(FiringEvent fe) {
        // No more need to reference this event.
        pendingEvents.remove(fe);


        // Notify all listeners.
        listeners.firingComplete(fe);


        // Notify the master transition and all its listeners.
        transition.getListenerSet().firingComplete(fe);
    }

    /**
     * Return all firing events for firings that have started,
     * but not yet completed.
     *
     * Before calling this method, it is necessary to synchronize on
     * this object. Only after the processing of all firing events
     * the synchronization may end.
     *
     * If the enumeration is required for a very long time,
     * it is suggested to copy it into an array.
     *
     * @see FiringEvent
     *
     * @return an enumeration of firing events.
     */
    public Iterator<FiringEvent> pendingFiringEvents() {
        return pendingEvents.iterator();
    }

    public synchronized boolean isFiring() {
        return !pendingEvents.isEmpty();
    }

    /**
     * My net will call this method if it creation is confirmed.
     * Just to be safe, I will register myself at the search queue
     * if I am spontaneous.
     **/
    void createConfirmation() {
        if (transition.isSpontaneous()) {
            SearchQueue.includeNow(this);
        }
    }

    public boolean listensToChannel(String channel) {
        return transition.listensToChannel(channel);
    }

    private void createAndSearchOccurrence(Variable params, Searcher searcher) {
        int checkpoint = searcher.recorder.checkpoint();
        try {
            // Create a new occurrence. If there is an uplink,
            // synchronize using the parameters.
            searcher.search(new TransitionOccurrence(this, params, searcher));
        } catch (Impossible e) {
            // When getting the binders, an exception was thrown.
            // The occurrence cannot be enabled.
        } finally {
            searcher.recorder.restore(checkpoint);
        }
    }

    /**
     * This method is called if somebody wants me to continue a
     * search for an activated binding by matching my uplink
     * with a downlink whose parameters are given as arguments.
     *
     * @param params
     *   the variable that holds the value that it send over the channel
     * @param searcher
     *   the searcher that is supposed to actually perform the search.
     **/
    public void bindChannel(Variable params, Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // This method does exactly what the method createAndSearchOccurrence
        // would do. I hesitate to join the the methods, because
        // this method is not supposed to handle spontaneous transitions.
        createAndSearchOccurrence(params, searcher);
    }

    /**
     * This method is called if somebody wants me to start a
     * search for an activated binding. I initiate the search and
     * let the searcher do the rest.
     *
     * @param searcher
     *   the searcher that is supposed to actually perform the search.
     **/
    public void startSearch(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        createAndSearchOccurrence(null, searcher);


        // Is the next line really needed?
        searcher.recorder.restore(); // no bindings sensible
    }

    // This method fires one binding. It resets the triggers
    // of the transition instance.
    public boolean fireOneBinding(boolean asynchronous, Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        ExecuteFinder finder = new ExecuteFinder();
        SimulatorHelper.searchOnce(searcher, finder, this, this);
        StepIdentifier step = SimulatorPlugin.getCurrent()
                                             .getCurrentEnvironment()
                                             .getSimulator().nextStepIdentifier();

        if (finder.isCompleted()) {
            finder.execute(step, asynchronous);
            return true;
        }
        return false;
    }

    public boolean fireOneBinding(boolean asynchronous) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Searcher searcher = new Searcher();
        return fireOneBinding(asynchronous, searcher);
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except checking an additional error condition
     * and writing the not-really-transient field triggers.
     * If the stream used is a RenewObjectOutputStream, the
     * field is delayed to cut down recursion depth and the
     * domain trace feature will be used.
     * @see de.renew.util.RenewObjectOutputStream
     *
     * @throws NotSerializableException if the object cannot
     * be serialized because there are pending events or
     * bindings
     */
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        RenewObjectOutputStream rOut = null;
        if (out instanceof RenewObjectOutputStream) {
            rOut = (RenewObjectOutputStream) out;
        }
        if (rOut != null) {
            rOut.beginDomain(this);
        }
        if (pendingEvents.isEmpty()) {
            out.defaultWriteObject();
            if (rOut != null) {
                rOut.delayedWriteObject(triggers, this);
            } else {
                out.writeObject(triggers);
            }
        } else {
            throw new NotSerializableException("Active bindings "
                                               + " at transition instance "
                                               + this);
        }
        if (rOut != null) {
            rOut.endDomain(this);
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring the not-really-transient field
     * triggers, <b>if</b> the stream used is <b>not</b> a
     * RenewObjectInputStream.
     * It also assigns default values to some transient fields.
     * <p>
     *
     * If the <code>copiousBehaviour</code> flag of the
     * <code>RenewObjectInputStream</code> is active, a spontaneous
     * transition instance inserts itself into the {@link SearchQueue}.
     * This would be a bit dangerous to do when this method is called
     * because of incomplete data due to delayed object deserialization.
     * Therefore, the <code>SearchQueue</code> registration has to wait
     * until {@link #reassignField} has been called.
     * </p>
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        in.defaultReadObject();
        pendingEvents = new HashSet<FiringEvent>();
        listeners = new TransitionEventListenerSet();

        if (in instanceof RenewObjectInputStream) {
            RenewObjectInputStream rIn = (RenewObjectInputStream) in;
            if (rIn.isCopiousBehaviour()) {
                final TransitionInstance tiRef = this;
                executeAfterDeserialization = new Runnable() {
                        public void run() {
                            assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
                            // If the transition is spontaneous, insert ourselves in
                            // the SearchQueue (as in createConfirmation)
                            if (transition.isSpontaneous()) {
                                SearchQueue.includeNow(tiRef);
                            }
                        }
                    };
            }

            // Besides this, do nothing.  The field will be
            // reassigned by the stream soon.
        } else {
            triggers = (TriggerCollection) in.readObject();
        }
    }

    /**
     * Method used on deserialization by RenewObjectInputStream.
     * Reassigns values to the not-really-transient fields
     * <code>triggers</code>, one at a time.
     * Executes any delayed deserialization action if all fields have been
     * assigned.
     **/
    public void reassignField(Object value) throws java.io.IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (value instanceof TriggerCollection) {
            triggers = (TriggerCollection) value;
            // Now all data is complete.  Execute delayed action, if any.
            if (executeAfterDeserialization != null) {
                executeAfterDeserialization.run();
                executeAfterDeserialization = null;
            }
        } else {
            throw new java.io.NotSerializableException("Value of unexpected type given to reassign().");
        }
    }
}