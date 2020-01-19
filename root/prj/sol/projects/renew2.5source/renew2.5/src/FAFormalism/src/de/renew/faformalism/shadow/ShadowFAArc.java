package de.renew.faformalism.shadow;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNode;


public class ShadowFAArc extends ShadowNode {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ShadowFAArc.class);
    public ShadowFAState src;
    public ShadowFAState dest;
    private boolean trace;

    /**
     * Defines a shadow arc for an FA from a shadow state
     * to another.
     * @param from                the start state of the arc
     * @param to                the end state of the arc
     */
    public ShadowFAArc(ShadowFAState from, ShadowFAState to) {
        super(ensureIdentity(from, to));

        src = from;
        dest = to;

        // The states should remember the Arc
        src.add(this);
        dest.add(this);
    }

    /**
     * Returns the net this arc identifies with.
     * Both given states determine this net.
     *
     * @param from  the state, the arc is coming from
     * @param to  the state, the arc is going to
     * @return  the net of the arc
     */
    private static ShadowNet ensureIdentity(ShadowFAState from, ShadowFAState to) {
        if ((from == null) | (to == null)) {
            throw new RuntimeException("Arc must be connected at both ends.");
        }

        // get from-net
        ShadowNet net = (from != null ? from.getNet() : null);

        // compare to to-net
        if (to != null && net != to.getNet()) {
            throw new RuntimeException("Must connect within one net.");
        }

        return net;
    }

    @Override
    public void discard() {
        logger.debug("discard() called by " + this);
        src.remove(this);
        dest.remove(this);

        super.discard();
    }

    @Override
    public String toString() {
        return super.toString() + "  [(" + src.getID() + ") -> ("
               + dest.getID() + ")]";
    }

    @Override
    public void setTrace(boolean trace) {
        if (this.trace != trace) {
            this.trace = trace;
        }
    }

    @Override
    public boolean getTrace() {
        return trace;
    }

    //TODO: readObject
}