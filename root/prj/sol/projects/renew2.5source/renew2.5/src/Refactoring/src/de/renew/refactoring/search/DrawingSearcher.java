package de.renew.refactoring.search;

import CH.ifa.draw.framework.Drawing;

import de.renew.refactoring.match.TextFigureMatch;
import de.renew.refactoring.search.range.DrawingSearchRange;

import java.util.List;


/**
 * Searches multiple drawings and returns {@link TextFigureMatch} objects.
 * Subclasses need to override {@link #searchDrawing(Drawing)}.
 *
 * @author 2mfriedr
 */
public abstract class DrawingSearcher<T extends TextFigureMatch>
        extends IteratorSearcher<Drawing, List<T>> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(DrawingSearcher.class);

    /**
     * Constructs a new DrawingSearcher.
     *
     * @param searchRange the search range
     */
    public DrawingSearcher(final DrawingSearchRange searchRange) {
        super(searchRange.drawings(), searchRange.numberOfDrawings());
    }

    /**
     * Returns the current drawing name.
     */
    @Override
    public String getCurrentItemString() {
        return getCurrentItem().getName();
    }

    @Override
    protected List<T> searchItem(Drawing item) {
        return searchDrawing(item);
    }

    /**
     * Override point for subclasses. This method is called from {@link
     * #searchNextItem()}.
     *
     * @param drawing the drawing to be searched
     * @return the resulting matches
     */
    protected abstract List<T> searchDrawing(Drawing drawing);
}