package CH.ifa.draw.io;

import CH.ifa.draw.io.importFormats.ImportFormat;


/**
 * This interface must be implemented if you want to add new ImportFormats to Renew.
 */
public interface ImportHolder {

    /**
     * Adds an ImportFormat to Renew.
     * @require importFormat != null.
     * @param importFormat The ImportFormat which is added to Renew.
     */
    public void addImportFormat(ImportFormat importFormat);

    /**
     * Removes an ImportFormat from Renew.
     * @param format The importFormat to be removed.
     */
    public void removeImportFormat(ImportFormat format);

    /**
     * Returns an iterator that contains all ImportFormats added to Renew.
     * @ensure result != null.
     * @return ImportFormat[] Contains all ImportFormats added to Renew.
     */
    public ImportFormat[] allImportFormats();
}