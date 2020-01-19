package CH.ifa.draw.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * Abstract implementation of the ExportFormat interface.
 */
public abstract class ExportFormatAbstract implements ExportFormat {
    // Attributes
    //	The name of the format.
    private String _formatName;

    //	The FileFilter of the format.
    private FileFilter _fileFilter;

    // Constructor
    public ExportFormatAbstract(String name, FileFilter fileFilter) {
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
        assert (fileFilter() != null) : "Failure in ExportFormatAbstract: fileFilter() == null";
        assert (fileFilter().equals(fileFilter)) : "Failure in ExportFormatAbstract: fileFilter() != fileFilter";
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
        assert (formatName() != null) : "Failure in ExportFormatAbstract: formatName() == null";
        assert (formatName().equals(formatName)) : "Failure in ExportFormatAbstract: formatName() != fielFilter";
    }


    //	---------------------------------------------------------------------
    // Implementation of the ExportFormat interface
    // ---------------------------------------------------------------------


    /**
             * @see de.renew.io.ExportFormat#fileFilter()
             */
    public FileFilter fileFilter() {
        FileFilter result = null;
        result = _fileFilter;
        assert (result != null) : "Failure in ExportFormatAbstract: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportFormat#formatName()
     */
    public String formatName() {
        String result = null;
        result = _formatName;
        assert (result != null) : "Failure in ExportFormatAbstract: result == null";
        return result;
    }


    /**
     * @see de.renew.io.ExportFormat#exportAll(CH.ifa.draw.framework.Drawing[], java.net.URI[])
     */
    public File[] exportAll(Drawing[] drawings, File[] path)
            throws Exception {
        File[] result = null;
        if (drawings != null && path != null) {
            if (drawings.length == path.length) {
                result = new File[path.length];
                for (int pos = 0; pos < drawings.length; pos++) {
                    result[pos] = export(drawings[pos], path[pos]);
                }
            }
        }
        assert (result != null) : "Failure in ExportFormatAbstract : result == null";
        return result;
    }

    public ExportFormat[] canExport(File path) {
        ExportFormat[] result = null;
        if (path != null) {
            if (fileFilter().accept(path)) {
                result = new ExportFormat[] { this };
            } else {
                result = new ExportFormat[0];
            }
        }
        assert (result != null) : "Failure in ExportFormatAbstract: result == null";
        return result;
    }

    public int getShortCut() {
        return -1;
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.io.exportFormats.ExportFormat#getModifier()
     */
    public int getModifier() {
        return -1;
    }

    public boolean forceGivenName() {
        return false;
    }

    public String toString() {
        String result = "";
        result = formatName();
        return result;
    }
}