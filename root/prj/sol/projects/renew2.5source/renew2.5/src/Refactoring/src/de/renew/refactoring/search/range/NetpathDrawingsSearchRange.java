package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import de.renew.refactoring.util.DrawingHelper;

import java.io.File;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Drawing search range implementation that finds drawings with specified
 * filename extensions in the netpath.
 *
 * @author 2mfriedr
 */
public class NetpathDrawingsSearchRange implements DrawingSearchRange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(NetpathDrawingsSearchRange.class);
    private final int _numberOfDrawings;
    private final Iterator<Drawing> _drawings;

    /**
     * Constructs a netpath drawings search range with a list of filename
     * extensions.
     *
     * @param extensions the filename extensions
     */
    public NetpathDrawingsSearchRange(final List<String> extensions) {
        List<Drawing> openedDrawings = DrawingHelper.toDrawingList(new OpenedDrawingsSearchRange(extensions)
                                                                   .drawings());
        List<File> netpathDrawings = NetpathFiles.files(extensions, "");
        List<Object> drawings = DrawingHelper.mergeDrawingLists(openedDrawings,
                                                                netpathDrawings);

        _numberOfDrawings = drawings.size();
        _drawings = new MakeDrawingIterator(drawings);
    }

    /**
     * Constructs a netpath drawings search range with a single filename
     * extension.
     *
     * @param extension the filename extension
     */
    public NetpathDrawingsSearchRange(final String extension) {
        this(Collections.singletonList(extension));
    }

    @Override
    public String description() {
        return "Netpath drawings";
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