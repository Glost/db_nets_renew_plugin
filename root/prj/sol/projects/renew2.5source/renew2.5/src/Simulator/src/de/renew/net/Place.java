package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.event.PlaceEventListener;
import de.renew.net.event.PlaceEventListenerSet;
import de.renew.net.event.PlaceEventProducer;

import de.renew.unify.Impossible;

import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;


public class Place implements Serializable, PlaceEventProducer {
    static final long serialVersionUID = 5661978821450640374L;
    public static final int MULTISETPLACE = 0;
    public static final int FIFOPLACE = 1;

    /**
     * The net element id.
     * @serial
     */
    NetElementID id;

    /**
     * My name.
     **/
    String name;

    /**
     * A description of me.
     */
    String comment;

    /**
     * true, if my instances should output a trace message when they
     * receive an initial token. This is the default.
     **/
    private boolean trace;
    /**
     * My set of token source inscriptions.
     **/
    Set<TokenSource> inscriptions;

    /**
     *  <code>placetype</code> contains the type of the place.
     */
    private int placetype = MULTISETPLACE;

    // ---- PlaceEvent Handling ----------------------------------------


    /**
     * The listeners to notify if place events occur.
     *
     * This object is used to synchronize listener additions/removals
     * and notification.
     **/
    private transient PlaceEventListenerSet listeners = new PlaceEventListenerSet();

    public Place(Net net, String name, NetElementID id) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.id = id;
        this.name = name;

        trace = true;
        inscriptions = new HashSet<TokenSource>();

        net.add(this);
    }

    /**
     * Returns the net element id.
     * @return The net element id.
     */
    public NetElementID getID() {
        return id;
    }

    public String toString() {
        return name;
    }

    public void setBehaviour(int behaviour) {
        placetype = behaviour;
    }

    PlaceInstance makeInstance(NetInstance netInstance,
                               boolean wantInitialTokens)
            throws Impossible {
        PlaceInstance instance = null;
        switch (placetype) {
        case MULTISETPLACE:
            instance = new MultisetPlaceInstance(netInstance, this,
                                                 wantInitialTokens);
            break;
        case FIFOPLACE:
            instance = new FIFOPlaceInstance(netInstance, this,
                                             wantInitialTokens);
            break;
        default:
            throw new RuntimeException("Illegal place behaviour: " + placetype);
        }
        return instance;
    }

    /**
     * Switch my trace flag on or off.
     *
     * @param trace true, if tracing is desired
     **/
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * Am I being traced?
     *
     * @return true, if my trace flag is set
     **/
    public boolean getTrace() {
        return trace;
    }

    /**
     * What's my name?
     *
     * @return my name
     **/
    public String getName() {
        return name;
    }

    /**
     * Give me another token source.
     **/
    public void add(TokenSource tokenSource) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        inscriptions.add(tokenSource);
    }

    /**
     * Remove one my current token sources.
     **/
    public void remove(TokenSource tokenSource) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        inscriptions.remove(tokenSource);
    }

    /**
     * Receive my token sources.
     * @return
     */
    public Set<TokenSource> getTokenSources() {
        return inscriptions;
    }

    /**
     * Do I have a TokenSource?
     * @return
     */
    public boolean hasInitialTokens() {
        return inscriptions.size() != 0;
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except using the domain trace feature, if the
     * output is a RenewObjectOutputStream.
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
            rOut.endDomain(this);
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring additional transient fields.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        in.defaultReadObject();
        listeners = new PlaceEventListenerSet();
    }

    public void addPlaceEventListener(PlaceEventListener listener) {
        listeners.addPlaceEventListener(listener);
    }

    public void removePlaceEventListener(PlaceEventListener listener) {
        listeners.removePlaceEventListener(listener);
    }

    PlaceEventListenerSet getListenerSet() {
        return listeners;
    }

    public void setComment(String comment) {
        this.comment = comment;

    }

    public String getComment() {
        return comment;
    }
}