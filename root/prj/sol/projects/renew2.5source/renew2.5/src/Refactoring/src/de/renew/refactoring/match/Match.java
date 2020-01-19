package de.renew.refactoring.match;



/**
 * Parent class for arbitrary string matches.
 *
 * @author 2mfriedr
 */
public class Match {
    protected final StringMatch _stringMatch;

    /**
     * Constructs a Match object.
     *
     * @param stringMatch the string match
     */
    public Match(final StringMatch stringMatch) {
        _stringMatch = stringMatch;
    }

    /**
     * Returns the match's start index.
     *
     * @return the start index
     */
    public int getStart() {
        return _stringMatch.start();
    }

    /**
     * Returns the match's end index.
     *
     * @return the end index
     */
    public int getEnd() {
        return _stringMatch.end();
    }

    /**
     * Returns the matched string.
     *
     * @return the match
     */
    public String getMatch() {
        return _stringMatch.match();
    }

    @Override
    public int hashCode() {
        return _stringMatch.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Match) {
            Match other = (Match) obj;
            return _stringMatch.equals(other._stringMatch);
        }
        return false;
    }
}