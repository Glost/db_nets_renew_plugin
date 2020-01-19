/*
 * @(#)FilteredFigureEnumerator.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureFilter;

import java.util.NoSuchElementException;


/**
 * An Enumeration for a Vector of Figures.
 */
public final class FilteredFigureEnumerator implements FigureEnumeration {
    FigureEnumeration fEnumeration;
    FigureFilter fFilter;
    private Figure head;

    public FilteredFigureEnumerator(FigureEnumeration enumeration,
                                    FigureFilter filter) {
        fEnumeration = enumeration;
        fFilter = filter;
        next();
    }

    private void next() {
        while (fEnumeration.hasMoreElements()) {
            head = fEnumeration.nextFigure();
            if (fFilter.isUsed(head)) {
                return;
            }
        }
        head = null;
    }

    /**
     * Returns true if the enumeration contains more elements; false
     * if it is empty.
     */
    public boolean hasMoreElements() {
        return head != null;
    }

    /**
     * Returns the next element of the enumeration. Calls to this
     * method will enumerate successive elements.
     * @exception NoSuchElementException If no more elements exist.
     */
    public Figure nextElement() {
        return nextFigure();
    }

    /**
     * Returns the next element of the enumeration. Calls to this
     * method will enumerate successive elements.
     * @exception NoSuchElementException If no more elements exist.
     */
    public Figure nextFigure() {
        Figure last = head;
        next();
        return last;
    }
}