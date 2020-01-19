package CH.ifa.draw.io.exportFormats;

import CH.ifa.draw.util.Command;


public abstract class ExportFormatCommand extends Command {
    // Attributes
    // The ImportFormat
    private ExportFormat _format;

    // Constructor
    public ExportFormatCommand(ExportFormat format, String append) {
        super(format.formatName() + append);
        setFormat(format);
    }

    // Methods


    /**
     * Returns the FormatId.
     * @ensure result != null.
     * @return FormatId the formatId.
     */
    public ExportFormat format() {
        ExportFormat result = null;
        result = _format;
        assert (result != null) : "Failure in GuiPlugin.FormatCommand: result == null";
        return result;
    }

    /**
     * Sets _formatId to formatId.
     * @ensure formatId() != null.
     * @param formatId the value to be set.
     */
    private void setFormat(ExportFormat format) {
        _format = format;
        assert (format() != null) : "Failure in GuiPlugin.FormatCommand: formatId() == null";
    }

    public abstract boolean isExecutable();
}