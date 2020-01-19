package de.renew.refactoring.match;



/**
 * A StringMatch object represents a slice of a string.
 * It is defined by start and end indices and the matched string.
 *
 * @author 2mfriedr
 */
public class StringMatch {
    private final String _match;
    private final int _start;
    private final int _end;

    public StringMatch(final String match, final int start, final int end) {
        _match = match;
        _start = start;
        _end = end;
    }

    public String match() {
        return _match;
    }

    public int start() {
        return _start;
    }

    public int end() {
        return _end;
    }

    @Override
    public String toString() {
        return "StringMatch<\"" + _match + "\", " + _start + ", " + _end + ">";
    }

    @Override
    public int hashCode() {
        return _match.length() ^ _start ^ _end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringMatch) {
            StringMatch other = (StringMatch) obj;
            return match().equals(other.match()) && start() == other.start()
                   && end() == other.end();
        }
        return false;
    }
}