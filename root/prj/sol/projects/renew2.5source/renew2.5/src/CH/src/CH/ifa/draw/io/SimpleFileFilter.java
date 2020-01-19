/*
 * Created on Apr 13, 2003
 */
package CH.ifa.draw.io;

import de.renew.util.StringUtil;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * SimpleFileFilter is the class that is the basis for all FileFilters that
 * can be connected to the file types of the Drawings.
 * <p>
 * The SimpleFileFilter is used by the JFileChooser to determine the files that are displayed.
 * Implementations of SimpleFileFilter provide the extension and the description of the
 * denoted filetype. To define FileFilter that handle multiple extensions see
 * CombinationFileFilter.
 * </p>
 *
 * @author Lawrence Cabac
 */
public class SimpleFileFilter extends FileFilter implements java.io.FileFilter {
    private String ext;
    private String description;
    private boolean allowDirectory = true;
    private boolean allowHidden = false;

    public SimpleFileFilter(String extension, String description) {
        setExtension(extension);
        setDescription(description);
    }

    protected SimpleFileFilter() {
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

    /**
    * A file is accepted by a SimpleFileFilter when its extension equals the extension
    * of the Simple|FileFilter.
    * @return true for a file that is accepted by the FileFileter.
    * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
    */
    @Override
    public boolean accept(File f) {
        if (f != null
                    && (allowHidden
                               || !f.isHidden() && !f.getName().startsWith("."))) {
            if (f.isDirectory()) {
                return allowDirectory;
            }

            String extension = StringUtil.getExtension(f.getName());

            if (extension != null && ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The description of a file type is a human readable identifier that is
     * used in the select box of the JFileCooser.
     * @return The Description of the file type.
     * @see javax.swing.JFileChooser
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @return The extension of the file type.
     */
    public String getExtension() {
        return ext;
    }

    public void setDescription(String string) {
        description = string;
    }

    public void setExtension(String string) {
        ext = string;
    }

    /**
     * Two SimpleFileFilter are equal when the extensions of them are equal.
     * @param o The SimpleFileFilter that is compared.
     * @return true if the extensions of the two SimpleFileFilter are equal.
     */
    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof SimpleFileFilter
               && ((SimpleFileFilter) o).getExtension().equals(getExtension());
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ getExtension().hashCode();
    }
}