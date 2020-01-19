package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import java.util.Arrays;
import java.util.List;


/**
 * Provides default search ranges.
 *
 * @author 2mfriedr
 */
public class SearchRanges {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SearchRanges.class);

    /**
     * Returns the default search ranges for net drawings.
     *
     * @param currentDrawing the current drawing, i.e. the drawing that is
     * searched when the "this drawing" search range is selected
     * @return a list of search ranges
     */
    public static List<DrawingSearchRange> netDrawingSearchRanges(final Drawing currentDrawing) {
        return Arrays.asList(new SingleDrawingSearchRange(currentDrawing),
                             new OpenedDrawingsSearchRange("rnw"),
                             new NetpathDrawingsSearchRange("rnw"));
    }
}