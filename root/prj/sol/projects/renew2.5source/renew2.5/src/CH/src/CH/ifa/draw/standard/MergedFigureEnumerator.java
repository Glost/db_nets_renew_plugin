package CH.ifa.draw.standard;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;


/**
 * Merges two figure enumerations into one, so that all elements
 * of both enumerations are traversed.
 * <p>
 * </p>
 * MergedFigureEnumerator.java
 * Created: Thu Mar  1  2001
 * @author Michael Duvigneau
 */
public class MergedFigureEnumerator implements FigureEnumeration {
    private FigureEnumeration enumA;
    private FigureEnumeration enumB;

    public MergedFigureEnumerator(FigureEnumeration enumA,
                                  FigureEnumeration enumB) {
        if (enumA == null || enumB == null) {
            throw new NullPointerException("Enumeration may not be null!");
        }

        this.enumA = enumA;
        this.enumB = enumB;
    }

    public boolean hasMoreElements() {
        return enumA.hasMoreElements() || enumB.hasMoreElements();
    }

    public Figure nextElement() {
        if (enumA.hasMoreElements()) {
            return enumA.nextElement();
        } else {
            // There is no need to check if enumB has
            // more elements, as we would have to throw
            // the same exception anyway.
            return enumB.nextElement();
        }
    }

    public Figure nextFigure() {
        Figure fig = nextElement();
        return fig;
    }
}