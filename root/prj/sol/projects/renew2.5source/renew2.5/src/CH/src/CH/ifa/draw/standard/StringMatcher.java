package CH.ifa.draw.standard;



/**
   StringMatcher provides the interface for classes that are able
   to match Strings with the given search String and can also
   replace - in case of a match - the matching part with the
   replacement string. Examples are exact match, substring match,
   match by regular expressions etc.
*/
public interface StringMatcher {

    /** True iff matching conditions of searchstring are fulfilled
    for the compare String.
    */
    public boolean matches(String compareString, boolean ignoreCase);

    /** Replaces the match
    of searchString
    in compareString
    with the replaceString.
    */
    public String replacement(String compareString);
}