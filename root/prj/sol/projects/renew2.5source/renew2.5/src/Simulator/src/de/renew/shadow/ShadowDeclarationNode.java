package de.renew.shadow;

public class ShadowDeclarationNode extends ShadowNetElement {
    static final long serialVersionUID = 687490760763472293L;
    public final String inscr;

    public ShadowDeclarationNode(ShadowNet shadowNet, String inscr) {
        super(shadowNet);
        this.inscr = inscr;
    }
}