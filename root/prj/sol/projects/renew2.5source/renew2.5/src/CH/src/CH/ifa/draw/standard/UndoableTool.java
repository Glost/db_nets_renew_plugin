package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingEditor;


/**
 * Default implementation for tools with undoable effects.
 *
 * <p>
 * The implementation depends on a call do editor.toolDone()
 * whenever an undoable effect is complete.
 * When the tool is activated, the current state of the drawing
 * is saved (prepareUndoSnapshot). On deactivation the snapshot
 * is committed to the undo list.
 * </p><p>
 * Subclasses must report changes to the drawing via changesMade()
 * before deactivation.
 * The commit can be avoided if the Tool called noChangesMade()
 * (this is the default behaviour).
 * </p>
 *
 * UndoableTool.java
 * Created: Wed Jan 10  2001
 *
 * @author Julia Hagemeister
 * @author Michael Duvigneau
 *
 * @see CH.ifa.draw.framework.UndoRedoManager
 */
public class UndoableTool extends AbstractTool {
    private boolean didChange;

    public UndoableTool(DrawingEditor editor) {
        super(editor);
    }

    public void activate() {
        super.activate();
        fEditor.prepareUndoSnapshot();
        noChangesMade();
    }

    public void deactivate() {
        if (didChange) {
            fEditor.commitUndoSnapshot();
            noChangesMade();
        }
        super.deactivate();
    }

    protected void noChangesMade() {
        didChange = false;
    }

    protected void changesMade() {
        didChange = true;
    }

    protected void intermediateUndoSnapshot() {
        if (didChange) {
            fEditor.commitUndoSnapshot();
            fEditor.prepareUndoSnapshot();
            noChangesMade();
        }
    }
} // UndoableTool
