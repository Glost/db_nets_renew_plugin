package de.renew.refactoring.search;

import de.renew.refactoring.util.ProgressCalculator;

import java.util.Iterator;
import java.util.List;


/**
 * Abstract base class for searchers that uses an iterator of items.
 *
 * @author 2mfriedr
 */
public abstract class IteratorSearcher<T, R> implements Searcher<R> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(IteratorSearcher.class);
    private final Iterator<T> _items;
    private final int _numberOfItems;
    private int _numberOfSearchedItems;
    private T _currentItem;

    /**
     * Constructs an iterator searcher with an iterator and the number of its
     * items.
     *
     * @param items the iterator
     * @param numberOfItems the number of items
     */
    public IteratorSearcher(final Iterator<T> items, final int numberOfItems) {
        _items = items;
        _numberOfItems = numberOfItems;
    }

    /**
     * Constructs an iterator searcher with a list of items.
     *
     * @param items the list of items
     */
    public IteratorSearcher(final List<T> items) {
        this(items.iterator(), items.size());
    }

    @Override
    public int getNumberOfItemsToSearch() {
        return _numberOfItems;
    }

    @Override
    public boolean hasNextItemToSearch() {
        return _items.hasNext();
    }

    @Override
    public int getProgress() {
        return ProgressCalculator.calculateProgress(_numberOfSearchedItems,
                                                    _numberOfItems);
    }

    @Override
    public String getCurrentItemString() {
        return _currentItem.toString();
    }

    @Override
    public R searchNextItem() {
        _currentItem = _items.next();
        _numberOfSearchedItems += 1;
        return searchItem(_currentItem);
    }

    /**
     * Searches an item.
     *
     * @param item the item
     * @return an object of type R
     */
    protected abstract R searchItem(T item);

    /**
     * Returns the item that is currently being searched.
     *
     * @return the item
     */
    protected T getCurrentItem() {
        return _currentItem;
    }
}