package de.renew.diagram;

import CH.ifa.draw.contrib.DiamondFigure;

import java.awt.Rectangle;


/**
 * @author Lawrence Cabac
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MiniDiamondFigure extends DiamondFigure {

    /**
     * @see CH.ifa.draw.framework.Figure#displayBox()
     */
    public Rectangle displayBox() {
        Rectangle r = super.displayBox();
        r.setSize(16, 16);
        return r;
    }
}