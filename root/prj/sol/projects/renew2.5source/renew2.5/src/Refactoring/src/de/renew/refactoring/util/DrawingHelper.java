package de.renew.refactoring.util;

import CH.ifa.draw.framework.Drawing;

import de.renew.util.StringUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Provides helper methods for drawings and drawing iterators.
 *
 * @author 2mfriedr
 */
public class DrawingHelper {

    /**
     * Should not be instantiated
     */
    private DrawingHelper() {
    }

    /**
     * Merges a list of drawings with a list of files. Files whose filenames
     * occur in the list of drawings are excluded from the result.
     *
     * @param drawings the drawings
     * @param files the files
     * @return a list of drawings
     */
    public static List<Object> mergeDrawingLists(final List<Drawing> drawings,
                                                 final List<File> files) {
        List<Object> result = new ArrayList<Object>(drawings);
        for (File file : files) {
            String filename = StringUtil.getFilename(file.getPath());
            if (!containsDrawingWithFileName(drawings, filename)) {
                result.add(file);
            }
        }
        return result;
    }

    /**
     * Checks if a list of drawings contains a drawing with a specified
     * filename.
     *
     * @param drawings the list of drawings
     * @param filename the filename
     * @return {@code true} if the list contains a drawing with the specified
     * filename, otherwise {@code false}
     */
    public static boolean containsDrawingWithFileName(final List<Drawing> drawings,
                                                      final String filename) {
        for (Drawing drawing : drawings) {
            if (drawing.getName().equals(filename)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a drawing iterator to a list by exhausting the iterator and
     * adding all its elements to a new list.
     *
     * @param iterator the iterator
     * @return a list
     */
    public static List<Drawing> toDrawingList(final Iterator<Drawing> iterator) {
        List<Drawing> list = new ArrayList<Drawing>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}