package CH.ifa.draw.io.importFormats;

import java.io.File;

import java.net.URI;
import java.net.URL;

import javax.swing.filechooser.FileFilter;


/**
 * Abstract implementation of the ImportFormat interface.
 */
public abstract class ImportFormatAbstract implements ImportFormat {
    // Attributes
    // The name of the format.
    private String _formatName;

    // The FileFilter of the format.
    private FileFilter _fileFilter;

    // constructor
    public ImportFormatAbstract(String name, FileFilter fileFilter) {
        setFormatName(name);
        setFileFilter(fileFilter);
    }

    // Methods


    /**
     * Sets _fileFilter to fileFilter.
     * @require fileFilter != null
     * @ensure fileFilter() != null
     * @ensure fileFilter().equals(fileFilter)
     * @param fileFilter the value to be set.
     */
    protected void setFileFilter(FileFilter fileFilter) {
        _fileFilter = fileFilter;
        assert (fileFilter() != null) : "Failure in ImportFormatAbstract: fileFilter() == null";
        assert (fileFilter().equals(fileFilter)) : "Failure in ImportFormatAbstract: fileFilter() != fileFilter";
    }

    /**
     * Sets _formatName to formatName.
     * @require formatName != null
     * @ensure formatName() != null
     * @ensure formatName().equals(formatName)
     * @param formatName the value to be set.
     */
    protected void setFormatName(String formatName) {
        _formatName = formatName;
        assert (formatName() != null) : "Failure in ImportFormatAbstract: formatName() == null";
        assert (formatName().equals(formatName)) : "Failure in ImportFormatAbstract: formatName() != fileFilter";
    }

    // ---------------------------------------------------------------------
    // Implementation of the ImportFormat interface
    // ---------------------------------------------------------------------


    /**
         * @see de.renew.io.ImportFormat#fileFilter()
         */
    public FileFilter fileFilter() {
        FileFilter result = null;
        result = _fileFilter;
        assert (result != null) : "Failure in ImportFormatAbstract: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ImportFormat#formatName()
     */
    public String formatName() {
        String result = null;
        result = _formatName;
        assert (result != null) : "Failure in ImportFormatAbstract: result == null";
        return result;
    }


    /**
     * @see de.renew.io.ImportFormat#canImport(java.io.File)
     */
    public boolean canImport(URL path) {
        boolean result = false;
        if (fileFilter().accept(new File(path.getFile()))) {
            result = true;
        }
        return result;
    }

    /**
     * @see de.renew.io.ImportFormat#canImport(java.net.URI)
     */
    public boolean canImport(URI path) {
        boolean result = false;
        if (fileFilter().accept(new File(path.getPath()))) {
            result = true;
        }
        return result;
    }

    public String toString() {
        String result = "";
        result = formatName();
        return result;
    }
}