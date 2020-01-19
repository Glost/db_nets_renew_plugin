package de.renew.net;

import de.renew.database.Transaction;
import de.renew.database.TransactionSource;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.NetInstantiation;
import de.renew.engine.searcher.UplinkProvider;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.unify.Impossible;

import de.renew.util.DelayedFieldOwner;
import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;


public class NetInstanceImpl implements DelayedFieldOwner, NetInstance {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NetInstanceImpl.class);
    static final long serialVersionUID = 6136525457931796546L;

    /**
     * Configurable flag, determines whether to use a global token
     * {@link IDRegistry} for all net instances or individual token
     * registries per instance.  The flag is evaluated during net
     * instance initialization (see {@link #initNet(Net, boolean)}).
     **/
    public static boolean useGlobalIDRegistry = false;

    /**
     * Map from net elements to net instance elements.
     * Contains pairs of the following types:
     * <ul>
     * <li> {@link Place} -> {@link PlaceInstance} </li>
     * <li> {@link Transition} -> {@link TransitionInstance} </li>
     * </ul>
     * The map is initialized in the {@link #initNet(Net, boolean)}
     * method.
     **/
    private Hashtable<Object, Object> instanceLookup;

    /**
     * Reference to the net template that underlies this instance.
     *
     **/
    private Net net;

    /**
     * The unique, persistent identifier of this net instance.
     * It persists for the lifetime of the net instance within
     * one Java VM.  It can change on deserialization given that
     * {@link RenewObjectInputStream#isCopiousBehaviour()}
     * returns <code>true</code>.
     **/
    private String netID;

    /**
     * The IDRegistry that is responsible for this net instance.
     * The registry is kept in a field, because the net instance
     * has to deregister its tokens at exactly this net instance
     * during finialization. The current registry might have
     * changed by that time.
     * <p>
     * This field is not really transient, but as we want
     * to cut down the serialization recursion depth, it
     * is serialized manually.</p>
     */
    private transient IDRegistry registry;

    protected NetInstanceImpl() {
    }

    protected NetInstanceImpl(Net net) throws Impossible {
        this(net, true);
    }

    protected NetInstanceImpl(Net net, boolean wantInitialTokens)
            throws Impossible {
        initNet(net, wantInitialTokens);
    }

    protected void initNet(Net net, boolean wantInitialTokens)
            throws Impossible {
        if (net == null) {
            throw new Impossible();
        }
        this.net = net;

        netID = IDSource.createID();

        if (useGlobalIDRegistry) {
            registry = IDRegistry.getInstance();
        } else {
            registry = new IDRegistry();
        }

        instanceLookup = new Hashtable<Object, Object>();

        for (Place place : net.places()) {
            instanceLookup.put(place,
                               place.makeInstance(this, wantInitialTokens));
        }

        for (Transition transition : net.transitions()) {
            instanceLookup.put(transition,
                               new TransitionInstance(this, transition));
        }
    }

    public String toString() {
        return net + "[" + netID + "]";
    }

    /**
     * Get the IDRegistry that is responsible for this net instance.
     */
    public IDRegistry getRegistry() {
        return registry;
    }

    /**
     * Query the ID of the net instance. By default the ID is a
     * simple number, but this is not guaranteed.
     *
     * @return the current ID string
     */
    public String getID() {
        return netID;
    }

    /**
     * Set the ID of the net instance. This should only be done
     * during the setup of the net instance, typeically after restoring
     * the net from a saved state.
     *
     * @param id the new ID string
     */
    public void setID(String id) {
        netID = id;
    }

    /**
     * The creation of the net instance is confirmed for the
     * purposes of the database storage. This confirmation can still
     * be rolled back. No actions except database accesses are performed
     * here.
     *
     * The access has to be done quite early, because
     * the net must be known to the database transaction.
     * Also, the initial tokens may have to be inserted early.
     */
    public void earlyConfirmation() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // First, the net is made public to the database.
        Transaction transaction = TransactionSource.get();
        try {
            transaction.createNet(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // Afterwards, the places are notified, too.
        for (Place place : net.places()) {
            getInstance(place).earlyConfirmation();
        }
    }

    /**
     * Trace the initial tokens of every place, if they are
     * inserted early on. Also, make sure that this net instance
     * is accessible via the net list, if this is desired.
     *
     */
    public void earlyConfirmationTrace(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        for (Place place : net.places()) {
            getInstance(place).earlyConfirmationTrace(stepIdentifier);
        }

        NetInstanceList.add(this);
    }

    /**
     * Upon creation, no transition will be checked for enabledness
     * by a searcher, because it is not contained in the
     * search queue of possibly activated transitions.
     * By calling this method, all transitions are inserted into the search
     * queue. Then all tokens of the initial marking that might
     * have been held back are inserted into the places.
     *
     * Additionally, if the net's automatic registration flag is
     * set, the net will be registered at the net list.
     *
     * @see de.renew.net.Net#setAutomaticRegistration(boolean)
     * @see de.renew.net.NetInstanceList
     *
     */
    public void lateConfirmation(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // log instance creation on net level
        SimulatorEventLogger.log(stepIdentifier, new NetInstantiation(this),
                                 this);


        // The places may now fully acquire their correct initial marking,
        // even if the initial marking is inserted late.
        for (Place place : net.places()) {
            getInstance(place).lateConfirmation(stepIdentifier);
        }


        // Let's inform all transitions of the confirmation.
        // The transitions can insert themselves into the search queue.
        for (Transition transition : net.transitions()) {
            getInstance(transition).createConfirmation();
        }
    }

    /**
     * Call this method if the creation of a net instance can be confirmed
     * in one single step.
     *
     */
    public void createConfirmation(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        earlyConfirmation();
        earlyConfirmationTrace(stepIdentifier);
        lateConfirmation(stepIdentifier);
    }

    public Object getInstance(Object netObject) {
        return instanceLookup.get(netObject);
    }

    public PlaceInstance getInstance(Place place) {
        return (PlaceInstance) instanceLookup.get(place);
    }

    public TransitionInstance getInstance(Transition transition) {
        return (TransitionInstance) instanceLookup.get(transition);
    }

    public Net getNet() {
        return net;
    }

    public Collection<UplinkProvider> getUplinkProviders(String channel) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        List<UplinkProvider> result = new ArrayList<UplinkProvider>();

        for (Transition transition : getNet().transitions()) {
            TransitionInstance instance = getInstance(transition);
            if (instance.listensToChannel(channel)) {
                result.add(instance);
            }
        }
        return result;
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except using the domain trace feature, if the
     * output is a {@link RenewObjectOutputStream}.
     * @see de.renew.util.RenewObjectOutputStream
     **/
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
        out.defaultWriteObject();
        if (rOut != null) {
            rOut.delayedWriteObject(registry, this);
            rOut.endDomain(this);
        } else {
            out.writeObject(registry);
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring additional transient fields.
     * The method restores the not-really-transient field
     * <code>registry</code>, <b>if</b> the stream used is
     * <b>not</b> a {@link RenewObjectInputStream}.
     * The method also repeats this instance's registration at the
     * {@link NetInstanceList}.
     * If the <code>copiousBehaviour</code> flag of the
     * <code>RenewObjectInputStream</code> is active, the net instance
     * assigns itself a new, unused net instance id.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        in.defaultReadObject();

        if (in instanceof RenewObjectInputStream) {
            if (((RenewObjectInputStream) in).isCopiousBehaviour()) {
                String newNetID = IDSource.createID();
                if (logger.isDebugEnabled()) {
                    logger.debug("Deserialized NetInstance copy: changed id from "
                                 + netID + " to " + newNetID + ".");
                }
                netID = newNetID;
            }

            // Besides this, do nothing.  The fields will be
            // reassigned by the stream soon.
        } else {
            registry = (IDRegistry) in.readObject();
        }

        // This is normally done at the net instance's creation
        // confirmation, but the confirmation of this instance's creation
        // has taken place long ago and far away.
        // So we have to repeat it here, in our current universe.
        NetInstanceList.add(this);
    }

    /**
     * Deserialization method used by {@link RenewObjectInputStream}.
     * Reassigns a value to the not-really-transient field
     * <code>registry</code>.
     *
     * @exception java.io.NotSerializableException
     *    if the given value was not an IDRegistry object.
     */
    public void reassignField(Object value) throws IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (value instanceof IDRegistry) {
            registry = (IDRegistry) value;
        } else {
            throw new java.io.NotSerializableException("Value of unexpected type given to NetInstanceImpl.reassignField():"
                                                       + value.getClass()
                                                              .getName() + ".");
        }
    }
    // ### Problem: NetInstanceReference only used for database
    // serialization, not during normal simulation save.
    //    /**
    //     * Serialization method to replace the object
    //     * before it is serialized.
    //     * In this case, the NetInstanceImpl is replaced
    //     * by a NetInstanceImplReference.
    //     * @return A NetInstanceImplReference pointing to
    //     * the original NetInstanceImpl by its NetID.
    //     */
    //    private Object writeReplace()
    //    {
    //      return new NetInstanceReference(netID);
    //    }
}