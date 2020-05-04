package de.renew.gui;

import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.dbnets.shadow.node.ShadowRollbackArc;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;

import java.util.Objects;

public class RollbackArcConnection extends ArcConnection {

    public static final RollbackArcConnection ROLLBACK_ARC_CONNECTION = new RollbackArcConnection();

    private transient ShadowRollbackArc shadow;

    public RollbackArcConnection() {
        super(ShadowRollbackArc.ROLLBACK_ARC);
    }

    @Override
    public void release() {
        super.release();
        if (Objects.nonNull(shadow)) {
            shadow.discard();
        }
    }

    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowRollbackArc(((ShadowDBNetTransition) startShadow()), ((ShadowPlace) endShadow()));
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        return shadow;
    }

    @Override
    public ShadowRollbackArc getShadow() {
        return shadow;
    }

    @Override
    public int getArcType() {
        return ShadowRollbackArc.ROLLBACK_ARC;
    }
}
