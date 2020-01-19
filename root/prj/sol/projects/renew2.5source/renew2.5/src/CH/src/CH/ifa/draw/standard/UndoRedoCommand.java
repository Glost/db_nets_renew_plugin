package CH.ifa.draw.standard;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.SnapshotHistory;

import CH.ifa.draw.util.Command;


/**
 * Restores one undo or redo step.
 *
 * UndoRedoCommand.java
 * Created: Wed Dec 13  2000
 * @author Michael Duvigneau
 *
 * @see CH.ifa.draw.framework.UndoRedoManager
 */
public class UndoRedoCommand extends Command {
    public static final int UNDO = 1;
    public static final int REDO = 2;

    // private DrawingEditor editor;
    private int mode;

    public UndoRedoCommand(String name, int mode) {
        super(name);
        // this.editor = editor;
        this.mode = mode;
    }

    private DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        if (getEditor() == NullDrawingEditor.INSTANCE) {
            return false;
        }
        SnapshotHistory history = null;
        switch (mode) {
        case UNDO:
            history = getEditor().getUndoRedoManager()
                          .getUndoHistory(getEditor().drawing());
            break;
        case REDO:
            history = getEditor().getUndoRedoManager()
                          .getRedoHistory(getEditor().drawing());
            break;
        }
        return (history != null) && (!history.isEmpty());
    }

    public void execute() {
        if (isExecutable()) {
            getEditor().toolDone();
            switch (mode) {
            case UNDO:
                getEditor().getUndoRedoManager()
                    .restoreUndoSnapshot(getEditor().drawing());
                getEditor().selectionChanged(getEditor().view());
                break;
            case REDO:
                getEditor().getUndoRedoManager()
                    .restoreRedoSnapshot(getEditor().drawing());
                getEditor().selectionChanged(getEditor().view());
                break;
            }
        }
    }
} // UndoRedoCommand
