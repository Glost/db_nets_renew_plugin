package de.renew.refactoring.parse.name;

import de.renew.refactoring.match.StringMatch;

import java.util.Collections;
import java.util.List;


/**
 * Convenience name finder implementation that returns {@code false} on {@link
 * NameFinder#find(String)} and an empty list on {@link
 * NameFinder#listOfMatches(String)}.
 *
 * @author 2mfriedr
 */
public class NoNameFinder implements NameFinder {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(NoNameFinder.class);

    @Override
    public boolean find(String input) {
        return false;
    }

    @Override
    public List<StringMatch> listOfMatches(String input) {
        return Collections.emptyList();
    }
}