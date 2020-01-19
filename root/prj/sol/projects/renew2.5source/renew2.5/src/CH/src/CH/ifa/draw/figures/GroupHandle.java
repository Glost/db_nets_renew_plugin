/*
 * @(#)GroupHandle.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.NullHandle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;


/**
 * A Handle for a GroupFigure.
 */
public final class GroupHandle extends NullHandle {
    public GroupHandle(Figure owner, Locator locator) {
        super(owner, locator);
    }

    /**
     * Draws the Group handle.
     */
    public void draw(Graphics g) {
        Rectangle r = displayBox();

        g.setColor(Color.black);
        g.drawRect(r.x, r.y, r.width, r.height);
        r.grow(-1, -1);
        g.setColor(Color.white);
        g.drawRect(r.x, r.y, r.width, r.height);
    }
}