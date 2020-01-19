package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import java.io.File;

import java.util.Iterator;
import java.util.List;


/**
 * Combines a {@link NetpathDrawingsSearchRange} and a {@link
 * NetpathFilesSearchRange} into a search range that implements both
 * {@link DrawingSearchRange} and {@link FileSearchRange}.
 *
 * @author 2mfriedr
 */
public class NetpathSearchRange implements DrawingSearchRange, FileSearchRange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(NetpathSearchRange.class);
    private final DrawingSearchRange _drawings;
    private final FileSearchRange _files;


    /**
     * Constructs a netpath search range with a list of drawing filename
     * extensions and a list of file filename extensions.
     *
     * @param drawingExtensions
     * @param fileExtensions
     */
    public NetpathSearchRange(final List<String> drawingExtensions,
                              final List<String> fileExtensions) {
        _drawings = new NetpathDrawingsSearchRange(drawingExtensions);
        _files = new NetpathFilesSearchRange(fileExtensions);
    }

    @Override
    public String description() {
        return "Netpath";
    }

    @Override
    public int numberOfDrawings() {
        return _drawings.numberOfDrawings();
    }

    @Override
    public Iterator<Drawing> drawings() {
        return _drawings.drawings();
    }

    @Override
    public int numberOfFiles() {
        return _files.numberOfFiles();
    }

    @Override
    public Iterator<File> files() {
        return _files.files();
    }
}