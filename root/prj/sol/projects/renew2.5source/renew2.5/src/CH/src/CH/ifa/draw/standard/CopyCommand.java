/*
 * @(#)CopyCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;



/**
 * Copy the selection to the clipboard.
 * @see CH.ifa.draw.util.Clipboard
 */
public class CopyCommand extends FigureTransferCommand {

    /**
     * Constructs a copy command.
     * @param name the command name
     */
    public CopyCommand(String name) {
        super(name);
    }

    public boolean executeUndoable() {
        copySelection();
        return false;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }
}