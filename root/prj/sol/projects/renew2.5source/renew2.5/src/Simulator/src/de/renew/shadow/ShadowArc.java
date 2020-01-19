package de.renew.shadow;

public class ShadowArc extends ShadowInscribable {
    static final long serialVersionUID = -774253458615347237L;
    public static final int test = 0;
    public static final int ordinary = 1;
    public static final int both = 2;
    public static final int inhibitor = 3;
    public static final int doubleOrdinary = 4;
    public static final int doubleHollow = 5;
    public ShadowTransition transition;
    public ShadowPlace place;
    private boolean trace;

    /** The arc type is determined by the constants given above. */
    public int shadowArcType;
    public boolean placeToTransition;

    /**
     * Defines a shadow arc from one shadow node to another.
     * An arc can only connect a shadow transition and a
     * shadow place or vice versa.
     * @param from          the start node of the shadow arc
     * @param to            the end node of the shadow arc
     * @param shadowArcType the type of the shadow arc: One of
     *                      the constants <tt>test</tt>,
     *                      <tt>ordinary</tt>, or <tt>both</tt>
     */
    public ShadowArc(ShadowNode from, ShadowNode to, int shadowArcType) {
        super(ensureIdentity(from, to));

        if (from instanceof ShadowTransition) {
            if (to instanceof ShadowPlace) {
                transition = (ShadowTransition) from;
                place = (ShadowPlace) to;
                placeToTransition = false;
            } else {
                throw new RuntimeException("Must connect place and transition.");
            }
        } else if (from instanceof ShadowPlace) {
            if (to instanceof ShadowTransition) {
                place = (ShadowPlace) from;
                transition = (ShadowTransition) to;
                placeToTransition = true;
            } else {
                throw new RuntimeException("Must connect place and transition.");
            }
        } else {
            throw new RuntimeException("Must connect place and transition.");
        }

        this.shadowArcType = shadowArcType;

        trace = true;


        // Ok, remember the inscription.
        transition.add(this);
        place.add(this);
    }

    public ShadowArc(ShadowNode from, ShadowNode to) {
        this(from, to, ordinary);
    }

    // Convenience constructor for arcs with inscription.
    public ShadowArc(ShadowNode from, ShadowNode to, int shadowArcType,
                     String inscription) {
        this(from, to, shadowArcType);
        new ShadowInscription(this, inscription);
    }

    // Convenience constructor for ordinary arcs with inscription.
    public ShadowArc(ShadowNode from, ShadowNode to, String inscription) {
        this(from, to, ordinary, inscription);
    }

    private static ShadowNet ensureIdentity(ShadowNode from, ShadowNode to) {
        if ((from == null) | (to == null)) {
            throw new RuntimeException("Arc must be connected at both ends.");
        }

        //NOTICEnull
        ShadowNet net = from != null ? from.getNet() : null;
        if (to != null && net != to.getNet()) {
            throw new RuntimeException("Must connect within one net.");
        }
        return net;
    }

    public void setTrace(boolean trace) {
        if (this.trace != trace) {
            this.trace = trace;
        }
    }

    public boolean getTrace() {
        return trace;
    }

    public void discard() {
        transition.remove(this);
        place.remove(this);
        super.discard();
    }

    public String toString() {
        return "ShadowArc (type " + shadowArcType + ", " + transition
               + (placeToTransition ? " <- " : " -> ") + place + ")";
    }

    /**
     * Deserialization method, behaves like default readObject
     * method, additionally re-registers the arc at its nodes.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        transition.add(this);
        place.add(this);
    }
}