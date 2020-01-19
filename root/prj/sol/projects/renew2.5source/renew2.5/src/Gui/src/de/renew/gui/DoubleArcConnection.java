/*
 * @(#)InhibitorConnection.java 5.1
 *
 */
package de.renew.gui;

import de.renew.shadow.ShadowArc;


public class DoubleArcConnection extends ArcConnection {
    public static final DoubleArcConnection DoubleArc = new DoubleArcConnection();

    public DoubleArcConnection() {
        fArrowTipClass = DoubleArrowTip.class;
        setStartDecoration(null);
        setEndDecoration();
    }

    protected void setStartDecoration() {
        setStartDecoration(new DoubleArrowTip());
    }

    protected void setEndDecoration() {
        setEndDecoration(new DoubleArrowTip());
    }

    public int getArcType() {
        return ShadowArc.doubleOrdinary;
    }

    public void setAttribute(String name, Object value) {
        if (name.equals("ArrowMode")) {
            Integer intObj = (Integer) value;
            if (intObj != null) {
                int decoration = intObj.intValue();

                switch (decoration) {
                case ARROW_TIP_START:
                    setStartDecoration(new DoubleArrowTip());
                    setEndDecoration(null);
                    changed();
                    break;
                case ARROW_TIP_END:
                    setStartDecoration(null);
                    setEndDecoration(new DoubleArrowTip());
                    changed();
                    break;
                }
            }
        } else {
            super.setAttribute(name, value);
        }
    }
}