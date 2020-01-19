package de.renew.refactoring.match;

import java.io.File;


/**
 * Describes a match in a file.
 *
 * @author 2mfriedr
 */
public class FileMatch extends Match {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FileMatch.class);
    private final File _file;
    private final int _line;

    /**
     * Constructs a FileMatch with a file, line number and StringMatch.
     *
     * @param file the file
     * @param line the line
     * @param stringMatch the match
     */
    public FileMatch(final File file, final int line,
                     final StringMatch stringMatch) {
        super(stringMatch);
        _file = file;
        _line = line;
    }

    /**
     * Returns the file.
     *
     * @return the file
     */
    public File getFile() {
        return _file;
    }

    /**
     * Returns the line number.
     *
     * @return the line number
     */
    public int getLine() {
        return _line;
    }

    @Override
    public String toString() {
        return "FileMatch<" + _file + ", Line " + _line + ", " + _stringMatch
               + ">";
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ _file.hashCode() ^ _line;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof FileMatch) {
            FileMatch other = (FileMatch) obj;
            return getFile().equals(other.getFile())
                   && getLine() == other.getLine();
        }
        return false;
    }
}