/*
 * Created on Oct 10, 2005
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.Drawing;


/**
 * Allow any kind of Drawing to be automatically layoutable
 * through the automatic layout by implementing this interface.
 * @author cabac
 *
 */
public interface LayoutableDrawing extends Drawing {
    public abstract void fillInGraph(GraphLayout layout);
}