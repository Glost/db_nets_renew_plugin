package CH.ifa.draw.standard;



/**
 *  SubstringMatcher provides functionality to match
 Substrings. Matches are replaced directly with the replacementString.
 *
 */
public class SubstringMatcher implements StringMatcher {
    private String replaceString;
    private String searchString;

    /**
     * @return the lastposition
     */
    public int getLastposition() {
        return lastposition;
    }

    /**
     * @param lastposition the lastposition to set
     */
    public void setLastposition(int lastposition) {
        this.lastposition = lastposition;
    }

    private int lastposition;

    public SubstringMatcher(String searchString, String replaceString) {
        this.searchString = searchString;
        this.replaceString = replaceString;
    }

    /**
       True iff this.searchString is a substring of compareString.
     */
    public boolean matches(String compareString, boolean ignoreCase) {
        boolean found = false;

        for (int i = 0; i < compareString.length() && !found; i++) {
            found = compareString.regionMatches(ignoreCase, i, searchString, 0,
                                                searchString.length());
            setLastposition(i);
        }

        return found;
    }

    /**
    True iff this.searchString is a substring of compareString
    beginning at or after fromIndex.
    */
    public boolean matches(String compareString, int fromIndex,
                           boolean ignoreCase) {
        boolean found = false;
        for (int i = fromIndex; i < compareString.length() && !found; i++) {
            found = compareString.regionMatches(ignoreCase, i, searchString, 0,
                                                searchString.length());
        }
        return found;
    }

    /** Replaces only the first occurrence
    of this.searchString
    in compareString
    with this.replaceString.
    Ignores case.
    */
    public String replacement(String compareString) {
        String s1 = null;
        String s2 = null;
        boolean found = false;
        final boolean ignoreCase = true;
        for (int i = 0; i < compareString.length() && !found; i++) {
            found = compareString.regionMatches(ignoreCase, i, searchString, 0,
                                                searchString.length());
            if (found) {
                s1 = compareString.substring(0, i);
                s2 = compareString.substring(i + searchString.length(),
                                             compareString.length());

            }
        }
        if (found) {
            return s1 + replaceString + s2;
        } else {
            return compareString;
        }
    }

    /** Replaces only the first occurrence
    of this.searchString
    in compareString
    with this.replaceString, starting from fromIndex, so
    that replacement(s, 0) is equivalent to replacement(s)
    Ignores case.
    */
    public String replacement(String compareString, int fromIndex) {
        String s1 = null;
        String s2 = null;
        boolean found = false;
        final boolean ignoreCase = true;
        for (int i = fromIndex; i < compareString.length() && !found; i++) {
            found = compareString.regionMatches(ignoreCase, i, searchString, 0,
                                                searchString.length());
            if (found) {
                s1 = compareString.substring(0, i);
                s2 = compareString.substring(i + searchString.length(),
                                             compareString.length());

            }
        }

        if (found) {
            return s1 + replaceString + s2;
        } else {
            return compareString;
        }
    }

    /** Like the indexOf Method of String, but ignores case
    */
    public int indexOf(String compareString, int fromIndex) {
        final boolean ignoreCase = true;
        for (int i = fromIndex; i < compareString.length(); i++) {
            if (compareString.regionMatches(ignoreCase, i, searchString, 0,
                                                    searchString.length())) {
                return i;
            }
        }
        return -1;
    }

    /**returns the Search String
    */
    public String getSearchString() {
        return searchString;
    }

    /**returns the Replace String
    */
    public String getReplaceString() {
        return replaceString;
    }
}