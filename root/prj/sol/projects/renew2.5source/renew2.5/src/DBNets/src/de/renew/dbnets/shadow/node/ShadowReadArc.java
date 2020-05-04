package de.renew.dbnets.shadow.node;

import de.renew.shadow.ShadowArc;

public class ShadowReadArc extends ShadowArc {

    public static final int READ_ARC = 1006;

    public ShadowReadArc(ShadowViewPlace from, ShadowDBNetTransition to) {
        super(from, to, READ_ARC);
    }

    public ShadowReadArc(ShadowViewPlace from, ShadowDBNetTransition to, String inscription) {
        super(from, to, READ_ARC, inscription);
    }
}
