package de.renew.gui;

import de.renew.dbnets.shadow.ShadowDBNet;
import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.util.Objects;

public class DBNetTransitionFigure extends TransitionFigure {

    private transient ShadowDBNetTransition shadow;

    @Override
    public void release() {
        super.release();
        if (Objects.nonNull(shadow)) {
            shadow.discard();
        }
    }

    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowDBNetTransition(net);
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        logger.debug("DB-net transition shadow created");
        return shadow;
    }

    @Override
    public ShadowDBNetTransition getShadow() {
        return shadow;
    }
}
