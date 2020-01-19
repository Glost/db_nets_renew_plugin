package CH.ifa.draw.io.exportFormats;



/**
 * Container for ExportFormats. Is an ExportFormat itself.
 * Can be used to arrange ExportFormats in groups.
 */
public interface ExportFormatMulti extends ExportFormat {

    /**
      * Adds a format to the MultiFormat.
      * @require format != null.
      * @ensure format.equals(getExportFormat(format.formatId())) == true.
      * @param format The format to be added to MultiFormat.
      */
    public void addExportFormat(ExportFormat format);

    /**
      * Remove a format from the MultiFormat.
      * @ensure getExportFormat(formatId) == null.
      * @param formatId the Id of the format to be removed.
      */
    public void removeExportFormat(ExportFormat format);


    /**
     * Returns all formats in the MultiFormat.
     * @ensure result != null.
     * @return Iterator All formats in MultiFormat.
     */
    public ExportFormat[] allExportFormats();
}