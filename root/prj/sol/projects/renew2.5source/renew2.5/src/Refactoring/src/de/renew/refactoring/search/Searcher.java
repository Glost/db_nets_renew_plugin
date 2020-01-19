package de.renew.refactoring.search;



/**
 * Interface for searchers that return objects of type {@link R}.
 *
 * @author 2mfriedr
 */
public interface Searcher<R> {

    /**
     * Returns the number of items to be searched.
     *
     * @return the number of items
     */
    public int getNumberOfItemsToSearch();

    /**
     * Checks if there are more items to search.
     *
     * @return {@code true} if there are more items to search, otherwise
     * {@code false}
     */
    public boolean hasNextItemToSearch();

    /**
     * Returns the progress of the operation.
     *
     * @return an integer that is greater than or equal to 0 and less than or
     * equal to 100
     */
    public int getProgress();

    /**
     * Returns the name of the item that is currently being searched.
     *
     * @return the name of the item, or an empty string if the operation
     * has not started.
     */
    public String getCurrentItemString();

    /**
    * Searches the next item.
    *
    * @return the result of type {@link R}
    */
    public R searchNextItem();
}