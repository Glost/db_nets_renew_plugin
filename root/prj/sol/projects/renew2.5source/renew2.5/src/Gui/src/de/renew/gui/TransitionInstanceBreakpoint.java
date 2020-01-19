package de.renew.gui;

import de.renew.net.Net;
import de.renew.net.Transition;
import de.renew.net.TransitionInstance;
import de.renew.net.event.FiringEvent;
import de.renew.net.event.TransitionEventListener;
import de.renew.net.event.TransitionEventProducer;


/**
 * Breakpoints of this type listen to transition events.
 * These events may be produced by transitions themselves
 * (then the breakpoint will be hit at all instances) or
 * by a single transition instance (then the breakpoint
 * will be hit at that instance only).
 * <p>
 * </p>
 * TransitionInstanceBreakpoint.java
 * Created: Tue May 23  2000
 * @author Michael Duvigneau
 */
class TransitionInstanceBreakpoint implements Breakpoint,
                                              TransitionEventListener {
    private BreakpointManager manager;
    private TransitionEventProducer producer;
    private int reactOn;
    private Net net;
    private TransitionInstance hitInstance;
    private String description;

    /**
     * Creates a breakpoint waiting for a transition to fire.
     *
     * @param manager  the breakpoint manager supplies needed
     *                 information and methods
     * @param producer the transition (instance) to observe
     * @param reactOn  on which event to react on, given as
     *                 <code>BreakpointManager</code> mode.
     *                 Recognized values are:
     *                 DEFAULT, FIRE, FIRECOMPLETE.
     *                 Other values will cause the breakpoint
     *                 to be never hit.
     * @param net      the net to which the transition belongs
     *
     * @see Breakpoint#DEFAULT
     * @see Breakpoint#FIRE
     * @see Breakpoint#FIRECOMPLETE
     **/
    TransitionInstanceBreakpoint(BreakpointManager manager,
                                 TransitionEventProducer producer, int reactOn,
                                 Net net) {
        if (reactOn == Breakpoint.DEFAULT) {
            reactOn = FIRE;
        }
        producer.addTransitionEventListener(this);
        this.manager = manager;
        this.producer = producer;
        this.reactOn = reactOn;
        this.net = net;
        this.hitInstance = null;
        composeDescription();
    }

    public boolean wantSynchronousNotification() {
        return true;
    }

    private void composeDescription() {
        StringBuffer desc = new StringBuffer("BP: ");
        if (producer instanceof TransitionInstance) {
            desc.append("transition instance " + producer);
        } else if (producer instanceof Transition) {
            desc.append("transition " + net.getName() + "." + producer);
        } else {
            desc.append("TransitionEventProducer " + producer);
        }
        switch (reactOn) {
        case Breakpoint.FIRE:
            desc.append(" starts firing");
            break;
        case Breakpoint.FIRECOMPLETE:
            desc.append(" completes firing");
            break;
        default:
            desc.append(" <wrong mode!>");
            break;
        }
        this.description = desc.toString();
    }

    // implementation of de.renew.event.TransitionEventListener interface
    public void firingStarted(FiringEvent event) {
        if (reactOn == Breakpoint.FIRE) {
            hitInstance = event.getTransitionInstance();
            manager.stopSimulation();
            manager.informHitBreakpoint(this);
        }
    }

    public void firingComplete(FiringEvent event) {
        if (reactOn == Breakpoint.FIRECOMPLETE) {
            hitInstance = event.getTransitionInstance();
            manager.stopSimulation();
            manager.informHitBreakpoint(this);
        }
    }

    // implementation of de.renew.gui.Breakpoint interface
    public Object getTaggedElement() {
        return producer;
    }

    public Object getHitElement() {
        return hitInstance;
    }

    public void release() {
        producer.removeTransitionEventListener(this);
        this.manager = null;
        this.producer = null;
        this.hitInstance = null;
    }

    public Net getTaggedNet() {
        return net;
    }

    public String toString() {
        return description;
    }
}