package de.renew.refactoring.parse;



/**
 * Provides a method to check if a package is imported in an import statement.
 *
 * @author 2mfriedr
 */
public class ImportParser {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ImportParser.class);

    /**
     * Checks if an input string is an import statement.
     *
     * @param input the input
     * @return {@code true} if the input is an import statement, otherwise
     * {@code false}
     */
    public boolean isImportStatement(final String input) {
        return input.trim().startsWith("import");
    }

    /**
     * Checks if an import statement, or a block of import statements, imports
     * a specified package.
     *
     * @param statement the import statement
     * @param packageName the package name
     * @return {@code true} if the statement imports the specified package
     * name, otherwise {@code false}
     */
    public boolean importsPackage(final String statement,
                                  final String packageName) {
        // TODO remove comments from input
        if (statement.contains(packageName)) {
            return true;
        }

        // remove the last package component and check if it is imported with .*
        String shortenedPackage = packageName;
        while (shortenedPackage.lastIndexOf('.') != -1) {
            shortenedPackage = shortenedPackage.substring(0,
                                                          shortenedPackage
                                   .lastIndexOf('.'));
            String withStarImport = shortenedPackage + ".*";
            if (statement.contains(withStarImport)) {
                return true;
            }
        }
        return false;
    }
}