package de.renew.gui;

import CH.ifa.draw.framework.Figure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;


public interface ShadowHolder extends Figure {

    /** Build a shadow in the given shadow net.
      *  This shadow is stored as well as returned.
      */
    public ShadowNetElement buildShadow(ShadowNet net);

    /** Get the associated shadow, if any.
     */
    public ShadowNetElement getShadow();
}