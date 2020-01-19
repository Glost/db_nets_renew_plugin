package de.renew.shadow;

public class ShadowPlace extends ShadowNode {
    static final long serialVersionUID = -5586049092512348025L;

    // A shadow place is really just a specialized shadow node.
    public ShadowPlace(ShadowNet shadowNet) {
        super(shadowNet);
    }
}