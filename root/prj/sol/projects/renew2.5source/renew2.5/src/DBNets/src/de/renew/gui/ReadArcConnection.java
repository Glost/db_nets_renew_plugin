package de.renew.gui;

import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.dbnets.shadow.node.ShadowReadArc;
import de.renew.dbnets.shadow.node.ShadowViewPlace;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.util.Objects;

public class ReadArcConnection extends ArcConnection {

    public static final ReadArcConnection READ_ARC_CONNECTION = new ReadArcConnection();

    private transient ShadowReadArc shadow;

    public ReadArcConnection() {
        setStartDecoration(null);
        setEndDecoration(null);
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
        shadow = new ShadowReadArc(((ShadowViewPlace) startShadow()), ((ShadowDBNetTransition) endShadow()));
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        return shadow;
    }

    @Override
    public ShadowReadArc getShadow() {
        return shadow;
    }

    @Override
    public int getArcType() {
        return ShadowReadArc.READ_ARC;
    }
}
