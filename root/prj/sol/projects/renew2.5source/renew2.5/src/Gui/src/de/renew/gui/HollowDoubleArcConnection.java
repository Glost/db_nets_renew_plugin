/*
 * @(#)HollowDoubleArcConnection.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.util.ColorMap;

import de.renew.shadow.ShadowArc;


public class HollowDoubleArcConnection extends ArcConnection {
    public static final HollowDoubleArcConnection HollowArc = new HollowDoubleArcConnection();

    public HollowDoubleArcConnection() {
        setFillColor(ColorMap.BACKGROUND);

        fArrowTipClass = DoubleArrowTip.class;
        setStartDecoration(null);
        setEndDecoration();
    }

    protected void setStartDecoration() {
        setStartDecoration(new DoubleArrowTip(0.40, 8, 8, false));
    }

    protected void setEndDecoration() {
        setEndDecoration(new DoubleArrowTip(0.40, 8, 8, false));
    }

    public int getArcType() {
        return ShadowArc.doubleHollow;
    }

    public boolean canConnect(Figure start, Figure end) {
        return (start instanceof PlaceFigure && end instanceof TransitionFigure);
    }

    public void setAttribute(String name, Object value) {
        if (name.equals("ArrowMode")) {
            // Ignore. Not yet implemented.
        } else {
            super.setAttribute(name, value);
        }
    }
}