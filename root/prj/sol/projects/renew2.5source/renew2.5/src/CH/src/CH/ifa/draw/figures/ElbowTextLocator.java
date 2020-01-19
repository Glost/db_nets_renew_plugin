/*
 * @(#)ElbowTextLocator.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractLocator;

import java.awt.Point;


public class ElbowTextLocator extends AbstractLocator {
    public Point locate(Figure owner) {
        Point p = owner.center();
        return new Point(p.x, p.y - 10); // hack
    }
}