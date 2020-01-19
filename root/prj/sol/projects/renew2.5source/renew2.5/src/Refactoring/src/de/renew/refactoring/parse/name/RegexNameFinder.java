package de.renew.refactoring.parse.name;

import de.renew.refactoring.match.StringMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Name finder implementation using a regular expression. The expression
 * checks if the character before and after an occurrence of the name are
 * non-word characters. An additional exclude pattern can be provided.
 *
 * @author 2mfriedr
 */
public class RegexNameFinder implements NameFinder {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RegexNameFinder.class);
    private final String _name;
    private final Pattern _namePattern;

    /**
     * Constructs a RegexNameFinder for a specified name with no additional
     * exclude pattern.
     *
     * @param name the name
     */
    public RegexNameFinder(final String name) {
        this(name, null);
    }

    /**
     * Constructs a RegexNameFinder for a specified name with an additional
     * exclude pattern. The exclude pattern is inserted into the regular
     * expression like this: {@code [^...--here--]}.
     *
     * @param name the name
     * @param exclude the exclude pattern
     */
    public RegexNameFinder(final String name, final String exclude) {
        this(name, exclude, exclude);
    }

    /**
     * Constructs a RegexNameFinder for a specified name with an additional
     * exclude pattern. The exclude pattern is inserted into the regular
     * expression like this: {@code [^...--here--]}.
     *
     * @param name the name
     * @param excludeBefore the exclude pattern before the name
     * @param excludeAfter the exclude pattern after the name
     */
    public RegexNameFinder(final String name, final String excludeBefore,
                           final String excludeAfter) {
        _name = name;
        _namePattern = Pattern.compile("(^|[^\\w" + excludeBefore + "])" + name
                                       + "($|[^\\w" + excludeAfter + "])");
        //FIXME when two name occurrences are separated by only one character,
        // this pattern doesn't match both
    }

    @Override
    public boolean find(final String input) {
        return _namePattern.matcher(input).find();
    }

    @Override
    public List<StringMatch> listOfMatches(final String input) {
        Matcher matcher = _namePattern.matcher(input);
        List<StringMatch> matches = new ArrayList<StringMatch>();

        while (matcher.find()) {
            String matchedString = input.substring(matcher.start(),
                                                   matcher.end());

            int nameStart = matcher.start() + matchedString.indexOf(_name);
            int nameEnd = nameStart + _name.length();
            String nameString = input.substring(nameStart, nameEnd);
            matches.add(new StringMatch(nameString, nameStart, nameEnd));
        }
        return matches;
    }
}