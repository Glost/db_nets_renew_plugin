package CH.ifa.draw.framework;



/**
 * Tags figures which depend on other figures or
 * are needed by other figures. The information from
 * this interface is used to expand the selection
 * when cut, copy or duplicate facilities are used.
 * <p>
 * </p>
 * FigureWithDependencies.java
 * Created: Thu Mar  1  2001
 * @author Michael Duvigneau
 **/
public interface FigureWithDependencies extends Figure {

    /**
     * Returns all figures with dependencies to or from
     * this one.
     *
     * @return enumeration of all figures which depend on
     *         this figure or on which this figure depends.
     *         The enumeration may contain the same figure
     *         twice or even <code>null</code>s.
     **/
    FigureEnumeration getFiguresWithDependencies();
}