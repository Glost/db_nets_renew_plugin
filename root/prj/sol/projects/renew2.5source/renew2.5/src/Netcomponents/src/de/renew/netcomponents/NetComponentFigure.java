/*
 * Created on Feb 12, 2004
 *
 * A NetComponentFigure is a virtual figure that hats no graphical
 * representation and no other function as to group a couple of other figures
 * into one loosely connected group. Mouse drags are being passed by such a
 * figure on to all its attached figures. Selections are being passed on to one
 * (or some) specific figures.
 */
package de.renew.netcomponents;

import CH.ifa.draw.figures.CompositeAttributeFigure;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureChangeListener;

import java.util.Collection;
import java.util.Iterator;


/**
 * The NetComponentFigure groups a set of net elements.
 * The set can be conveniently moved and selected (double-click) by mouse.
 *
 * @author Lawrence Cabac
 */
@SuppressWarnings("serial")
public class NetComponentFigure extends CompositeAttributeFigure
        implements FigureChangeListener {
    public NetComponentFigure() {
        super();
    }

    /**
     * Group a vector of figures to form a NetComponentFigure.
     *
     * @param c -
     *            a collection of Figures that will be grouped.
     * @return the same collection
     */
    public Collection<Figure> group(Collection<Figure> c) {
        willChange();
        Iterator<Figure> it = c.iterator();
        while (it.hasNext()) {
            Figure figure = it.next();
            if ((!(figure instanceof ChildFigure))
                        || ((ChildFigure) figure).parent() == null) {
                if (!attached.contains(figure)) {
                    attached.add(figure);
                    figure.addFigureChangeListener(this);
                }
            }
        }
        changed();
        return c;
    }

    /**
     * Attach a figure to this NetComponent.
     * @param figure the figure to attach.
     */
    public void attach(Figure figure) {
        if (!attached.contains(figure)) {
            willChange();
            attached.add(figure);
            figure.addFigureChangeListener(this);
            changed();
        }
    }

    /**
     * Releases a collection of figures from this NetComponentFigure
     * by detaching them and removing FigureChangeListeners.
     * Also requests removal of this figure when its empty.
     * @param figures The collection of figures to remove.
     */
    public void releaseFigures(Collection<Figure> figures) {
        willChange();
        for (Figure fig : figures) {
            attached.remove(fig);
            fig.removeFigureChangeListener(this);
        }
        if (removeWhenEmpty && attached.isEmpty() && listener() != null) {
            listener().figureRequestRemove(new FigureChangeEvent(this));
        }
        changed();
    }
}