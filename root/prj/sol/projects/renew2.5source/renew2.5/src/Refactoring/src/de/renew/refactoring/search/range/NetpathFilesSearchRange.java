package de.renew.refactoring.search.range;

import java.io.File;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * File search range implementation that finds files with specified
 * filename extensions in the netpath.
 *
 * @author 2mfriedr
 */
public class NetpathFilesSearchRange implements FileSearchRange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(NetpathFilesSearchRange.class);
    private final int _numberOfFiles;
    private final Iterator<File> _files;

    /**
     * Constructs a netpath files search range with a list of filename
     * extensions.
     *
     * @param extensions the filename extensions
     */
    public NetpathFilesSearchRange(final String extension) {
        this(Collections.singletonList(extension));
    }

    /**
     * Constructs a netpath files search range with a single filename
     * extension.
     *
     * @param extension the filename extension
     */
    public NetpathFilesSearchRange(final List<String> extensions) {
        List<File> files = NetpathFiles.files(extensions, null);
        _numberOfFiles = files.size();
        _files = files.iterator();
    }

    @Override
    public String description() {
        return "Netpath files";
    }

    @Override
    public int numberOfFiles() {
        return _numberOfFiles;
    }

    @Override
    public Iterator<File> files() {
        return _files;
    }
}