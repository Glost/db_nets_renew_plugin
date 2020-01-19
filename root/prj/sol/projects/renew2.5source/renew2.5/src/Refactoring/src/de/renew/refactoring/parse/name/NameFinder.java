package de.renew.refactoring.parse.name;

import de.renew.refactoring.match.StringMatch;

import java.util.List;


/**
 * Interface for name finders.
 *
 * @author 2mfriedr
 */
public interface NameFinder {

    /**
     * Checks if a specified name occurs in an input string.
     *
     * @param input the input string
     * @return {@code true} if the name was found in the input string,
     * otherwise {@code false}
     */
    public boolean find(String input);

    /**
     * Returns a list of matches of a specified name in an input string.
     *
     * @param input the input string
     * @return a list of {@link StringMatch} objects that may be empty if the
     * name was not found in the input string
     */
    public List<StringMatch> listOfMatches(String input);
}