/*
 * @(#)BoxHandleKit.java 5.1
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.LocatorHandle;
import CH.ifa.draw.standard.RelativeLocator;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.Vector;


/**
 * A set of utility methods to create Handles for the common
 * locations on a figure's display box.
 * @see CH.ifa.draw.framework.Handle
 */


// TBD: use anonymous inner classes (had some problems with JDK 1.1)
public class VSplitFigureBoxHandleKit {

    /**
     * Fills the given Vector with handles at each corner of a
     * figure.
     */
    static public void addCornerHandles(Figure f, Vector<Handle> handles) {
        handles.addElement(southEast(f));
        handles.addElement(southWest(f));
        handles.addElement(northEast(f));
        handles.addElement(northWest(f));
    }

    /**
     * Fills the given Vector with handles at each corner
     * and the north, south, east, and west of the figure.
     */
    static public void addHandles(Figure f, Vector<Handle> handles) {
        addCornerHandles(f, handles);
        handles.addElement(south(f));
        handles.addElement(north(f));
        handles.addElement(east(f));
        handles.addElement(west(f));
    }

    static public Handle south(Figure owner) {
        return new SouthHandle(owner);
    }

    static public Handle southEast(Figure owner) {
        return new SouthEastHandle(owner);
    }

    static public Handle southWest(Figure owner) {
        return new SouthWestHandle(owner);
    }

    static public Handle north(Figure owner) {
        return new NorthHandle(owner);
    }

    static public Handle northEast(Figure owner) {
        return new NorthEastHandle(owner);
    }

    static public Handle northWest(Figure owner) {
        return new NorthWestHandle(owner);
    }

    static public Handle east(Figure owner) {
        return new EastHandle(owner);
    }

    static public Handle west(Figure owner) {
        return new WestHandle(owner);
    }
}

class NorthEastHandle extends LocatorHandle {
    NorthEastHandle(Figure owner) {
        super(owner, RelativeLocator.northEast());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();

        //        logger.debug("NorthEastHandle");
        owner()
            .displayBox(new Point(Math.min(x, r.x - x + r.x + r.width),
                                  Math.min(r.y + r.height, y)),
                        new Point(Math.max(r.x, x), r.y + r.height));
    }
}

class EastHandle extends LocatorHandle {
    EastHandle(Figure owner) {
        super(owner, RelativeLocator.east());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();

        //        logger.debug("EastHandle");
        //        logger.debug("1 ############# x   "+x);
        //        logger.debug("2 ############# z   "+y);
        //        logger.debug("3 ############# r.x "+r.x);
        //        logger.debug("4 ############# r.y "+r.y);
        //        logger.debug("5 ############# r.w "+r.width);
        owner()
            .displayBox(new Point(Math.min(x, r.x - x + r.x + r.width), r.y),
                        new Point(Math.max(r.x, x), r.y + r.height));
    }
}

class NorthHandle extends LocatorHandle {
    NorthHandle(Figure owner) {
        super(owner, RelativeLocator.north());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();
        owner()
            .displayBox(new Point(r.x, Math.min(r.y + r.height, y)),
                        new Point(r.x + r.width, r.y + r.height));
    }
}

class NorthWestHandle extends LocatorHandle {
    NorthWestHandle(Figure owner) {
        super(owner, RelativeLocator.northWest());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();

        // move the right side as well as the left fixate the center
        owner()
            .displayBox(new Point(Math.min(r.x + r.width, x),
                                  Math.min(r.y + r.height, y)),
                        new Point(Math.max(x, r.x + r.width + r.x - x),
                                  r.y + r.height));
    }
}

class SouthEastHandle extends LocatorHandle {
    SouthEastHandle(Figure owner) {
        super(owner, RelativeLocator.southEast());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();

        //        logger.debug("SouthEastHandle");
        //        logger.debug("1 ############# x   "+x);
        //        logger.debug("2 ############# z   "+y);
        //        logger.debug("3 ############# r.x "+r.x);
        //        logger.debug("4 ############# r.y "+r.y);
        //        logger.debug("5 ############# r.w "+r.width);
        owner()
            .displayBox(new Point(Math.min(x, r.x - x + r.x + r.width), r.y),
                        new Point(Math.max(r.x, x), Math.max(r.y, y)));
    }
}

class SouthHandle extends LocatorHandle {
    SouthHandle(Figure owner) {
        super(owner, RelativeLocator.south());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();
        owner()
            .displayBox(new Point(r.x, r.y),
                        new Point(r.x + r.width, Math.max(r.y, y)));
    }
}

class SouthWestHandle extends LocatorHandle {
    SouthWestHandle(Figure owner) {
        super(owner, RelativeLocator.southWest());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();

        // move the right side as well as the left fixate the center
        owner()
            .displayBox(new Point(Math.min(r.x + r.width, x), r.y),
                        new Point(Math.max(r.x + r.width + r.x - x, x),
                                  Math.max(r.y, y)));
    }
}

class WestHandle extends LocatorHandle {
    WestHandle(Figure owner) {
        super(owner, RelativeLocator.west());
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        Rectangle r = owner().displayBox();

        // move the right side as well as the left fixate the center
        owner()
            .displayBox(new Point(Math.min(r.x + r.width, x), r.y),
                        new Point(Math.max(x, r.x + r.width + r.x - x),
                                  r.y + r.height));
    }
}