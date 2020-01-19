package de.renew.navigator.gui.filters;

import de.renew.navigator.gui.NavigatorIcons;
import de.renew.navigator.models.NavigatorFileTree;
import de.renew.navigator.models.SearchFilter;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-09-15
 */
public final class JavaSearchFilter extends AbstractFilterAction {

    /**
     * Creates the ARM search filter.
     */
    public JavaSearchFilter(NavigatorFileTree fileTree) {
        super(getSearchFilter(), NavigatorIcons.FILE_JAVA, fileTree);
    }

    /**
     * @return search filter instance used by this action.
     */
    private static SearchFilter getSearchFilter() {
        return new SearchFilter("java", SearchFilter.Type.ENDS_WITH, true,
                                ".java", ".jsp");
    }
}