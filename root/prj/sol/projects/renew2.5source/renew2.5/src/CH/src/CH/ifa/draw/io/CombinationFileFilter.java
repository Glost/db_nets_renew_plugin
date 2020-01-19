/*
 * Created on Apr 13, 2003
 */
package CH.ifa.draw.io;

import java.io.File;

import java.util.Collection;
import java.util.HashSet;


/**
 * @author Lawrence Cabac, Konstantin Moellers (1kmoelle)
 *
 * SimpleFileFilters can be combined into new combinations of FileFilters.
 * By adding SimpleFileFilters the Combination will filter all except for those listed.
 * CombinationFileFilter can be added to a new Combination but not to the same.
 * Beware of multiple linked recursions.
 */
public class CombinationFileFilter extends SimpleFileFilter implements java.io.FileFilter {
    private final HashSet<SimpleFileFilter> filterList;
    private SimpleFileFilter preferredFileFilter;
    private String description;
    private boolean allowDirectory = true;
    private boolean allowHidden = false;

    public CombinationFileFilter(String description) {
        filterList = new HashSet<SimpleFileFilter>();
        setDescription(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Same Filter should not be addable. fix!
    // Duplicate Filter should not be addable. fix!
    public boolean add(SimpleFileFilter ff) {
        boolean result = false;
        if (!contains(ff)) {
            if (filterList.isEmpty()) {
                preferredFileFilter = ff;
            }
            result = filterList.add(ff);
        }
        return result;
    }

    public boolean addAll(Collection<SimpleFileFilter> filters) {
        boolean result = false;
        for (SimpleFileFilter ff : filters) {
            result = add(ff) || result;
        }
        return result;
    }

    public boolean contains(SimpleFileFilter filter) {
        boolean result = false;
        for (SimpleFileFilter ff : filterList) {
            if (filter != null && filter.equals(ff)) {
                result = true;
            }
        }
        return result;
    }

    public void remove(SimpleFileFilter ff) {
        filterList.remove(ff);
    }

    public HashSet<SimpleFileFilter> getFileFilters() {
        return filterList;
    }

    public String getExtension() {
        return getPreferedFileFilter().getExtension();
    }

    public void allowHidden(boolean b) {
        allowHidden = b;
    }

    public void allowDirectory(boolean b) {
        allowDirectory = b;
    }

    public boolean isHiddenAllowed() {
        return allowHidden;
    }

    public boolean isDirectoryAllowed() {
        return allowDirectory;
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        // Never accept null files.
        if (f == null) {
            return false;
        }

        // Check for a hidden file.
        if (!allowHidden && (f.isHidden() || f.getName().startsWith("."))) {
            return false;
        }

        // Check for directories.
        if (f.isDirectory()) {
            return allowDirectory;
        }

        // Check if one of the contained filters accepts the file.
        for (SimpleFileFilter filter : filterList) {
            if (filter.accept(f)) {
                return true;
            }
        }

        return false;
    }

    public SimpleFileFilter getPreferedFileFilter() {
        return preferredFileFilter;
    }

    public void setPreferedFileFilter(SimpleFileFilter filter) {
        preferredFileFilter = filter;
    }

    public boolean isEmpty() {
        return filterList.isEmpty();
    }

    /* (non-Javadoc)
    * @see javax.swing.filechooser.FileFilter#getDescription()
    */
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CombinationFileFilter) {
            CombinationFileFilter other = (CombinationFileFilter) o;
            if (getDescription().equals(other.getDescription())
                        && getFileFilters().equals(other.getFileFilters())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return CombinationFileFilter.class.hashCode()
               ^ getDescription().hashCode() ^ getFileFilters().hashCode();
    }
}