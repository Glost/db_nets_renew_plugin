package de.renew.refactoring.parse;



/**
 * Provides methods to parse package declarations.
 *
 * @author 2mfriedr
 */
public class PackageParser {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(PackageParser.class);

    /**
     * Checks if an input string is a package declaration.
     *
     * @param input the input
     * @return {@code true} if the input is a package declaration, otherwise
     * {@code false}
     */
    public boolean isPackageDeclaration(final String input) {
        return input.trim().startsWith("package");
    }

    /**
     * Finds the declared package in a string, i.e. the substring between
     * "package" and ";".
     *
     * @param input the input string, with declaration node syntax
     * @return the package, or {@code null} if none is declared
     */
    public String findPackage(final String input) {
        // TODO remove comments from input
        if (!input.contains("package")) {
            return null;
        }
        int packageIndex = input.indexOf("package") + "package".length();
        int semicolonIndex = input.indexOf(";", packageIndex);
        return input.substring(packageIndex, semicolonIndex).trim();
    }
}