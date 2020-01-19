/*
 * @(#)ParentFigure.java 5.1
 *
 */
package CH.ifa.draw.framework;

public interface ParentFigure extends FigureWithDependencies {
    public FigureEnumeration children();

    public void addChild(ChildFigure child);

    public void removeChild(ChildFigure child);
}