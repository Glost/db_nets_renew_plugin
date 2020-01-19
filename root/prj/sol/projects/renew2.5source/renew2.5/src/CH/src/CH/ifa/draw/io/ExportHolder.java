package CH.ifa.draw.io;

import CH.ifa.draw.io.exportFormats.ExportFormat;


/**
 * This interface must be implemented if you want to add new ExportFormats to Renew.
 */
public interface ExportHolder {

    /**
     * Adds an ExportFormat to Renew.
     * @require exportFormat != null.
     * @param exportFormat The ExportFormat which is added to Renew.
     */
    public void addExportFormat(ExportFormat exportFormat);

    /**
     * Removes an ExportFormat from Renew.
     * @param formatId The ID of the ExportFormat to be removed.
     */
    public void removeExportFormat(ExportFormat exportFormat);

    /**
     * Returns an array that contains all ExportFormats added to Renew.
     * @ensure result != null.
     * @return Iterator, Contains all ExportFormats added to Renew.
     */
    public ExportFormat[] allExportFormats();
}