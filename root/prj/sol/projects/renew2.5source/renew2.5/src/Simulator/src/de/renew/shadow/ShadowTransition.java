package de.renew.shadow;

public class ShadowTransition extends ShadowNode {
    static final long serialVersionUID = -1038713503047565256L;

    // A shadow transition is really just a specialized shadow node.
    public ShadowTransition(ShadowNet shadowNet) {
        super(shadowNet);
    }
}