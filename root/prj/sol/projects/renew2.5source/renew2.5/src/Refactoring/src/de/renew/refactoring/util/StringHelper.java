package de.renew.refactoring.util;

import de.renew.refactoring.match.StringMatch;


/**
 * Helper class for String operations.
 * @author 2mfriedr
 *
 */
public class StringHelper {

    /**
    * Should not be instantiated
    */
    private StringHelper() {
    }

    /**
     * Returns a new string in which the characters in the specified range are
     * replaced with a specified string.
     *
     * @param string the original string
     * @param start the start index
     * @param end the end index
     * @param replacement the replacement string
     * @return a new string with the replaced range
     */
    public static String replaceRange(final String string, final int start,
                                      final int end, final String replacement) {
        assert start >= 0;
        assert end >= start;
        assert end <= string.length();

        return string.substring(0, start) + replacement
               + string.substring(end, string.length());
    }

    /**
     * Returns a string containing the context around a substring in a
     * specified range. Newlines will be replaced by spaces.
     *
     * @param string the original string
     * @param substringStart the substring's start index
     * @param substringEnd the substring's end index
     * @param length the preferred result length
     * @return an HTML string containing the substring and its context
     */
    public static String substringWithContext(final String string,
                                              final int substringStart,
                                              final int substringEnd,
                                              final int length) {
        String boldSubstring = "<strong>"
                               + string.substring(substringStart, substringEnd)
                               + "</strong>";

        String oneline = string.replace('\n', ' '); // replace newlines

        StringBuilder sb = new StringBuilder(length);
        sb.append("<html>");

        if (length < substringEnd - substringStart) {
            // if the substring is longer than the specified length, return
            // only the bold substring
            sb.append(boldSubstring);
        } else {
            int padding = length - (substringEnd - substringStart);
            int carry = (length % 2 == 0) ? 0 : 1;
            int contextStart = Math.max(0,
                                        substringStart - (padding / 2 + carry));
            int contextEnd = Math.min(oneline.length(),
                                      substringEnd + (padding / 2));

            if (contextStart > 0) {
                sb.append("...");
            }
            sb.append(oneline.substring(contextStart, substringStart));
            sb.append(boldSubstring);
            sb.append(oneline.substring(substringEnd, contextEnd));
            if (contextEnd < oneline.length()) {
                sb.append("...");
            }
        }

        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Converts an index that is specified by a line and column (both 1-based)
     * to a string index (0-based). Assumes that lines are ended by "\n" and
     * that the index is valid, i.e. the specified line number is not greater
     * than the number of lines in the string and the column number is not
     * greater than the number of columns in its line.
     *
     * @param string the string
     * @param line the line
     * @param col the column
     * @return the index
     */
    public static int indexForLineAndColumn(final String string,
                                            final int line, final int col) {
        String[] lines = string.split("\n");
        int index = 0;
        for (int i = 0; i + 1 < line; ++i) {
            index += lines[i].length();
            index += 1; // for '\n'
        }
        return index + (col - 1);
    }

    /**
     * Wraps {@link indexForLineAndColumn} and returns a
     * string match object.
     *
     * @param string the string
     * @param beginLine the begin line
     * @param beginColumn the begin column
     * @param endLine the end line
     * @param endColumn the end column
     * @return a string match object
     */
    public static StringMatch makeStringMatch(final String string,
                                              final int beginLine,
                                              final int beginColumn,
                                              final int endLine,
                                              final int endColumn) {
        int beginIndex = indexForLineAndColumn(string, beginLine, beginColumn);
        int endIndex = indexForLineAndColumn(string, endLine, endColumn) + 1;
        String match = string.substring(beginIndex, endIndex);
        return new StringMatch(match, beginIndex, endIndex);

    }
}