package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import java.util.Iterator;


/**
 * Interface for drawing search ranges.
 *
 * @author 2mfriedr
 */
public interface DrawingSearchRange extends SearchRange {

    /**
     * Returns the number of drawings in the search range.
     *
     * @return the number of drawings
     */
    public int numberOfDrawings();

    /**
     * Returns an iterator of all drawings in the search range.
     *
     * @return an iterator
     */
    public Iterator<Drawing> drawings();
}