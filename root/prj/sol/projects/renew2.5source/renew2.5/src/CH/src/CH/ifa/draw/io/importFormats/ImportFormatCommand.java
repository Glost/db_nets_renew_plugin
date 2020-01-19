/*
 * Created on 13.01.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package CH.ifa.draw.io.importFormats;

import CH.ifa.draw.util.Command;


/**
 * @author 9tell
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class ImportFormatCommand extends Command {
    // Attributes
    // The ImportFormat
    private ImportFormat _format;

    // Constructor
    public ImportFormatCommand(ImportFormat format) {
        super(format.formatName());
        setFormat(format);
    }

    // Methods


    /**
     * Returns the FormatId.
     * @ensure result != null.
     * @return FormatId the formatId.
     */
    public ImportFormat format() {
        ImportFormat result = null;
        result = _format;
        assert (result != null) : "Failure in GuiPlugin.FormatCommand: result == null";
        return result;
    }

    /**
     * Sets _formatId to formatId.
     * @ensure formatId() != null.
     * @param formatId the value to be set.
     */
    private void setFormat(ImportFormat format) {
        _format = format;
        assert (format() != null) : "Failure in GuiPlugin.FormatCommand: formatId() == null";
    }
}