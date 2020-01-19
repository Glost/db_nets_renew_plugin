package de.renew.refactoring.parse;

import de.renew.refactoring.match.StringMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * LinkParser implementation using regular expressions.
 *
 * @author 2mfriedr
 */
public class RegexLinkParser implements LinkParser {
    @Override
    public boolean isValidChannelName(final String name) {
        return Patterns.NAME_PATTERN.matcher(name).matches();
    }

    @Override
    public boolean containsUplink(final String string) {
        return Patterns.UPLINK_PATTERN.matcher(string).find();
    }

    @Override
    public boolean containsDownlink(final String string) {
        return Patterns.DOWNLINK_PATTERN.matcher(string).find();
    }

    @Override
    public StringMatch findUplink(final String string) {
        Matcher matcher = Patterns.UPLINK_PATTERN.matcher(string);
        if (matcher.find()) {
            return new StringMatch(string.substring(matcher.start(),
                                                    matcher.end()),
                                   matcher.start(), matcher.end());
        }
        return null;
    }

    @Override
    public StringMatch findUplink(String string, String channel,
                                  int parameterCount) {
        return null; //FIXME Implementation (oder delete unused RegexLinkParser)
    }

    @Override
    public StringMatch findChannelName(final String string) {
        assert containsUplink(string) || containsDownlink(string);

        // channel name is the string between ':' and '(', stripped of whitespace. 
        int nameStart = string.indexOf(':') + 1;
        int nameEnd = string.indexOf('(');

        return new StringMatch(string.substring(nameStart, nameEnd).trim(),
                               nameStart, nameEnd);
    }

    @Override
    public int findParameterCount(final String string) {
        assert containsUplink(string) || containsDownlink(string);

        // if there is only whitespace between '(' and ')', the parameter count is 0.
        int openingPar = string.indexOf('(');
        int closingPar = string.indexOf(')');
        String betweenPars = string.substring(openingPar + 1, closingPar);
        if (betweenPars.trim().isEmpty()) {
            return 0;
        }

        // otherwise, we count the commas between '(' and ')' and add 1.
        // commas between '[' and ']' must be ignored.
        int bracketLevel = 0;
        int parameterCount = 1;
        for (int i = 0; i < betweenPars.length(); i++) {
            switch (betweenPars.charAt(i)) {
            case '[':
                bracketLevel++;
                break;
            case ']':
                bracketLevel--;
                break;
            case ',':
                if (bracketLevel == 0) {
                    parameterCount++;
                }
                break;
            default:
                break;
            }
        }
        return parameterCount;
    }

    @Override
    public boolean isDownlinkToThis(String link) {
        return Patterns.DOWNLINK_TO_THIS_PATTERN.matcher(link).matches();
    }

    @Override
    public List<StringMatch> findDownlinks(final String string) {
        return null; //FIXME Implementation (oder delete unused RegexLinkParser)
    }

    @Override
    public List<StringMatch> findDownlinks(final String string,
                                           final String channel,
                                           final int parameterCount) {
        Pattern downlinkPattern = downlinkPatternForChannel(channel,
                                                            parameterCount);

        List<StringMatch> matches = new ArrayList<StringMatch>();
        Matcher downlinkMatcher = downlinkPattern.matcher(string);
        while (downlinkMatcher.find()) {
            int downlinkStart = downlinkMatcher.start();
            int downlinkEnd = downlinkMatcher.end();
            String downlink = string.substring(downlinkStart, downlinkEnd);
            StringMatch match = new StringMatch(downlink, downlinkStart,
                                                downlinkEnd);
            matches.add(match);
        }

        return matches;
    }

    /**
     * Builds a {@link Pattern} that matches downlinks to a channel, like
     * "this:ch(x,y,z), with the specified name and parameter count.
     * @param channel the channel name
     * @param parameterCount the parameter count
     * @return the pattern
     */
    protected Pattern downlinkPatternForChannel(final String channel,
                                                final int parameterCount) {
        StringBuilder sb = new StringBuilder();
        sb.append(Patterns.DOWNLINK_BEFORE_CHANNEL_NAME);
        sb.append(Patterns.addWhitespace(channel));
        sb.append(Patterns.OPENING_PAR);
        if (parameterCount > 0) {
            sb.append(Patterns.PARAM);
        }
        if (parameterCount > 1) {
            // add comma-separated parameters 
            sb.append("(," + Patterns.PARAM + ")");
            sb.append("{" + (parameterCount - 1) + "}");
        }
        sb.append(Patterns.CLOSING_PAR);
        return Pattern.compile(sb.toString());
    }
}

/**
 * Stores various regular expressions that are used by RegexLinkParser.
 *
 * @author 2mfriedr
 */
final class Patterns {
    static final String WHITESPACE = "\\s*";
    static final String PARENTHESES = "\\(.*\\)";
    static final Pattern NAME_PATTERN = Pattern.compile(addWhitespace("\\w[\\w\\d]*"));
    static final Pattern UPLINK_PATTERN = Pattern.compile("(^\\s*|;\\s*):"
                                                          + addWhitespace(NAME_PATTERN)
                                                          + PARENTHESES);
    static final Pattern DOWNLINK_PATTERN = Pattern.compile(NAME_PATTERN
                                                            + addWhitespace(":")
                                                            + addWhitespace(NAME_PATTERN)
                                                            + PARENTHESES);
    static final Pattern DOWNLINK_TO_THIS_PATTERN = Pattern.compile(Patterns
                                                        .addWhitespace("this")
                                                                    + Patterns
                                                        .addWhitespace(":")
                                                                    + addWhitespace(NAME_PATTERN)
                                                                    + Patterns.PARENTHESES);
    static final String DOWNLINK_BEFORE_CHANNEL_NAME = addWhitespace(NAME_PATTERN)
                                                       + ":";
    static final String OPENING_PAR = addWhitespace("\\(");
    static final String CLOSING_PAR = addWhitespace("\\)");
    static final String PARAM = addWhitespace("(([\\w]+[\\w\\d\\s]*)|(\\[.*\\]))");

    static final String addWhitespace(String string) {
        return WHITESPACE + string + WHITESPACE;
    }

    static final String addWhitespace(Pattern pattern) {
        return WHITESPACE + pattern + WHITESPACE;
    }
}