package de.renew.refactoring.search.range;



/**
 * Base interface for search ranges that only requires a description. This
 * interface should only be used in a GUI search range picker. In almost all
 * cases, {@link DrawingSearchRange} or {@link FileSearchRange} should be
 * implemented instead of this interface.
 *
 * @author 2mfriedr
 */
public interface SearchRange {

    /**
     * Returns a short, human-readable description of the search range.
     *
     * @return the description
     */
    public String description();
}