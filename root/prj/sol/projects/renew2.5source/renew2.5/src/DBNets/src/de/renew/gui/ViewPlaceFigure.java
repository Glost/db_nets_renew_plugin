package de.renew.gui;

import de.renew.gui.PlaceFigure;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;

import java.util.Objects;

public class ViewPlaceFigure extends PlaceFigure {

    private transient ShadowPlace shadow;

    @Override
    public void release() {
        super.release();
        if (Objects.nonNull(shadow)) {
            shadow.discard();
        }
    }

    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowPlace(net);
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        logger.debug("View place created");
        return shadow;
    }

    @Override
    public ShadowPlace getShadow() {
        return shadow;
    }
}
