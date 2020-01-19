package de.renew.refactoring.search;

import de.renew.refactoring.match.FileMatch;
import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.search.range.FileSearchRange;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Abstract superclass for simple file searchers that search a file line by
 * line. This class handles i/o operations and leaves subclasses to implement
 * {@link #searchLine(String)}.
 *
 * @author 2mfriedr
 */
public abstract class LineByLineFileSearcher extends IteratorSearcher<File, List<FileMatch>> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LineByLineFileSearcher.class);

    /**
     * Constructs a new Line by line file searcher.
     *
     * @param searchRange the search range
     */
    public LineByLineFileSearcher(final FileSearchRange searchRange) {
        super(searchRange.files(), searchRange.numberOfFiles());
    }

    @Override
    protected List<FileMatch> searchItem(final File file) {
        logger.debug("Searching file: " + file);
        newFile();
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            List<FileMatch> result = new ArrayList<FileMatch>();
            int lineNumber = 0;
            while (scanner.hasNext()) {
                lineNumber += 1;
                String line = scanner.nextLine();
                for (StringMatch match : searchLine(line)) {
                    result.add(new FileMatch(file, lineNumber, match));
                    logger.debug("Found a match in line " + lineNumber);
                }
            }
            return result;
        } catch (FileNotFoundException e) {
            logger.error("File not found: " + file);
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return null;
    }

    @Override
    public String getCurrentItemString() {
        return getCurrentItem().getName();
    }

    /**
     * Searches a line.
     *
     * @param line the line
     * @return a list of string matches
     */
    protected abstract List<StringMatch> searchLine(String line);

    /**
     * Override point for subclasses. This method is called when search of a
     * new file begins.
     */
    protected void newFile() {
    }
}