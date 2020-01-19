package de.renew.gui;

import de.renew.net.Net;
import de.renew.net.Place;
import de.renew.net.PlaceInstance;
import de.renew.net.event.PlaceEvent;
import de.renew.net.event.PlaceEventListener;
import de.renew.net.event.PlaceEventProducer;
import de.renew.net.event.TokenEvent;


/**
 * Breakpoints of this type listen to place events.
 * These events may be produced by places themselves
 * (then the breakpoint will be hit at all instances) or
 * by a single place instance (then the breakpoint
 * will be hit at that instance only).
 * <p>
 * </p>
 * PlaceInstanceBreakpoint.java
 * Created: Mon Jun 05  2000
 * @author Michael Duvigneau
 */
class PlaceInstanceBreakpoint implements Breakpoint, PlaceEventListener {
    private BreakpointManager manager;
    private int reactOn;
    private PlaceEventProducer producer;
    private Net net;
    private PlaceInstance hitInstance;
    private String description;

    /**
     * Creates a breakpoint waiting for a place to change its
     * marking.
     *
     * @param manager  the breakpoint manager supplies needed
     *                 information and methods
     * @param producer the place (instance) to observe
     * @param reactOn  on which event to react on, given as
     *                 <code>BreakpointManager</code> mode.
     *                 Recognized values are:
     *                 DEFAULT, MARKINGCHANGE.
     *                 Other values will cause the breakpoint
     *                 to be never hit.
     * @param net      the net to which the place belongs
     *
     * @see Breakpoint#DEFAULT
     * @see Breakpoint#MARKINGCHANGE
     **/
    PlaceInstanceBreakpoint(BreakpointManager manager,
                            PlaceEventProducer producer, int reactOn, Net net) {
        if (reactOn == Breakpoint.DEFAULT) {
            reactOn = Breakpoint.MARKINGCHANGE;
        }
        producer.addPlaceEventListener(this);
        ;
        this.manager = manager;
        this.reactOn = reactOn;
        this.producer = producer;
        this.net = net;
        this.hitInstance = null;
        composeDescription();
    }

    public boolean wantSynchronousNotification() {
        return true;
    }

    private void composeDescription() {
        StringBuffer desc = new StringBuffer("BP: ");
        if (producer instanceof PlaceInstance) {
            desc.append("place instance " + producer);
        } else if (producer instanceof Place) {
            desc.append("place " + net.getName() + "." + producer);
        } else {
            desc.append("PlaceEventProducer " + producer);
        }
        switch (reactOn) {
        case Breakpoint.MARKINGCHANGE:
            desc.append(" changes marking");
            break;
        case Breakpoint.MARKINGCHANGENOTEST:
            desc.append(" changes marking (test arcs ignored)");
            break;
        case Breakpoint.TOKENADDED:
            desc.append(" gets one token added");
            break;
        case Breakpoint.TOKENREMOVED:
            desc.append(" looses one token");
            break;
        case Breakpoint.TOKENTESTCHANGE:
            desc.append(" changes test status of tokens");
            break;
        default:
            desc.append(" <wrong mode!>");
            break;
        }
        description = desc.toString();
    }

    // implementation of de.renew.event.PlaceEventListener interface
    private void hitBP(PlaceEvent event) {
        hitInstance = event.getPlaceInstance();
        manager.stopSimulation();
        manager.informHitBreakpoint(this);
    }

    public void markingChanged(PlaceEvent event) {
        switch (reactOn) {
        case Breakpoint.MARKINGCHANGE:
        case Breakpoint.MARKINGCHANGENOTEST:
            hitBP(event);
        default:
        }
    }

    public void tokenAdded(TokenEvent event) {
        switch (reactOn) {
        case Breakpoint.MARKINGCHANGE:
        case Breakpoint.MARKINGCHANGENOTEST:
        case Breakpoint.TOKENADDED:
            hitBP(event);
        default:
        }
    }

    public void tokenRemoved(TokenEvent event) {
        switch (reactOn) {
        case Breakpoint.MARKINGCHANGE:
        case Breakpoint.MARKINGCHANGENOTEST:
        case Breakpoint.TOKENREMOVED:
            hitBP(event);
        default:
        }
    }

    public void tokenTested(TokenEvent event) {
        switch (reactOn) {
        case Breakpoint.MARKINGCHANGE:
        case Breakpoint.TOKENTESTCHANGE:
            hitBP(event);
        default:
        }
    }

    public void tokenUntested(TokenEvent event) {
        switch (reactOn) {
        case Breakpoint.MARKINGCHANGE:
        case Breakpoint.TOKENTESTCHANGE:
            hitBP(event);
        default:
        }
    }

    // implementation of de.renew.gui.Breakpoint interface
    public Object getTaggedElement() {
        return producer;
    }

    public Object getHitElement() {
        return hitInstance;
    }

    public Net getTaggedNet() {
        return net;
    }

    public void release() {
        producer.removePlaceEventListener(this);
        this.manager = null;
        this.producer = null;
        this.net = null;
        this.hitInstance = null;
    }

    public String toString() {
        return description;
    }
}