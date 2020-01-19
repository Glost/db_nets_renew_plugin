package de.renew.formalism.fs;

import de.renew.shadow.ShadowNetElement;


public class ShadowIsa extends ShadowNetElement {
    private ShadowConcept from;
    private ShadowConcept to;
    private boolean isDisjunctive;

    public ShadowIsa(ShadowConcept from, ShadowConcept to) {
        this(from, to, false);
    }

    public ShadowIsa(ShadowConcept from, ShadowConcept to, boolean isDisjunctive) {
        super(from.getNet());
        this.from = from;
        this.to = to;
        this.isDisjunctive = isDisjunctive;
        from.add(this);
    }

    public ShadowConcept getSource() {
        return from;
    }

    public ShadowConcept getTarget() {
        return to;
    }

    public boolean isDisjunctive() {
        return isDisjunctive;
    }

    /*
      public void discard() {
        super.discard();
        // is there anything else to do here?
      }
    */


    /**
     * Deserialization method, behaves like default readObject
     * method, additionally re-registers the Isa at its
     * <code>from</code> concept.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        from.add(this);
    }
}