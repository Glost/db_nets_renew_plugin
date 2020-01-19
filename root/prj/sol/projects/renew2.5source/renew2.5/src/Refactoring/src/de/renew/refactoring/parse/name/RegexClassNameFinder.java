package de.renew.refactoring.parse.name;

import de.renew.refactoring.match.StringMatch;

import java.util.ArrayList;
import java.util.List;


/**
 * Name finder implementation that finds class names using regex name finders.
 *
 * @author 2mfriedr
 */
public class RegexClassNameFinder implements NameFinder {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RegexClassNameFinder.class);
    private final String _className;
    private final String _packageName;
    private final NameFinder _classNameFinder;
    private final NameFinder _qualifiedClassNameFinder;

    /**
     * Constructs a RegexClassNameFinder with a fully qualified class name.
     *
     * @param fullyQualifiedClassName the fully qualified class name
     * @param matchClassName {@code true} if the short class name should be
     * matched, {@code false} otherwise
     */
    public RegexClassNameFinder(final String fullyQualifiedClassName,
                                final boolean matchClassName) {
        this(className(fullyQualifiedClassName),
             packageName(fullyQualifiedClassName), matchClassName);
    }

    /**
     * Constructs a RegexClassNameFinder with a package name and class name.
     * @param className the class name
     * @param packageName the package name
     * @param matchClassName {@code true} if the short class name should be
     * matched, {@code false} otherwise
     */
    public RegexClassNameFinder(final String className,
                                final String packageName,
                                final boolean matchClassName) {
        _className = className;
        _packageName = packageName;

        _classNameFinder = (matchClassName)
                           ? new RegexNameFinder(className, ".", null)
                           : new NoNameFinder();
        _qualifiedClassNameFinder = new RegexNameFinder(packageName + "."
                                                        + className);
    }

    @Override
    public boolean find(String input) {
        return _classNameFinder.find(input)
               || _qualifiedClassNameFinder.find(input);
    }

    @Override
    public List<StringMatch> listOfMatches(String input) {
        List<StringMatch> result = new ArrayList<StringMatch>(_classNameFinder
                                       .listOfMatches(input));

        for (StringMatch match : _qualifiedClassNameFinder.listOfMatches(input)) {
            // only add the class name part of the qualified name to the result
            int classNameStart = match.start()
                                 + match.match().lastIndexOf(_className);
            int classNameEnd = classNameStart + _className.length();
            result.add(new StringMatch(_className, classNameStart, classNameEnd));
        }
        return result;
    }

    /**
     * Finds the short class name in a fully qualified class name.
     *
     * @param fullyQualifiedClassName the fully qualified class name
     * @return the short class name
     */
    private static String className(String fullyQualifiedClassName) {
        int lastDot = fullyQualifiedClassName.lastIndexOf('.');
        return fullyQualifiedClassName.substring(lastDot + 1);
    }

    /**
     * Finds the package name in a fully qualified class name.
     *
     * @param fullyQualifiedClassName the fully qualified class name
     * @return the package name
     */
    private static String packageName(String fullyQualifiedClassName) {
        int lastDot = fullyQualifiedClassName.lastIndexOf('.');
        return fullyQualifiedClassName.substring(0, lastDot);
    }
}