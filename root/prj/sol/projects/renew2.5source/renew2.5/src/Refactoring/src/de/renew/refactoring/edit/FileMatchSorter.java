package de.renew.refactoring.edit;

import de.renew.refactoring.match.FileMatch;

import java.io.File;


/**
 * Sorts {@link FileMatch} objects to an order that allows renaming them.
 *
 * @author 2mfriedr
 */
public class FileMatchSorter extends MatchSorter<FileMatch, FileAndLine> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FileMatchSorter.class);

    @Override
    protected FileAndLine group(FileMatch match) {
        return new FileAndLine(match.getFile(), match.getLine());
    }
}

/**
 * Wraps a pair of a file and line number.
 *
 * @author 2mfriedr
 */
class FileAndLine {
    private final File _file;
    private final int _line;

    public FileAndLine(final File file, final int line) {
        _file = file;
        _line = line;
    }

    public File getFile() {
        return _file;
    }

    public int getLine() {
        return _line;
    }

    @Override
    public int hashCode() {
        return _file.hashCode() ^ _line;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileAndLine) {
            FileAndLine other = (FileAndLine) obj;
            return getFile().equals(other.getFile())
                   && getLine() == other.getLine();
        }
        return false;
    }
}