package de.renew.shadow;

public abstract class ShadowNode extends ShadowInscribable {
    static final long serialVersionUID = 6077849783470486673L;
    private String name;
    private boolean trace;
    private String comment;

    public ShadowNode(ShadowNet shadowNet) {
        super(shadowNet);
        name = null;
        setTrace(true);


        // Register myself with the net.
        shadowNet.add(this);
    }

    public ShadowNet getNet() {
        return shadowNet;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTrace(boolean trace) {
        if (this.trace != trace) {
            this.trace = trace;
        }
    }

    public boolean getTrace() {
        return trace;
    }

    public String toString() {
        String cln = getClass().getName();
        int ind = cln.lastIndexOf('.') + 1;
        if (ind > 0) {
            cln = cln.substring(ind);
        }
        if (name == null) {
            return cln + " (" + getID() + ")";
        } else {
            return cln + " \"" + name + "\"";
        }
    }

    public void setComment(String comment) {
        this.comment = comment;

    }

    public String getComment() {
        return comment;
    }
}