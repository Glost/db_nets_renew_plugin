package CH.ifa.draw.framework;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.standard.NullDrawingEditor;

import CH.ifa.draw.util.Command;


/**
 * An abstract superclass for commands whose effects
 * should be undoable. Before the command's body is
 * executed, an undo snapshot of the currently active
 * drawing will be appended to the undo history.
 * <p>
 * The undo mechanism covers modification of figures only.
 * This does not include the selection state.
 * </p>
 * UndoableCommand.java
 * Created: Wed Dec 6  2000
 * @author Julia Hagemeister, Michael Duvigneau
 *
 * @see CH.ifa.draw.framework.UndoRedoManager
 */
public abstract class UndoableCommand extends Command {
    //    private DrawingEditor fEditor;

    /**
     * Constructs a command with the given name.
     */
    public UndoableCommand(String name) {
        super(name);
        // fEditor = editor;
    }

    /**
     * Executes the command.
     * This method should not be overridden by subclasses
     * unless they care about taking snapshots on their own.
     * Use executeUndoable() instead.
     * @see #executeUndoable
     **/
    public void execute() {
        DrawingEditor editor = getEditor();
        editor.prepareUndoSnapshot();
        boolean changed = executeUndoable();
        if (changed) {
            editor.commitUndoSnapshot();
        } else {
            //fEditor.forgetUndoSnapshot();	  
        }
    }

    protected DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        if (!super.isExecutable()) {
            return false;
        }
        if (getEditor() == NullDrawingEditor.INSTANCE) {
            return false;
        }
        return true;
    }

    /**
     * Executes the command.
     * This method should contain the core functionality,
     * it is called by execute().
     * An undo snapshot has already been taken when this
     * method is called.
     *
     * @return <code>true</code> if the drawing's figures were modified,
     *         <code>false</code> otherwise.
     */
    protected abstract boolean executeUndoable();
}