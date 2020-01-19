/*
 * Created on Apr 18, 2003
 */
package CH.ifa.draw.framework;

import CH.ifa.draw.io.SimpleFileFilter;

import java.util.HashSet;


/**
 * This class holds the SimpleFileFilters for a Drawing.
 * It will provide the default SimpleFileFilters.
 *
 * @author Lawrence Cabac
 */
public class FilterContainer {
    private SimpleFileFilter defaultFileFilter;
    private HashSet<SimpleFileFilter> importfileFilters;
    private HashSet<SimpleFileFilter> exportfileFilters;

    public FilterContainer(SimpleFileFilter ff) {
        importfileFilters = new HashSet<SimpleFileFilter>();
        exportfileFilters = new HashSet<SimpleFileFilter>();
        defaultFileFilter = ff;

    }

    public boolean registerImportFileFilter(SimpleFileFilter ff) {
        return importfileFilters.add(ff);
    }

    public boolean registerExportFileFilter(SimpleFileFilter ff) {
        return exportfileFilters.add(ff);
    }

    public boolean deregisterImportFileFilter(SimpleFileFilter ff) {
        return importfileFilters.remove(ff);
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.drawing.FilterContainer#getFileFilter()
     */
    public HashSet<SimpleFileFilter> getImportFileFilters() {
        return importfileFilters;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.drawing.FilterContainer#getFileFilter()
     */
    public HashSet<SimpleFileFilter> getExportFileFilters() {
        return exportfileFilters;
    }

    /* (non-Javadoc)
    * @see de.renew.diagram.drawing.FilterContainer#getDefaultFileFilter()
    */
    public SimpleFileFilter getDefaultFileFilter() {
        return defaultFileFilter;
    }
}