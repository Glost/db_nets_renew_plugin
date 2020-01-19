package de.renew.navigator.gui.filters;

import de.renew.navigator.gui.NavigatorIcons;
import de.renew.navigator.models.NavigatorFileTree;
import de.renew.navigator.models.SearchFilter;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-09-15
 */
public final class RNWSearchFilter extends AbstractFilterAction {

    /**
     * Creates the ARM search filter.
     */
    public RNWSearchFilter(NavigatorFileTree fileTree) {
        super(getSearchFilter(), NavigatorIcons.FILE_NET, fileTree);
    }

    /**
     * @return search filter instance used by this action.
     */
    private static SearchFilter getSearchFilter() {
        return new SearchFilter("net", SearchFilter.Type.ENDS_WITH, true,
                                ".rnw", ".aip", ".draw", ".arm");
    }
}