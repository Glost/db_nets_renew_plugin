/*
 * @(#)DeleteCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;



/**
 * Command to delete the selection.
 */
public class DeleteCommand extends FigureTransferCommand {

    /**
     * Constructs a delete command.
     * @param name the command name
     */
    public DeleteCommand(String name) {
        super(name);
    }

    public boolean executeUndoable() {
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