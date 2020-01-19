package de.renew.shadow;

public class ShadowInscription extends ShadowNetElement {
    static final long serialVersionUID = -8570259322392235092L;
    public final ShadowInscribable inscribable;
    public final String inscr;
    boolean trace;
    boolean special;

    public ShadowInscription(ShadowInscribable inscribable, String inscr) {
        super(inscribable.getNet());
        this.inscribable = inscribable;
        this.inscr = inscr;
        inscribable.add(this);
    }

    public void setTrace(boolean trace) {
        if (this.trace != trace) {
            this.trace = trace;
        }
    }

    public boolean getTrace() {
        return trace;
    }

    public void setSpecial(boolean special) {
        if (this.special != special) {
            this.special = special;
        }
    }

    public boolean isSpecial() {
        return special;
    }

    public void discard() {
        inscribable.remove(this);
        super.discard();
    }

    public String toString() {
        if (inscr == null) {
            return "ShadowInscription (" + getID() + ") an " + inscribable;
        } else {
            return "ShadowInscription \"" + inscr + "\" an " + inscribable;
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method, additionally re-registers the inscription at its
     * inscribed net element.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        inscribable.add(this);
    }
}