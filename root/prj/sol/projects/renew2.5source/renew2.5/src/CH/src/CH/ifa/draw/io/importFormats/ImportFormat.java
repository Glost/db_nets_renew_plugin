package CH.ifa.draw.io.importFormats;

import CH.ifa.draw.framework.Drawing;

import java.net.URI;
import java.net.URL;

import javax.swing.filechooser.FileFilter;


/**
 * This interface must be implemented to define new ImportFormats.
 */
public interface ImportFormat {

    /**
     * Converts an array of files into a array of drawings.
     * @require files != null.
     * @ensure result != null.
     * @throws Exception is catched by the ImportHolder.
     * @param files array of files to be imported.
     * @return Drawing[] array of drawings.
     */
    public Drawing[] importFiles(URL[] paths) throws Exception;

    /**
     * Returns the FileFilter for the ImportFormat.
     * @ensure result != null.
     * @return FileFilter the FileFilter for the ImportFormat.
     */
    public FileFilter fileFilter();

    /**
     * Returns a name for the ImportFormat e.g. PNML.
     * @ensure result != null.
     * @return String the Name of the ImportFormat.
     */
    public String formatName();

    /**
     * Checks if the file can be imported.
     * @param file file to be checked.
     * @return boolean true: import possible; false: import NOT possible.
     */
    public boolean canImport(URL path);

    /**
     * Checks if the file can be imported.
     * @param path URI to be checked.
     * @return boolean true: import possible; false: import NOT possible.
     */
    public boolean canImport(URI path);
}