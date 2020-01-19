/*
 * @(#)StringUtil.java
 */
package de.renew.util;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * A utility class for advanced string handling, e.g. for filenames, paths,
 * extensions, replacing substring etc.
 * We use the following naming conventions:
 * <ul>
 * <i>An <b>absolute pathname</b> is a string which fully describes a file (including path and
 * extension), starting from a system-dependent root string. <i>A <b>relative pathname</b> is a
 * pathname which is specified relatively to another path and thus does not start with a
 * system-dependent root string. <i>An <b>path</b> is a string which fully describes a directory. We
 * also use "relative" and "absolute" here. <i>A <b>file name</b> is a pathname without the path and
 * without the extension. <i>An <b>extended file name</b> is a pathname without the path (but
 * including the extension). <i>An <b>extension</b> is a everything following the "dot" in an
 * extended file name. If there is not dot in an extended file name, its extension is the empty
 * string.
 * </ul>
 */
public class StringUtil {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StringUtil.class);

    /** The length of File.separator, which is usually 1. */
    public final static int separatorLength = File.separator.length();

    /**
     * Returns the string resulting from replacing all appearences of <tt>what</tt> in <tt>str</tt>
     * by <tt>by</tt>.
     *
     * @param str The original string.
     * @param what The substring to look for.
     * @param by The string by which <tt>what</tt> is replaced.
     * @return The result of replacing all apprearences of <tt>what</tt> in <tt>str</tt> by
     *         <tt>by</tt>.
     */
    public static String replace(String str, String what, String by) {
        StringBuffer rpl = new StringBuffer();
        int whatlen = what.length();
        int index = 0;
        int lastIndex;
        while (true) {
            // search for next
            lastIndex = index;
            index = str.indexOf(what, lastIndex);
            if (index < 0) {
                break;
            }
            rpl.append(str.substring(lastIndex, index));
            rpl.append(by);
            index += whatlen;
        }
        rpl.append(str.substring(lastIndex));
        return rpl.toString();
    }

    /**
     * Splits a string into parts defined by a seperation string.
     * The seperation string itself is not put into the array
     * elements. Empty elements are left out.
     * In contrast to java's StringTokenizer, the seperation string
     * is interpreted as a sequence of characters, not as a choice
     * of different seperation characters.
     */
    public static String[] split(String str, final String sep) {
        Vector<String> entries = new Vector<String>();
        final int seplen = sep.length();
        int index = 0;
        int lastIndex;
        str = str + sep;
        while (true) {
            // search for next
            lastIndex = index;
            index = str.indexOf(sep, lastIndex);
            if (index < 0) {
                break;
            }
            if (index > lastIndex) {
                entries.addElement(str.substring(lastIndex, index));
            }
            index += seplen;
        }

        String[] result = new String[entries.size()];
        entries.copyInto(result);
        return result;
    }

    /**
     * Converts only the first character of a String to
     * upper case. The rest of the String or the empty
     * String is not modified.
     */
    public static String firstToUpperCase(String str) {
        if (str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Converts only the first character of a String to
     * lower case. The rest of the String or the empty
     * String is not modified.
     */
    public static String firstToLowerCase(String str) {
        if (str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Returns whether the nth character in String str is in upper case.
     * Returns false if n is out of bounds.
     */
    public static boolean isUpperCaseAt(String str, int n) {
        return n < str.length() && Character.isUpperCase(str.charAt(n));
    }

    /**
     * Returns whether the nth character in String str is in lower case.
     * Returns false if n is out of bounds.
     */
    public static boolean isLowerCaseAt(String str, int n) {
        return n < str.length() && Character.isLowerCase(str.charAt(n));
    }

    /**
     * Counts the occurences of a substring within a String.
     * Overlapping occurences are not counted
     * (e.g. countOccurences("AAAA","AA")==2, not 3).
     */
    public static int countOccurences(String str, String sub) {
        int count = 0;
        int index = 0;
        int sublen = sub.length();
        while (true) {
            index = str.indexOf(sub, index);
            if (index < 0) {
                break;
            }
            index += sublen;
            ++count;
        }
        return count;
    }

    /**
     * Counts the number of lines within a String. A String has one more line
     * than the number of new-line characters it contains.
     */
    public static int countLines(String str) {
        return countOccurences(str, "\n") + 1;
    }

    /**
     * Splits a String into different tokens with the whitespace delimiter. A
     * whitespace may get escaped with surrounding quotes or a preceding
     * backslash (\).
     *
     * @param s the String to split
     * @return an array of Strings containing the different tokens
     */
    public static String[] splitStringWithEscape(String s) {
        List<String> tokens = new ArrayList<String>();
        char[] sChars = s.toCharArray();

        boolean quoted = false;
        boolean escaped = false;
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sChars.length; i++) {
            char currentChar = sChars[i];

            // escaping a space with a preceding backslash (\)
            if (currentChar == '\\' && i + 1 < sChars.length
                        && sChars[i + 1] == ' ') {
                escaped = true;
            }
            // start or end a quote
            else if (currentChar == '"' || currentChar == '\'') {
                quoted = !quoted;
            }
            // a whitespace (0x000A: line feed, 0x000D: carriage return, 0x0009: tab, 0x0020: space)
            else if (currentChar == 0x000A || currentChar == 0x000D
                             || currentChar == 0x0009 || currentChar == 0x0020) {
                if (quoted) {
                    sb.append(currentChar);
                } else if (escaped) {
                    sb.append(currentChar);
                    escaped = false;
                } else {
                    String token = sb.toString();
                    sb = new StringBuffer();
                    if (!token.matches("^\\s*$")) {
                        tokens.add(token);
                    }
                }
            }
            // normal character
            else {
                sb.append(currentChar);
            }

            // last character
            if (i == sChars.length - 1) {
                String filename = sb.toString();
                tokens.add(filename);
            }
        }

        String[] tokensArray = new String[tokens.size()];
        return tokens.toArray(tokensArray);
    }

    /**
     * Join a Collection of Strings into a single String separated by the given delimiter
     *
     * @param s the Collection of Strings
     * @param delimiter the delimiter
     * @return the joined String
     */
    public static String join(Collection<String> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    /**
     * Join an array of Strings into a single String separated by the given delimiter
     *
     * @param s the array of Strings
     * @param delimiter the delimiter
     * @return the joined String
     */
    public static String join(String[] s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            builder.append(s[i]);
            if (i < s.length - 1) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    /**
     * Join an array of Strings into a single String separated by the given delimiter
     *
     * @param s the array of Strings
     * @param delimiter the delimiter
     * @param strict trim String
     * @return the joined String
     */
    public static String join(String[] s, String delimiter, boolean strict) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            if (strict) {
                builder.append(trimToEmpty(s[i]));
            } else {
                builder.append(s[i]);
            }
            if (i < s.length - 1) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    /****************************************/

    /*                                      */
    /* File-related string editing methods */
    /*                                      */

    /****************************************/
    private static int getLastSeparatorPos(String pathname) {
        int pos = pathname.lastIndexOf('/');
        if (pos == -1) {
            pos = pathname.lastIndexOf(File.separator);
        }
        return pos;
    }

    private static int getLastDotPos(String pathname) {
        int lsp = getLastSeparatorPos(pathname);
        int ldp = pathname.lastIndexOf(".");
        if (ldp > lsp) {
            return ldp;
        } else {
            return pathname.length();
        }
    }

    /**
     * Removes the file extension from the filename.
     * Also remove the dot separating the extension.
     */
    public static String stripFilenameExtension(String pathname) {
        return pathname.substring(0, getLastDotPos(pathname));
    }

    /**
     * Exchanges the file extension with a new extension, if extension is not empty.
     *
     * @param filePath - original file path
     * @param extension - the new extension
     * @return file path with new extension
     */
    public static String extendFileNameBy(String filePath, String extension) {
        String result = stripFilenameExtension(filePath);
        if (!extension.isEmpty()) {
            result += "." + extension;
        }
        return result;
    }

    /**
     * Returns the corresponding extended filename for a given
     * pathname.
     */
    public static String getExtendedFilename(String pathname) {
        return pathname.substring(getLastSeparatorPos(pathname) + 1);
    }

    /**
     * Returns the corresponding filename for a given
     * pathname.
     */
    public static String getFilename(String pathname) {
        return pathname.substring(getLastSeparatorPos(pathname) + 1,
                                  getLastDotPos(pathname));
    }

    /**
     * Returns the corresponding extension for a given
     * pathname.
     */
    public static String getExtension(String pathname) {
        int firstAfterDot = getLastDotPos(pathname) + 1;
        if (firstAfterDot < pathname.length()) {
            return pathname.substring(firstAfterDot).toLowerCase();
        } else {
            return "";
        }
    }

    public static String getPath(String pathname) {
        int lastPos = getLastSeparatorPos(pathname);
        if (lastPos < 0) {
            return "";
        } else {
            return pathname.substring(0, lastPos);
        }
    }

    public static String makeRelative(String basePathname, String pathname) {
        // TO-DO: case that path is not a subpath of basePathname.
        if (pathname != null) {
            if (basePathname == null) {
                pathname = convertToSlashes(pathname);
            } else {
                // add seperator at end of basePathname:
                basePathname += File.separator;
                // find longest matching path-prefix:
                int index = 0;

                // find longest matching path-prefix:
                int size;
                while (true) {
                    // search for next
                    size = index;
                    index = basePathname.indexOf(File.separator, index)
                            + separatorLength;
                    if (index == -1 + separatorLength
                                || !pathname.startsWith(basePathname.substring(0,
                                                                                       index))) {
                        break;
                    }
                }
                String dotdots = "";
                if (size > 0) {
                    // size==0 would mean that either "base" was not
                    // absolute or it was a DOS path on a different drive. There
                    // is no way to make this path relative, so return the
                    // absolute pathname.
                    index = size;
                    while ((index = basePathname.indexOf(File.separator, index)) >= 0) {
                        index += separatorLength;
                        dotdots += "../";
                    }
                }
                pathname = dotdots + convertToSlashes(pathname.substring(size));
            }
        }
        return pathname;
    }

    public static String makeCanonical(String pathOrFilename) {
        String pathname = null;
        try {
            pathname = new File(pathOrFilename).getCanonicalPath();
            // Check for OS/2 network filename bug:
            if (pathname.equals(pathname.toUpperCase())) {
                // Only use the path information of the new String
                return getPath(pathname) + File.separator
                       + getExtendedFilename(pathOrFilename);
            }
            return pathname;
        } catch (java.io.IOException e) {
            // File does not exist, return the pathOrFilename itself.
            // Maybe try to make relative paths absolute "by hand"?
            return pathOrFilename;
        }
    }

    public static String makeAbsolute(String basePathname, String path) {
        if (basePathname == null || "".equals(basePathname)
                    || path.startsWith("/")
                    || (path.length() > 2 && path.charAt(1) == ':')) {
            // The base pathname is empty or the path was already absolute.
            return convertToSystem(path);
        } else {
            while (path.startsWith("../")) {
                path = path.substring(3);
                basePathname = getPath(basePathname);
            }
            return basePathname + File.separator + convertToSystem(path);
        }
    }

    /**
     * Returns the result of replacing all system-dependent file
     * separators by slashes.
     */
    public static String convertToSlashes(String path) {
        return replace(path, File.separator, "/");
    }

    /**
     * Returns the result of replacing all slashes by
     * system-dependent file separators.
     */
    public static String convertToSystem(String path) {
        return replace(path, "/", File.separator);
    }

    public static String[] splitPaths(String paths) {
        return split(paths, File.pathSeparator);
    }

    /**
     * Returns a relative pathname for a given full class name.
     */
    public static String classToFile(String classname) {
        return replace(classname, ".", File.separator) + ".class";
    }

    /**
     * Strips off all leading and trailing white space
     * and substitutes sequences of white space by a single space.
     */
    public static String unspace(String arg) {
        StringBuffer result = new StringBuffer();
        int n = arg.length();
        boolean space = false;
        boolean first = true;
        for (int i = 0; i < n; i++) {
            char c = arg.charAt(i);
            if (Character.isWhitespace(c)) {
                space = true;
            } else {
                if (space && !first) {
                    result.append(' ');
                }
                space = false;
                first = false;
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Determine whether a string constitutes a valid
     * Java identifier or is empty.
     */
    public static boolean isNameOrEmpty(String name, boolean dotAllowed) {
        int i = 0;

        // We accept the empty string.
        while (i < name.length()) {
            // One letter.
            if (!Character.isJavaIdentifierStart(name.charAt(i++))) {
                return false;
            }

            // An arbitrary number of letters of digits.
            while (i < name.length()
                           && Character.isJavaIdentifierPart(name.charAt(i))) {
                i++;
            }

            // Either the end of the string or ...
            if (i < name.length()) {
                if (!dotAllowed) {
                    return false;
                }

                // ... a dot ...
                if (name.charAt(i++) != '.') {
                    return false;
                }

                // ... followed by something.
                if (i == name.length()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Relativizes one URI against another by looking at the
     * parent of the given base URI. The parent is taken into
     * consideration only if direct URI relativization fails.
     * <p>
     * This operation is a refinement of {@link URI#relativize} that acts like the
     * {@link #makeRelative(String,String) makeRelative} operation for filename strings. If
     * <code>baseURI.relativize(uri)</code> would return a non-absolute URI, this method returns the
     * same result.
     * </p>
     * <p>
     * The following condition holds always:
     * <code>baseURI.resolve(makeRelative(baseURI, uri)).equals(uri)</code>
     *
     * @param baseURI the base URI
     * @param uri the
     * @return an <code>URI</code> value
     **/
    public static URI makeRelative(URI baseURI, URI uri) {
        // First try: ask the URI itself
        URI result = baseURI.relativize(uri);

        // If the result was not relative, make a second try
        // with the parent path of the baseURI. This attempt
        // makes sense only if there is hope, therefore check
        // whether scheme and authority of both URIs match.
        if (result.isAbsolute()
                    && equalOrBothNull(baseURI.getScheme(), uri.getScheme())
                    && equalOrBothNull(baseURI.getAuthority(),
                                               uri.getAuthority())) {
            URI lastParent = null;
            int backsteps = 0;
            URI parent = getParent(baseURI, false);
            result = parent.relativize(uri);

            // if the result is still not relative, iterate
            // backwards through the base URI's path.
            while (result.isAbsolute() && !parent.equals(lastParent)) {
                lastParent = parent;
                backsteps++;
                parent = getParent(lastParent, true);
                result = parent.relativize(uri);
            }

            // if we were successful, add a "../" to the path
            // for each backstep.
            if (!result.isAbsolute()) {
                StringBuffer newPath = new StringBuffer();
                for (int i = 0; i < backsteps; i++) {
                    newPath.append("../");
                }
                newPath.append(result.getPath());
                try {
                    result = new URI(result.getScheme(), result.getAuthority(),
                                     newPath.toString(), result.getQuery(),
                                     result.getFragment());
                } catch (URISyntaxException e) {
                    logger.error(e.getMessage(), e);
                    result = uri;
                }
            }
        }
        assert baseURI.resolve(result).equals(uri) : "StringUtil.makeRelative result does not hold its contract."
        + "\n  given uri=" + uri + "\n  verified =" + baseURI.resolve(result)
        + "\n  result   =" + result + "\n  baseURI  =" + baseURI;
        return result;
    }

    private static boolean equalOrBothNull(Object one, Object two) {
        return (one == null && two == null) || (one != null && one.equals(two));
    }

    /**
     * Computes a parent URI from the given URI. This means that
     * <i>scheme</i> and <i>authority</i> remain unchanged, the
     * last segment of the <i>path</i> is removed, and both
     * <i>query</i> and <i>fragment</i> are dropped.
     * If the path of the given URI ends with a slash (meaning
     * it denotes a directory), the path will not be shortened
     * unless <code>directoriesToo</code> is set.
     * If the path denotes the root directory (slash only), it
     * will never be shortened.
     * <p>
     * This operation is somewhat like the {@link #getPath} operation for filename strings.
     * </p>
     *
     * @param uri the base URI to compute the parent from
     * @param directoriesToo whether URIs denoting directories
     *            should also be shortened. If <code>false</code>, a directory is
     *            considered being its own parent.
     * @return the shortened URI.
     */
    public static URI getParent(URI uri, boolean directoriesToo) {
        String path = uri.getPath();
        String parent = null;
        if (path != null) {
            if (directoriesToo && path.endsWith("/") && (path.length() > 1)) {
                path = path.substring(0, path.length() - 1);
            }
            int lastPos = path.lastIndexOf("/");
            if (lastPos >= 0) {
                parent = path.substring(0, lastPos + 1);
            }
        }
        try {
            URI result = new URI(uri.getScheme(), uri.getAuthority(), parent,
                                 null, null);
            return result;
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalArgumentException("StringUtil: Could not derive parent URI from "
                                               + uri);
        }
    }

    /**
     * Checks whether the given String <b>text</b> is null.
     * Notice: An empty string is not null!
     *
     * @param text [String]
     * @author Eva Mueller
     * @return boolean
     */
    public static boolean isNull(String text) {
        if (text == null) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the given String <b>text</b> is empty.
     * Notice: A string is not empty if it is null,
     * but the string is empty as well if the trimmed string is of length 0.
     *
     * @param text [String]
     * @author Eva Mueller
     * @return boolean
     */
    public static boolean isEmpty(String text) {
        if (text != null && text.trim().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the given String <b>text</b> is null or empty.
     * Notice: A string is not empty if it is null,
     * but the string is empty as well if the trimmed string is of length 0.
     *
     * @param text [String]
     * @author Eva Mueller
     * @return boolean
     */
    public static boolean isNullOrEmpty(String text) {
        if (isNull(text) || isEmpty(text)) {
            return true;
        }
        return false;
    }

    /**
     * Returns null if the given String <b>text</b> is null or empty.<br>
     * Else the trimmed String will be returned.
     *
     * @param text [String]
     * @author Eva Mueller
     * @return String
     */
    public static String trimToNull(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        return text.trim();
    }

    /**
     * Returns an empty String if the given String <b>text</b> is empty or null.<br>
     * Else the trimmed String will be returned.
     *
     * @param text [String]
     * @author Eva Mueller
     * @return String
     */
    public static String trimToEmpty(String text) {
        if (text == null || text.trim().length() == 0) {
            return "";
        }
        return text.trim();
    }

    /**
     * Get part of string at given index when string has been separated by given separator
     *
     * @param text [String]
     * @param index [int]
     * @param separator [String]
     * @author Eva Mueller
     * @return String | NULL
     */
    public static String getPart(String text, int index, String separator) {
        if (trimToNull(text) == null || index < 0) {
            return null;
        }
        String[] parts = text.split(separator);
        if (parts == null || parts.length <= index) {
            return null;
        }
        return parts[index];
    }

    /**
     * Makes a string camel case.
     *
     * @param subject The string which is uncameled.
     * @param regex A regex giving the seperator, default is "-".
     * @param firstToUpper If the first part of the Camel should be capitalized or not, default is
     *
     *            <pre>
     * true
     * </pre>
     *
     *            .
     * @author Konstantin Simon Maria Möllers
     * @return a camel cased string.
     */
    public static String camelCase(String subject, String regex,
                                   boolean firstToUpper) {
        String[] split = subject.split(regex);
        StringBuilder resultBuilder = new StringBuilder(firstToUpper
                                                        ? firstToUpperCase(split[0])
                                                        : firstToLowerCase(split[0]));
        for (int i = 1; i < split.length; ++i) {
            resultBuilder.append(firstToUpperCase(split[i]));
        }
        return resultBuilder.toString();
    }

    public static String camelCase(String subject, boolean firstToUpper) {
        return camelCase(subject, "-", firstToUpper);
    }

    public static String camelCase(String subject) {
        return camelCase(subject, true);
    }

    /**
     * @param str string to be upper cased.
     * @return the string with every first letter in a word upper cased.
     */
    public static String upperCaseWords(String str) {
        String[] words = str.split(" ");
        String[] newWords = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            newWords[i] = StringUtil.firstToUpperCase(words[i].toLowerCase());
        }
        return StringUtil.join(newWords, " ");
    }

    /**
     * Moved from <code>DefaultShadowNetLoader</code>
     * Creates an array of path entries from an array of path strings.
     * Strings starting with the reserved word <code>"CLASSPATH"</code> are
     * converted to classpath-relative path entries. All other path strings
     * are converted to path entries that point to canonized directory
     * names.
     *
     * @param paths  the <code>String</code> array with path names.
     * @return an array of <code>PathEntry</code> objects.
     **/
    public static PathEntry[] canonizePaths(String[] paths) {
        if (paths == null) {
            return new PathEntry[0];
        }
        PathEntry[] canonizedEntries = new PathEntry[paths.length];
        for (int i = 0; i < paths.length; ++i) {
            if (paths[i].trim().startsWith("CLASSPATH" + File.separator)) {
                canonizedEntries[i] = new PathEntry(paths[i].trim()
                                                            .substring(9
                                                                       + File.separator
                                                                         .length()),
                                                    true);
            } else if (paths[i].trim().equals("CLASSPATH")) {
                canonizedEntries[i] = new PathEntry("", true);
            } else {
                canonizedEntries[i] = new PathEntry(StringUtil.makeCanonical(paths[i]),
                                                    false);
            }
        }
        return canonizedEntries;
    }
}