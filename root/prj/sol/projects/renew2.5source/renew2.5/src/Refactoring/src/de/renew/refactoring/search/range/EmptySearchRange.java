package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import java.io.File;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Convenience drawing and file search range implementation that acts as an
 * empty search range.
 *
 * @author 2mfriedr
 */
public class EmptySearchRange implements DrawingSearchRange, FileSearchRange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(EmptySearchRange.class);

    @Override
    public String description() {
        return "Empty";
    }

    @Override
    public int numberOfFiles() {
        return 0;
    }

    @Override
    public Iterator<File> files() {
        List<File> emptyList = Collections.emptyList();
        return emptyList.iterator();
    }

    @Override
    public int numberOfDrawings() {
        return 0;
    }

    @Override
    public Iterator<Drawing> drawings() {
        List<Drawing> emptyList = Collections.emptyList();
        return emptyList.iterator();
    }
}