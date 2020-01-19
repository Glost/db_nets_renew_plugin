package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import java.util.Collections;
import java.util.Iterator;


/**
 * Drawing search range that finds only one specified drawing.
 *
 * @author 2mfriedr
 */
public class SingleDrawingSearchRange implements DrawingSearchRange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SingleDrawingSearchRange.class);
    private final Iterator<Drawing> _drawings;

    /**
     * Constructs a single drawing search range.
     *
     * @param drawing the drawing
     */
    public SingleDrawingSearchRange(final Drawing drawing) {
        _drawings = Collections.singletonList(drawing).iterator();
    }

    @Override
    public String description() {
        return "Current drawing";
    }

    @Override
    public int numberOfDrawings() {
        return 1;
    }

    @Override
    public Iterator<Drawing> drawings() {
        return _drawings;
    }
}