/*
 * @(#)CutCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;



/**
 * Delete the selection and move the selected figures to
 * the clipboard.
 * @see CH.ifa.draw.util.Clipboard
 */
public class CutCommand extends FigureTransferCommand {

    /**
     * Constructs a cut command.
     * @param name the command name
     */
    public CutCommand(String name) {
        super(name);
    }

    public boolean executeUndoable() {
        copySelection();
        deleteSelection();
        getEditor().view().checkDamage();
        return true;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }
}