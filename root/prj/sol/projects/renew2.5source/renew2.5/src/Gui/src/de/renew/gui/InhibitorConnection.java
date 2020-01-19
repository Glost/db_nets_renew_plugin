/*
 * @(#)InhibitorConnection.java 5.1
 *
 */
package de.renew.gui;

import de.renew.shadow.ShadowArc;


public class InhibitorConnection extends ArcConnection {
    public static final InhibitorConnection InhibitorArc = new InhibitorConnection();

    public InhibitorConnection() {
        fArrowTipClass = CircleDecoration.class;
        setStartDecoration(new CircleDecoration());
        setEndDecoration(new CircleDecoration());
    }

    public int getArcType() {
        return ShadowArc.inhibitor;
    }

    public void setAttribute(String name, Object value) {
        // Inhibitor arcs do not have a directions, hence it does not
        // make sense to change this attribute. Moreover, setting
        // this attribute could corrupt the graphical decorations.
        if (!name.equals("ArrowMode")) {
            super.setAttribute(name, value);
        }
    }
}