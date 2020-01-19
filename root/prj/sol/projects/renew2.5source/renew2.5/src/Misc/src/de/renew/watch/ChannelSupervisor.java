package de.renew.watch;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.TriggerCollection;
import de.renew.engine.searcher.Triggerable;
import de.renew.engine.simulator.ExecuteFinder;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetInstance;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;
import de.renew.unify.Variable;

import de.renew.util.DelayedFieldOwner;
import de.renew.util.EmptyIterator;
import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;
import de.renew.util.Semaphor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Allows monitoring of synchronous channel uplinks in a given net instance
 * from Java code.  Users of this class must implement the {@link ChannelWatcher}
 * interface.
 *
 * @author Olaf Kummer
 * @author Michael Duvigneau (serialization)
 *
 * @serial This class is not intended to be serialized.  However,
 * serialization will occur when a simulation state is stored.  Hence,
 * instances of this class do not store state or references to other
 * objects with one notable exception.  The simulation-relevant collection
 * of triggers is stored in order to cleanly cut the connection on
 * deserialization.
 **/
public class ChannelSupervisor implements Triggerable, Serializable,
                                          DelayedFieldOwner {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ChannelSupervisor.class);
    static final long serialVersionUID = 8023504578713732407L;

    /**
     * A supervisor that might not have informed its watcher
     * about the possible bindings correctly will be inserted
     * into this list. Its set of bindings will be cleaned
     * up by the search thread.
     *
     * Because supervisors may be inserted and remove from the dirty
     * list asynchronously, it is required to synchronize on the
     * <code>allSupervisors</code> list before modifying it.
     **/
    private static LinkedList<ChannelSupervisor> dirtySupervisors = new LinkedList<ChannelSupervisor>();
    private static List<ChannelSupervisor> allSupervisors = Collections
                                                                .synchronizedList(new LinkedList<ChannelSupervisor>());

    /**
     * This semaphor counts the number of elements in the
     * dirty list. It controls the search process.
     **/
    private static Semaphor dirtySem = new Semaphor();
    private static SupervisorThread supervisorThread;

    private static class SupervisorThread implements Runnable {
        private boolean active = true;

        public void run() {
            if (logger.isDebugEnabled()) {
                logger.debug("ChannelSupervisor: Thread "
                             + Thread.currentThread() + " started.");
            }
            while (active) {
                // Wait until the dirty list is non-empty.
                dirtySem.P();
                if (!active) {
                    // The thread has been marked for termination inbetween.
                    // Therefore we should not handle this event.
                    // Put it back to the Semaphor...
                    dirtySem.V();
                } else {
                    // Extract one element.
                    ChannelSupervisor supervisor;
                    synchronized (allSupervisors) {
                        supervisor = dirtySupervisors.getFirst();
                        dirtySupervisors.removeFirst();
                    }

                    // Pass control to that supervisor for an update.
                    supervisor.update();
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("ChannelSupervisor: Thread "
                             + Thread.currentThread() + " terminated.");
            }
        }

        public void terminate() {
            logger.debug("ChannelSupervisor: Thread marked for termination.");
            active = false;
        }
    }

    private transient final NetInstance netInstance;
    private transient final ChannelWatcher watcher;
    private transient final String channel;
    private transient final Class<?> lateClass;

    /**
     * Since the <code>ChannelSupervisor</code> acts as a {@link Triggerable}
     * within the simulation, it needs to keep a list of the triggers it
     * is interested in.
     *
     * @serial The contents of this collection is needed to cut off all
     *         connections to other simulation elements after deserialization.
     **/
    private TriggerCollection triggers;

    /**
     * Marks whether this object belongs to the current simulation.  The
     * value is always true, unless the object has been deserialized from a
     * previous simulation state.  Invalid ChannelSupervisors do nothing
     * except cutting all connections to other simulation objects on their
     * first invocation.
     * <p>
     * This field may not be modified.  Unfortunately, an assignment on
     * deserialization requires omission of the <code>final</code>
     * modifier.
     * </p>
     **/
    private transient boolean valid;

    public ChannelSupervisor(NetInstance netInstance, ChannelWatcher watcher,
                             String channel, Class<?> lateClass) {
        this.netInstance = netInstance;
        this.watcher = watcher;
        this.channel = channel;
        this.lateClass = lateClass;
        this.valid = true;

        triggers = new TriggerCollection(this);
        synchronized (allSupervisors) {
            allSupervisors.add(0, this);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("New " + toString() + ".");
        }
        proposeSearch();
    }

    // This method is currently unused.
    public boolean execute(final Object early, final Object late,
                           final StepIdentifier stepIdentifier) {
        if (valid) {
            Future<Boolean> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        ExecuteFinder finder = new ExecuteFinder();
                        Tuple paramTuple = new Tuple(new Object[] { early, late },
                                                     null);
                        Variable variable = new Variable(paramTuple, null);

                        Searcher searcher = new Searcher();
                        searcher.initiatedSearch(netInstance, channel,
                                                 variable, false, finder, null);

                        boolean success = finder.isCompleted();
                        if (success) {
                            finder.execute(stepIdentifier, true);
                        }

                        return success;
                    }
                });
            try {
                return future.get();
            } catch (InterruptedException e) {
                logger.error("Timeout while waiting for simulation thread to finish",
                             e);
            } catch (ExecutionException e) {
                logger.error("Simulation thread threw an exception", e);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Call to execute on invalid ChannelSupervisor:"
                             + toString() + ", parameters: early=" + early
                             + ", late=" + late + ", step=" + stepIdentifier,
                             new Throwable("DEBUG STACKTRACE"));
            }
        }

        // We should never return nothing but some error occured before.
        return false;

    }

    public TriggerCollection triggers() {
        return triggers;
    }

    public void proposeSearch() {
        if (valid) {
            synchronized (allSupervisors) {
                dirtySupervisors.addLast(this);
                triggers.clear();
            }
            dirtySem.V();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Call to proposeSearch on invalid ChannelSupervisor:"
                             + toString() + ". Clearing trigger collection.");
            }
            triggers.clear();
        }
    }

    void update() {
        if (valid) {
            final ChannelSupervisor object = this;
            if (logger.isDebugEnabled()) {
                logger.debug("Initiating update on " + toString() + "...");
            }
            Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                    public Object call() throws Exception {
                        // Try to invoke the channel
                        //   :channel(early,late);
                        // where the early argument must be known after the search
                        // and the late argument must be freely assignable.
                        Tuple arguments = new Tuple(2);
                        Variable earlyVariable = new Variable(arguments
                                        .getComponent(0), null);

                        // We create a new searcher for this search process.
                        Searcher searcher = new Searcher();


                        // The searcher has to be rigged to satisfy the requirements
                        // on the channels: the first channel must be completely known
                        // after the search and the second channel must be freely assignable.
                        try {
                            // The first argument must be computed early
                            // during the search for a binding.
                            searcher.calcChecker.addEarlyVariable(earlyVariable,
                                                                  null);


                            // The last argument must not be computed early.
                            searcher.calcChecker.addCalculated(lateClass,
                                                               arguments
                                        .getComponent(1), null, null);
                        } catch (Impossible e) {
                            throw new RuntimeException("Error in unification algorithm.");
                        }


                        // We must not reuse the old finder, because its set of
                        // permissible values cannot be reset.
                        ParamFinder finder = new ParamFinder(earlyVariable);


                        // We want to find a transition within the specified
                        // net instance that can fire.
                        searcher.initiatedSearch(netInstance, channel,
                                                 new Variable(arguments, null),
                                                 false, finder, object);
                        synchronized (this) {
                            if (watcher != null) {
                                watcher.bindingsCalculated(finder.iterator());
                            }
                        }
                        return null;
                    }
                });
            try {
                future.get();
            } catch (InterruptedException e) {
                logger.error("Timeout while waiting for simulation thread to finish",
                             e);
            } catch (ExecutionException e) {
                logger.error("Simulation thread threw an exception", e);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Call to update on invalid ChannelSupervisor:"
                             + toString(), new Throwable("DEBUG STACKTRACE"));
            }
        }
    }

    private synchronized void discard() {
        if (watcher != null) {
            watcher.bindingsCalculated(EmptyIterator.INSTANCE);
        }
    }

    public static void reset() {
        synchronized (allSupervisors) {
            logger.debug("Resetting all ChannelSupervisors.");
            dirtySupervisors.clear();
            Iterator<ChannelSupervisor> iterator = allSupervisors.iterator();
            while (iterator.hasNext()) {
                ChannelSupervisor supervisor = iterator.next();
                supervisor.discard();
            }
            allSupervisors.clear();
            if (supervisorThread != null) {
                supervisorThread.terminate();
                supervisorThread = null;
            }
        }
    }

    public static void activate() {
        synchronized (allSupervisors) {
            reset();
            logger.debug("Activating new ChannelSupervisor thread.");
            if (supervisorThread == null) {
                supervisorThread = new SupervisorThread();
                SimulationThreadPool.getCurrent().execute(supervisorThread);
            }
        }
    }


    /**
     * Serialization method, behaves like default writeObject
     * method except storing the not-really-transient field
     * triggers.
     * If the stream used is a RenewObjectOutputStream, this
     * field is delayed to cut down recursion depth.
     * The domain trace feature of this special stream is also
     * used.
     * @see de.renew.util.RenewObjectOutputStream
     **/
    private void writeObject(ObjectOutputStream out) throws IOException {
        RenewObjectOutputStream rOut = null;
        if (out instanceof RenewObjectOutputStream) {
            rOut = (RenewObjectOutputStream) out;
        }
        if (rOut != null) {
            rOut.beginDomain(this);
        }
        out.defaultWriteObject();
        if (rOut != null) {
            rOut.delayedWriteObject(triggers, this);
            rOut.endDomain(this);
        } else {
            out.writeObject(triggers);
        }
    }


    /**
     * After deserialization, this object is not usable any more.
     * <p>
     * Most fields contain null references since they have not been stored
     * on serialization.
     * The deserialized <code>ChannelSupervisor</code> instance is
     * <em>not</em> registered with the {@link #allSuperivsors} list.
     * </p>
     * The method also restores the not-really-transient field
     * <code>triggers</code>, <b>if</b> the
     * stream used is <b>not</b> a RenewObjectInputStream.
     **/
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        valid = false;
        if (logger.isDebugEnabled()) {
            logger.debug("Deserializing " + toString() + "...");
        }
        if (in instanceof RenewObjectInputStream) {
            // Do nothing, the fields will be
            // reassigned by the stream soon.
        } else {
            reassignField(in.readObject());
        }
    }

    /**
     * Method used on deserialization by RenewObjectInputStream.
     * Reassigns values to the not-really-transient fields,
     * one at a time.
     * <p>
     * The contents of the {@link triggers} field is used to break
     * connections from this object to all other simulation elements.
     * </p>
     **/
    public void reassignField(Object value) throws java.io.IOException {
        if (value instanceof TriggerCollection) {
            triggers = (TriggerCollection) value;
            if (logger.isDebugEnabled()) {
                logger.debug("Deserialization completed:  " + toString());
            }
        }
    }

    public String toString() {
        final int sbSize = 1000;
        final String variableSeparator = ", ";
        final StringBuffer sb = new StringBuffer(sbSize);
        sb.append(this.getClass().getName());
        sb.append("(");
        if (valid) {
            sb.append("valid");
            sb.append(variableSeparator);
            sb.append("netInstance=").append(netInstance);
            sb.append(variableSeparator);
            sb.append("watcher=").append(watcher);
            sb.append(variableSeparator);
            sb.append("channel=").append(channel);
            sb.append(variableSeparator);
            sb.append("lateClass=").append(lateClass);
        } else {
            sb.append("invalid");
        }
        sb.append(variableSeparator);
        sb.append("triggers=").append(triggers);
        sb.append(")");
        return sb.toString();
    }
}