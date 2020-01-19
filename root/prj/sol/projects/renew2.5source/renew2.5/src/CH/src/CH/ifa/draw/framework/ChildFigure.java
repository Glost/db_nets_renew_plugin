/*
 * @(#)ParentFigure.java 5.1
 *
 */
package CH.ifa.draw.framework;

public interface ChildFigure extends FigureWithDependencies, FigureChangeListener {

    /**
     * Gets the parent Figure or null if top-level figure.
     */
    public ParentFigure parent();

    /**
     * Sets a new parent Figure or removes parent if null.
     * Returns whether the setting was performed.
     */
    public boolean setParent(ParentFigure newParent);

    /**
     * Returns whether the given ParentFigure can become
     * a new parent of this ChildFigure.
     * Should be called by setParent().
     */
    public boolean canBeParent(ParentFigure parent);

    public void updateLocation();
}