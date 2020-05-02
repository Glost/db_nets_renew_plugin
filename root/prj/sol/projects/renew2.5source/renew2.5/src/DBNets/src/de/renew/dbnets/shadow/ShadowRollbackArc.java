package de.renew.dbnets.shadow;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowPlace;

public class ShadowRollbackArc extends ShadowArc {

    public static final int ROLLBACK_ARC = 1007;

    public ShadowRollbackArc(ShadowDBNetTransition from, ShadowPlace to) {
        super(from, to, ROLLBACK_ARC);
    }

    public ShadowRollbackArc(ShadowDBNetTransition from, ShadowPlace to, String inscription) {
        super(from, to, ROLLBACK_ARC, inscription);
    }
}
