package de.renew.navigator.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-25
 */
final public class NavigatorFileTree extends Model {

    /**
     * List of all filters which are currently applied to the navigator trees.
     */
    protected List<SearchFilter> activeFileFilters;

    /**
     * A search filter which represents the current text search.
     */
    protected SearchFilter textSearch;

    /**
     * List of all tasks which are currently executed.
     */
    protected final List<BackgroundTask> backgroundTasks;

    /**
     * Constructor.
     */
    public NavigatorFileTree() {
        super();
        activeFileFilters = new ArrayList<SearchFilter>();
        backgroundTasks = new LinkedList<BackgroundTask>();
    }

    public List<SearchFilter> getActiveFileFilters() {
        return activeFileFilters;
    }

    public void setActiveFileFilters(List<SearchFilter> activeFileFilters) {
        this.activeFileFilters = activeFileFilters;
        setChanged();
    }

    public List<TreeElement> getTreeRoots() {
        return elements;
    }

    /**
     * Activates a file filter on this navigator model.
     *
     * @param fileFilter the filter which should be activated
     */
    public void activateFileFilter(SearchFilter fileFilter) {
        this.activeFileFilters.add(fileFilter);

        // Inform observers.
        setChanged();
    }

    /**
     * Deactivates a file filter on this navigator model.
     *
     * @param fileFilter the filter which should be activated
     */
    public void deactivateFileFilter(SearchFilter fileFilter) {
        if (!this.activeFileFilters.remove(fileFilter)) {
            throw new IllegalArgumentException(String.format("The file filter was not activated yet: %s",
                                                             fileFilter));
        }

        // Inform observers.
        setChanged();
    }

    /**
     * Deactivates a file filter by its name.
     *
     * @param name of the filter
     */
    public void deactivateFileFilter(String name) {
        SearchFilter filter = getFileFilterByName(name);

        if (filter == null) {
            return;
        }

        deactivateFileFilter(filter);
    }

    /**
     * Finds a file filter by its name.
     *
     * @param name of the file filter
     * @return null, if none found
     */
    public SearchFilter getFileFilterByName(String name) {
        for (SearchFilter filter : activeFileFilters) {
            if (filter.getName().equals(name)) {
                return filter;
            }
        }

        return null;
    }

    /**
     * Clears all tree roots.
     */
    public void clearTreeRoots() {
        elements.clear();
        setChanged();
    }

    /**
     * Expands all root directories.
     */
    public void expandAll() {
        for (TreeElement element : elements) {
            element.expandAll();
        }
    }

    /**
     * Clears the model.
     */
    public void clear() {
        activeFileFilters.clear();
        elements.clear();
    }

    /**
     * @return current active background task.
     */
    public List<BackgroundTask> getBackgroundTasks() {
        return backgroundTasks;
    }

    /**
     *
     * Adds a background
     * task.
     *
     * @param task The task to add.
     */
    public void addBackgroundTask(BackgroundTask task) {
        backgroundTasks.add(task);
        setChanged();
    }

    /**
     * Removes a background task.
     *
     * @param task The task to remove.
     */
    public void removeBackgroundTask(BackgroundTask task) {
        backgroundTasks.remove(task);
        setChanged();
    }

    public SearchFilter getTextSearch() {
        return textSearch;
    }

    public void setTextSearch(SearchFilter textSearch) {
        this.textSearch = textSearch;
        setChanged();
    }
}