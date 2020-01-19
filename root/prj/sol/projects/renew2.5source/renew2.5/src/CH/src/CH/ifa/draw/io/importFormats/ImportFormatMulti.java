package CH.ifa.draw.io.importFormats;



/**
 * Container for ImportFormats. Is an ImportFormat itself.
 * Can be used to arrange ImportFormats in groups.
 */
public interface ImportFormatMulti extends ImportFormat {

    /**
     * Adds a format to the MultiFormat.
     * @require importFormat != null.
     * @param format The format to be added to MultiFormat.
     */
    public void addImportFormat(ImportFormat importFormat);

    /**
     * Remove a format from the MultiFormat.
     * @param importFormat the ImportFormat to be removed.
     */
    public void removeImportFormat(ImportFormat importFormat);

    /**
     * Returns all formats in the MultiFormat.
     * @ensure result != null.
     * @return ImportFormat[] array of all ImportFormats in MultiFormat.
     */
    public ImportFormat[] allImportFormats();
}