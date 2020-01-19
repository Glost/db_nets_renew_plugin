package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;

import java.io.File;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Iterator implementation that takes {@link Drawing} and {@link File} objects
 * and tries to return {@link Drawing}s based on them.
 *
 * @author 2mfriedr
 */
public class MakeDrawingIterator implements Iterator<Drawing> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(MakeDrawingIterator.class);
    private Iterator<Object> _iterator;

    /**
     * Constructs an iterator with a list of drawings and files.
     *
     * @param objects a list of drawings and files
     */
    public MakeDrawingIterator(final List<Object> objects) {
        _iterator = objects.iterator();
    }

    /**
     * Constructs an iterator with a single drawing.
     *
     * @param drawing a drawing
     */
    public MakeDrawingIterator(final Drawing drawing) {
        this(Collections.singletonList((Object) drawing));
    }

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }

    @Override
    public Drawing next() {
        return makeDrawing(_iterator.next());
    }

    /**
     * Tries to return a {@link Drawing} object based on the input object.
     * {@link Drawing} objects are simply returned, while {@link File} objects
     * are loaded by {@link DrawingFileHelper}.
     *
     * @param object an object to be returned as a drawing
     * @return a drawing, or {@code null}
     */
    private static Drawing makeDrawing(final Object object) {
        if (object instanceof Drawing) {
            return (Drawing) object;
        }
        if (object instanceof File) {
            return DrawingFileHelper.loadDrawing((File) object, null);
        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}