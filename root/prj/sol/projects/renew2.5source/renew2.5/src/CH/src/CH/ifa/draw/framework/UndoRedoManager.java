package CH.ifa.draw.framework;

import java.util.Hashtable;


/**
 * Manages undo and redo histories for each drawing.
 * <p>
 * The undo support works as follows:
 * </p>
 * <p>
 * If a drawing is created/loaded, an undo history
 * should immediately be created by {@link #newUndoHistory}.
 * The {@link CH.ifa.draw.application.DrawApplication}
 * does this by default when a drawing is added. If the
 * undo support is not wanted for a particular drawing,
 * it can be disabled by a call to
 * {@link CH.ifa.draw.application.DrawApplication#noUndoHistoryFor}.
 * </p>
 * <p>
 * Each action/command/tool/handle/... which modifies
 * the drawing should <UL>
 * <LI> prepare an undo snapshot <b>before</b> the modifications
 *      are applied to the drawing ({@link #prepareUndoSnapshot}) </LI>
 * <LI> commit the snapshot after the changes took place
 *      ({@link #commitUndoSnapshot}) </LI>
 * <LI> just omit the 2nd step if no changes were made. </LI>
 * </UL>
 * For commands and tools, there exist classes which can be
 * inherited ({@link UndoableCommand}, {@link CH.ifa.draw.standard.UndoableTool}).
 * They provide a default behavior which implements the steps above.
 * For Handles, the implementation of {@link CH.ifa.draw.standard.AbstractHandle}
 * also provides a default undo support.
 * </p>
 * <p>
 * Undo and redo snapshots are restored by the methods
 * {@link #restoreUndoSnapshot} and {@link #restoreRedoSnapshot}.
 * The {@link CH.ifa.draw.standard.UndoRedoCommand}
 * uses these methods.
 * </p>
 * UndoRedoManager.java
 * Created: Wed Jan 31  2001
 * @author Michael Duvigneau
 * @author Julia Hagemeister
 */
public class UndoRedoManager {

    /**
     * The maximum number of actions which can be undone.
     * This value is used twice per each drawing (undo and redo).
     **/
    private static final int UNDOSTEPS = 10;

    /**
     * Contains a SnapshotHistory-Object for each drawing.
     * <p>
     * The history object should be instantiated and added to
     * the table immediately when a drawing is added.
     * </p>
     **/
    private Hashtable<Drawing, SnapshotHistory> undoHistoryTable = new Hashtable<Drawing, SnapshotHistory>();

    /**
     * Contains a SnapshotHistory-Object for each drawing.
     * @see #undoHistoryTable
     **/
    private Hashtable<Drawing, SnapshotHistory> redoHistoryTable = new Hashtable<Drawing, SnapshotHistory>();

    /**
     * Refers to the drawing editor, needed to give feedback to the user.
     **/
    private DrawingEditor editor;

    public UndoRedoManager(DrawingEditor editor) {
        this.editor = editor;
    }

    /**
     * Takes a snapshot of the given drawing and
     * remembers it until it will be added by {@link #commitUndoSnapshot}.
     * Any previously prepared snapshot will be forgotten.
     **/
    public void prepareUndoSnapshot(Drawing drawing) {
        SnapshotHistory undoHistory = getUndoHistory(drawing);
        if (undoHistory != null) {
            undoHistory.prepareSnapshot();
        }
    }

    /**
     * Takes the last prepared snapshot and
     * appends it to the undo history of the given drawing.
     * The redo history is cleared.
     **/
    public void commitUndoSnapshot(Drawing drawing) {
        SnapshotHistory undoHistory = getUndoHistory(drawing);
        SnapshotHistory redoHistory = getRedoHistory(drawing);
        if (undoHistory != null) {
            undoHistory.commitSnapshot();
            redoHistory.clear();
            editor.menuStateChanged();
        }
    }

    /**
     * Restores the drawing to the state saved by the last
     * call to {@link #commitUndoSnapshot}.
     * Additional calls to this method will restore more
     * undo snapshots step by step, until the history is empty.
     *
     * The effect can be undone by a call to {@link #restoreUndoSnapshot}.
     **/
    public void restoreUndoSnapshot(Drawing drawing) {
        SnapshotHistory undoHistory = getUndoHistory(drawing);
        SnapshotHistory redoHistory = getRedoHistory(drawing);
        if (undoHistory == null) {
            // do nothing
        } else if (!undoHistory.isEmpty()) {
            redoHistory.takeSnapshot();
            undoHistory.restoreSnapshot();


            // editor.menuStateChanged(); not needed because drawing changed
            editor.showStatus("Undone.");
        } else {
            editor.showStatus("Nothing to undo.");
        }
    }

    /**
     * Restores the drawing to the state it had before the last undo.
     **/
    public void restoreRedoSnapshot(Drawing drawing) {
        SnapshotHistory undoHistory = getUndoHistory(drawing);
        SnapshotHistory redoHistory = getRedoHistory(drawing);
        if (redoHistory == null) {
            // do nothing
        } else if (!redoHistory.isEmpty()) {
            undoHistory.takeSnapshot();
            redoHistory.restoreSnapshot();


            // editor.menuStateChanged(); not needed because drawing changed
            editor.showStatus("Redone.");
        } else {
            editor.showStatus("Nothing to redo.");
        }
    }

    /**
     * Returns the undo history for the given drawing.
     * May return <code>null</code> if there is no history kept for the drawing.
     **/
    public SnapshotHistory getUndoHistory(Drawing drawing) {
        return undoHistoryTable.get(drawing);
    }

    /**
     * Returns the redo history for the given drawing.
     * May return <code>null</code> if there is no history kept for the drawing.
     **/
    public SnapshotHistory getRedoHistory(Drawing drawing) {
        return redoHistoryTable.get(drawing);
    }

    /**
     * Clears undo <b>and</b> redo history for the given drawing.
     **/
    public void clearUndoHistory(Drawing drawing) {
        SnapshotHistory undoHistory = undoHistoryTable.get(drawing);
        SnapshotHistory redoHistory = redoHistoryTable.get(drawing);
        if (undoHistory != null) {
            undoHistory.clear();
            redoHistory.clear();
            editor.menuStateChanged();
        }
    }

    /**
     * Enables the undo/redo history management for the given drawing.
     **/
    public void newUndoHistory(Drawing drawing) {
        SnapshotHistory undoHistory = undoHistoryTable.get(drawing);
        if (undoHistory == null) {
            undoHistoryTable.put(drawing,
                                 new SnapshotHistory(drawing, UNDOSTEPS));
            redoHistoryTable.put(drawing,
                                 new SnapshotHistory(drawing, UNDOSTEPS));
        }
    }

    /**
     * Prohibits the management of undo and redo snapshots
     * for the given drawing.
     **/
    public void removeUndoHistory(Drawing drawing) {
        undoHistoryTable.remove(drawing);
        redoHistoryTable.remove(drawing);
        editor.menuStateChanged();
    }
}