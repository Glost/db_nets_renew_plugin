package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Drawing search range implementation that finds drawings that are opened
 * in the gui.
 *
 * @author 2mfriedr
 */
public class OpenedDrawingsSearchRange implements DrawingSearchRange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(OpenedDrawingsSearchRange.class);
    private final int _numberOfDrawings;
    private final Iterator<Drawing> _drawings;

    /**
     * Constructs an opened drawings search range with a list of drawing
     * filename extensions.
     *
     * @param extensions a list of filename extensions
     */
    public OpenedDrawingsSearchRange(final List<String> extensions) {
        List<Drawing> drawings = GuiDrawings.guiDrawings(extensions);
        _numberOfDrawings = drawings.size();
        _drawings = drawings.iterator();
    }

    public OpenedDrawingsSearchRange(final String extension) {
        this(Collections.singletonList(extension));
    }

    @Override
    public String description() {
        return "Opened drawings";
    }

    @Override
    public int numberOfDrawings() {
        return _numberOfDrawings;
    }

    @Override
    public Iterator<Drawing> drawings() {
        return _drawings;
    }
}