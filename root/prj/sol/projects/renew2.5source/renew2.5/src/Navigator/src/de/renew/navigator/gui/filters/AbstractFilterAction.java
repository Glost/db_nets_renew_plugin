package de.renew.navigator.gui.filters;

import de.renew.navigator.NavigatorAction;
import de.renew.navigator.models.NavigatorFileTree;
import de.renew.navigator.models.SearchFilter;

import java.awt.event.ActionEvent;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-11-02
 */
abstract class AbstractFilterAction extends NavigatorAction {
    private final SearchFilter searchFilter;
    private final NavigatorFileTree fileTree;

    public AbstractFilterAction(SearchFilter searchFilter, String iconName,
                                NavigatorFileTree fileTree) {
        super(nameByFilter(searchFilter), iconName, null);
        this.searchFilter = searchFilter;
        this.fileTree = fileTree;
    }

    private static String nameByFilter(SearchFilter searchFilter) {
        return searchFilter.getName();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String name = nameByFilter(searchFilter);
        final boolean doActivate = fileTree.getFileFilterByName(name) == null;
        this.putValue("active", doActivate);

        if (doActivate) {
            fileTree.activateFileFilter(searchFilter);
            fileTree.notifyObservers();
            return;
        }

        fileTree.deactivateFileFilter(name);
        fileTree.notifyObservers();
    }
}