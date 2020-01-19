package de.renew.net;

import de.renew.application.SimulatorPlugin;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.NetInstantiationException;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.loading.NetLoader;

import de.renew.unify.Impossible;

import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * A net, consisting of places and transitions.
 * <p>
 * Nets are not robust against concurrent updates.
 * Specifically, it is not allowed to change the structure
 * of a net while a simulation is running. The standard
 * pattern is to create a net, make it public, and then
 * use it without ever modifying it again.
 */
public class Net implements Serializable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Net.class);
    static final long serialVersionUID = 4424943946669800287L;

    /**
     * A global map from names to nets.  Synchronize on the facade
     * of the {@link SimulatorPlugin#lock SimulatorPlugin}
     * to get a consistent view on the set of registered nets.
     */
    private static java.util.Map<String, Net> netsByName = new HashMap<String, Net>();
    private static NetLoader netLoader = null;
    Set<Place> places = new HashSet<Place>();
    Set<Transition> transitions = new HashSet<Transition>();
    Map<NetElementID, Place> placesByID = new HashMap<NetElementID, Place>();
    Map<NetElementID, Transition> transitionsByID = new HashMap<NetElementID, Transition>();
    String name = null;
    boolean earlyTokens = false;
    private int netCount = 0;

    public Net() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        name = null;
    }

    /**
     * Creates an empty net body with the given name. Since the net
     * is not functional yet, it is not announced to the map for
     * the static {@link #forName} method. This has to be done
     * separately after full construction of the net by using the
     * {@link #makeKnown} method.
     **/
    public Net(String name) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.name = name;
    }

    /**
     * Returns the net object with the given name.
     * If a net is not known, asks the net loader (see
     * {@link #setNetLoader}) to load the net.
     *
     * <p>
     * This method includes a convenience wrapper so that it may be called from outside a simulation thread.
     * </p>
     *
     * @param name  the name of the net to find.
     *
     * @return
     *   the net object with the given name.
     *   Never returns <code>null</code>.
     *
     * @throws NetNotFoundException
     *   if the net is not loaded and no loader was set or the
     *   loader could not find the net.
     **/
    public static Net forName(final String name) throws NetNotFoundException {
        // We provide a convenience thread wrapper because the net search
        // may be classpath-dependent and should be carried out within a
        // simulation thread.
        //
        // Additionally, we must lock the SimulatorPlugin facade here because
        // the net loading mechanism may call SimulatorPlugin.insertNets().
        // Keeping the facade lock open until then would lead to possible
        // deadlocks if we lock netsByName first.  There would exist a race
        // condition for example with SimulatorPlugin.createNetInstance()
        // which aquires the facade lock before it calls Net.forName.
        //
        // TODO: Is this facade lock really neded?   We allow dangling calls
        // after simulation termination.  It seems not appropriate to lock
        // any simulator plugin operations during the time-consuming search
        // for net sources.
        SimulatorPlugin.lock.lock();
        try {
            Future<Net> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Net>() {
                    public Net call() throws Exception {
                        Net net = netsByName.get(name);
                        if (net == null) {
                            if (netLoader == null) {
                                throw new NetNotFoundException(name);
                            }
                            // Check again if the net is still unknown.
                            // Things may have changed during the synchronization sleep...
                            net = netsByName.get(name);
                            if (net == null) {
                                net = netLoader.loadNet(name);
                            }
                        }
                        return net;
                    }
                });
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof NetNotFoundException) {
                throw ((NetNotFoundException) t);
            }
            logger.error("Simulation thread threw an exception", e);
        } finally {
            SimulatorPlugin.lock.unlock();
        }

        // We should never return nothing but some error occurred before.
        return null;
    }

    /**
     * Clears the mapping used by the {@link #forName} method.
     * <p>
     * This method is mutually exclusive with all operations of the
     * {@link SimulatorPlugin#lock SimulatorPlugin} facade.
     * </p>
     **/
    public static void forgetAllNets() {
        SimulatorPlugin.lock.lock();
        try {
            netsByName.clear();
        } finally {
            SimulatorPlugin.lock.unlock();
        }
    }

    /**
     * Sets the net loader. This request will be denied when there
     * are any known nets. You are safe if you call it immediately
     * after {@link #forgetAllNets}...
     * <p>
     * This method is mutually exclusive with all operations of the
     * {@link SimulatorPlugin#lock SimulatorPlugin} facade.
     * </p>
     * @throws IllegalStateException
     *   if there are known nets
     **/
    public static void setNetLoader(NetLoader loader) {
        SimulatorPlugin.lock.lock();
        try {
            if (netsByName.isEmpty()) {
                netLoader = loader;
            } else {
                throw new IllegalStateException("Cannot change net loader while nets are known.");
            }
        } finally {
            SimulatorPlugin.lock.unlock();
        }
    }

    /**
     * Gets the net loader.
     **/
    public static NetLoader getNetLoader() {
        return netLoader;
    }

    /**
     * Returns an iterator of all known nets.  The iterator
     * will throw a {@link ConcurrentModificationException}
     * if changes to the set of known nets occur when it is
     * used.
     *
     * @return an iterator over all currently known
     *         <code>Net</code> objects.
     **/
    public static Iterator<Net> allKnownNets() {
        return netsByName.values().iterator();
    }

    /**
     * Tells whether a net with the given name is known at the moment.
     * This method will not trigger the net loading mechanism.
     *
     * @param name  the net's name
     * @return  <code>true</code> if the net has been announced as known net
     *          in the current simulation.
     **/
    public static boolean isKnownNet(String name) {
        return netsByName.containsKey(name);
    }

    /**
     * Confirms the end of the construction phase of the net. The
     * net is included in the {@link #forName} lookup.
     * <p>
     * This method is mutually exclusive with all operations of the
     * {@link SimulatorPlugin#lock SimulatorPlugin} facade.
     * </p>
     *
     * @since Renew 1.6
     **/
    public void makeKnown() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        SimulatorPlugin.lock.lock();
        try {
            if (name != null) {
                netsByName.put(name, this);
            }
        } finally {
            SimulatorPlugin.lock.unlock();
        }
    }

    public Collection<Place> places() {
        return places;
    }

    public int placeCount() {
        return places.size();
    }

    public Collection<Transition> transitions() {
        return transitions;
    }

    public int transitionCount() {
        return transitions.size();
    }

    synchronized int makeNetNumber() {
        return ++netCount;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    /**
     * Created one instance of this net, but do not yet confirm
     * its creation. Used for creating instances that may have to be
     * discarded early.
     * <p>
     * When instantiating nets from Java code outside the simulation
     * engine, in most cases {@link #buildInstance()} is the better choice.
     * </p>
     *
     * @return a newly allocated <code>NetInstance</code>
     * @exception Impossible if an initial marking expression
     *            evaluation fails.
     */
    public NetInstance makeInstance() throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Instead of creating a generic NetInstance, we might also
        // want to create a specialized NetInstance that supports
        // own methods and implements interfaces.
        return new NetInstanceImpl(this);
    }

    /**
     * Creates an instance of this net and confirms it.
     * <p>
     * Unlike the method {@link #makeInstance}, this
     * method immediately confirms the creation of the
     * generated net instance, so that external users do not need
     * to know about confirmation issues.
     * </p>
     * <p>
     * If the current simulation step is not known, call the method
     * {@link #buildInstance()} instead of this one.
     * </p>
     *
     * @param stepIdentifier  the simulation step which this
     *                        instantiation belongs to.
     * @return a newly allocated <code>NetInstance</code>
     */
    public NetInstance buildInstance(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        NetInstance instance = null;
        try {
            instance = makeInstance();

            instance.createConfirmation(stepIdentifier);
            return instance;
        } catch (Impossible e) {
            RuntimeException re = new RuntimeException("Could not make instance of net "
                                                       + getName() + ".", e);
            SimulatorEventLogger.log(stepIdentifier,
                                     new NetInstantiationException(this, e));
            throw re;
        }
    }

    /**
     * Creates an instance of this net and confirms it.
     * The method delegates to {@link #buildInstance(StepIdentifier)} with
     * a fresh <code>stepIdentifier</code> obtained from the current
     * simulation environment.
     *
     * @return a newly allocated <code>NetInstance</code>
     */
    public NetInstance buildInstance() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return buildInstance(SimulatorPlugin.getCurrent().getCurrentEnvironment()
                                            .getSimulator()
                                            .currentStepIdentifier());
    }

    void add(Place place) {
        NetElementID id = place.getID();
        assert !places.contains(place) : "Tried to add existing place: "
        + place;
        assert !placesByID.containsKey(id) : "Tried to add place with existing ID: "
        + id + ", old: " + placesByID.get(id) + ", new: " + place;
        places.add(place);
        placesByID.put(id, place);
    }

    void add(Transition transition) {
        NetElementID id = transition.getID();
        assert !transitions.contains(transition) : "Tried to add existing transition: "
        + transition;
        assert !transitionsByID.containsKey(id) : "Tried to add transition with existing ID: "
        + id + ", old: " + transitionsByID.get(id) + ", new: " + transition;
        transitions.add(transition);
        transitionsByID.put(id, transition);
    }

    void remove(Place place) {
        places.remove(place);
        placesByID.remove(place.getID());
    }

    void remove(Transition transition) {
        transitions.remove(transition);
        transitionsByID.remove(transition.getID());
    }

    public void setEarlyTokens(boolean earlyTokens) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.earlyTokens = earlyTokens;
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except using the domain trace feature, if the
     * output is a RenewObjectOutputStream.
     * @see de.renew.util.RenewObjectOutputStream
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        RenewObjectOutputStream rOut = null;
        if (out instanceof RenewObjectOutputStream) {
            rOut = (RenewObjectOutputStream) out;
        }
        if (rOut != null) {
            rOut.beginDomain(this);
        }
        out.defaultWriteObject();
        if (rOut != null) {
            rOut.endDomain(this);
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method and makes the net known to <code>Net.forName()</code>.
     * <p>
     * If the <code>copiousBehaviour</code> flag of the
     * {@link RenewObjectInputStream} is active, the net checks for name
     * clashes before it makes itself known to <code>Net.forName()</code>.
     * If a clash is detected, the net name is extended by hash symbols
     * until it becomes unique.
     * </p>
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (in instanceof RenewObjectInputStream) {
            RenewObjectInputStream rIn = (RenewObjectInputStream) in;
            if (rIn.isCopiousBehaviour()) {
                StringBuffer newName = new StringBuffer(name);
                while (isKnownNet(newName.toString())) {
                    newName.append("_COPY");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Deserialized Net copy: changed name from "
                                 + name + " to " + newName + ".");
                }
                name = newName.toString();
            }
        }
        makeKnown();
    }

    /**
     * Writes all currently known <code>Net</code>s
     * to the given stream. The written information
     * describes the static part of all compiled nets.
     * <p>
     * If the given <code>ObjectOutput</code> is a <code><b>
     * de.renew.util.RenewObjectOutputStream</b></code>, its
     * feature of cutting down the recursion depth by delaying
     * the serialization of some fields will be used.
     * </p>
     * <p>
     * This method is mutually exclusive with all operations of the
     * {@link SimulatorPlugin#lock SimulatorPlugin} facade.
     * </p>
     *
     * Added Apr 18 2000  Michael Duvigneau
     *
     * @param output target stream (see note about RenewObjectOutputStream)
     * @see de.renew.util.RenewObjectOutputStream
     **/
    public static void saveAllNets(ObjectOutput output)
            throws IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        SimulatorPlugin.lock.lock();
        try {
            RenewObjectOutputStream rOut = null;
            if (output instanceof RenewObjectOutputStream) {
                rOut = (RenewObjectOutputStream) output;
            }

            if (rOut != null) {
                rOut.beginDomain(Net.class);
            }


            // Save all currently known nets.
            output.writeInt(netsByName.size());
            Iterator<Net> iterator = netsByName.values().iterator();
            while (iterator.hasNext()) {
                output.writeObject(iterator.next());
            }


            // If a RenewObjectOutputStream is used, write
            // all delayed fields NOW.
            if (rOut != null) {
                rOut.writeDelayedObjects();
            }

            if (rOut != null) {
                rOut.endDomain(Net.class);
            }
        } finally {
            SimulatorPlugin.lock.unlock();
        }
    }

    /**
     * Reads nets stored by <code>saveAllInstances()</code>
     * and adds the new <code>Net</code>s to the list
     * of known nets by name.
     * </p><p>
     * If the given <code>ObjectInput</code> is a <code>
     * de.renew.util.RenewObjectInputStream</code>, the
     * neccessary steps to cover delayed serialization will
     * be made.
     * </p><p>
     * The ObjectInputStream will be read using
     * <code>de.renew.util.ClassSource</code> to provide
     * its ability of reloading all user defined classes.
     * </p>
     *
     * Added Apr 18 2000  Michael Duvigneau
     *
     * @param input source stream (see notes above)
     * @see #saveAllNets
     * @see de.renew.util.ClassSource
     * @see de.renew.util.RenewObjectInputStream
     **/
    public static void loadNets(ObjectInput input)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Read all stored Nets.
        int count = input.readInt();
        try {
            for (int i = 0; i < count; i++) {
                de.renew.util.ClassSource.readObject(input);
            }
        } catch (ClassCastException e) {
            logger.debug(e.getMessage(), e);
            throw new StreamCorruptedException("Object other than Net found "
                                               + "when looking for nets: "
                                               + e.getMessage());
        }


        // If a RenewObjectInputStream is used, read
        // all delayed fields NOW.
        if (input instanceof RenewObjectInputStream) {
            ((RenewObjectInputStream) input).readDelayedObjects();
        }
    }

    // ----------------------------------------------- ID handling ----


    /**
     * Return the place of the net with the given ID.
     * Return <code>null</code>, if no such place exists.
     * <p></p>
     * Added Mon Jul 17  2000 by Michael Duvigneau
     *
     * @param id the place's ID, may not be null
     * @return a place or <code>null</code>
     */
    public Place getPlaceWithID(NetElementID id) {
        return placesByID.get(id);
    }

    /**
     * Return the transition of the net with the given ID.
     * Return <code>null</code>, if no such place exists.
     * <p></p>
     * Added Mon Jul 17  2000 by Michael Duvigneau
     *
     * @param id the transition's ID, may not be null
     * @return a transition or <code>null</code>
     */
    public Transition getTransitionWithID(NetElementID id) {
        return transitionsByID.get(id);
    }
}