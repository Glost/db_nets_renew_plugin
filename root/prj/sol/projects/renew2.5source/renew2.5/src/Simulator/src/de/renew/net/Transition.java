package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.event.TransitionEventListener;
import de.renew.net.event.TransitionEventListenerSet;
import de.renew.net.event.TransitionEventProducer;

import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * I represent a transition with all its semantic information,
 * but without any state. I can have a name, an uplink,
 * and a set of inscriptions. My main job is to store
 * all this information. I do not actually perform any
 * useful services.
 */
public class Transition implements Serializable, TransitionEventProducer {
    static final long serialVersionUID = -7955261691016471176L;

    /**
     * The net element id.
     * @serial
     **/
    NetElementID id;

    /**
     * My name.
     **/
    private final String name;

    /**
     * A description of what I do.
     */
    String comment;

    /**
     * true, if my instances should output a trace message when they
     * occur. This is the default.
     **/
    private boolean trace;

    /**
     * My uplink or null, if I am spontaneous.
     **/
    UplinkInscription uplink;
    /**
     * A set of inscriptions. I expect all inscription objects to be
     * of the type TransitionInscription.
     *
     * @see de.renew.net.TransitionInscription
     **/
    Set<TransitionInscription> inscriptions;

    // ---- TransitionEvent Handling ----------------------------------------


    /**
     * The listeners to notify if transition events occur.
     *
     * This object is used to synchronize listener additions/removals
     * and notification.
     **/
    private transient TransitionEventListenerSet listeners = new TransitionEventListenerSet();

    /**
     * I (a transition) am created as a spontaneous transition.
     * Later on, inscriptions may be added, even an uplink.
     * I am automatically registered at the net as a
     * net element.
     *
     * @param net the net to which this transition should be added.
     */
    public Transition(Net net, String name, NetElementID id) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.id = id;
        this.name = name;


        // Trace actions.
        trace = true;


        // Clear the uplink: no uplink is the default.
        uplink = null;


        // Initialize the array for the inscriptions.
        inscriptions = new HashSet<TransitionInscription>();


        // Notify the net that we are done and want to be
        // inserted into the list of transitions.
        net.add(this);
    }

    /**
     * Returns the net element id.
     * @return The net element id.
     */
    public NetElementID getID() {
        return id;
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
     * I return my name as my string representation.
     * My net instances use my name as a prefix for their name.
     * @return my name.
     **/
    public String toString() {
        return name;
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
     * Am I spontaneous?
     *
     * @return true, if I am spontaneous, i.e., if I do not
     *   have an uplink and if I am not manual
     **/
    public boolean isSpontaneous() {
        return uplink == null
               && !inscriptions.contains(ManualInscription.getInstance());
    }

    public boolean listensToChannel(String channel) {
        return uplink != null && uplink.name.equals(channel);
    }

    /**
     * Here I extract my uplink, if any, from my inscriptions.
     * If there is more than one uplink, it is undetermined which
     * uplink I will use. This method is called after every
     * update to my set of inscriptions.
     **/
    private void checkUplink() {
        // Maintain the count of uplinks and select an uplink
        // from the set of inscriptions. By allowing multiple inscriptions
        // the embedding into a graphical editor becomes easier.
        // On the other hand, the transition might get into
        // an inconsistent state. We try to make it as simulatable as
        // possible, however.
        uplink = null;
        Iterator<TransitionInscription> iterator = inscriptions.iterator();
        while (iterator.hasNext()) {
            Object inscription = iterator.next();
            if (inscription instanceof UplinkInscription) {
                uplink = (UplinkInscription) inscription;
            }
        }
    }

    /**
     * Get my uplink, if I have one.
     * @return
     */
    public UplinkInscription getUplink() {
        return uplink;
    }

    /**
     * Give me another inscription. After inserting
     * the inscriptions, I will recheck my uplink.
     **/
    public void add(TransitionInscription transitionInscription) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (!inscriptions.contains(transitionInscription)) {
            inscriptions.add(transitionInscription);
            checkUplink();
        }
    }

    /**
     * Remove one my current inscriptions. Check if an
     * uplink is still provided.
     **/
    public void remove(TransitionInscription transitionInscription) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (inscriptions.contains(transitionInscription)) {
            inscriptions.remove(transitionInscription);
            checkUplink();
        }
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
        listeners = new TransitionEventListenerSet();
    }

    public void addTransitionEventListener(TransitionEventListener listener) {
        listeners.addTransitionEventListener(listener);
    }

    public void removeTransitionEventListener(TransitionEventListener listener) {
        listeners.removeTransitionEventListener(listener);
    }

    TransitionEventListenerSet getListenerSet() {
        return listeners;
    }

    public void setComment(String comment) {
        this.comment = comment;

    }

    public String getComment() {
        return comment;
    }
}