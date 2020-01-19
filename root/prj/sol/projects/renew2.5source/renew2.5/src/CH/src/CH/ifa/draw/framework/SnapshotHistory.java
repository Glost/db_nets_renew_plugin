package CH.ifa.draw.framework;

import java.util.Vector;


/**
 * Keeps a history of snapshots of a drawing.
 * Provides methods to take a new snapshot and to
 * restore the latest snapshot, the affected drawing
 * is specified at the constructor call.
 * <p>
 * The history is limited to <code>maxSize</code>
 * snapshots, the oldest snapshot will be removed if
 * the limit is exceeded. Currently the limit cannot
 * be modified after instantiation.
 * </p>
 *
 * SnapshotHistory.java
 * Created: Wed Dec 13  2000
 * @author Michael Duvigneau
 *
 * @see UndoRedoManager
 */
public class SnapshotHistory {
    private Vector<FigureSelection> history;
    private int maxSize;
    private Drawing drawing;
    private FigureSelection preparedSnapshot;

    /**
     * Creates a new snapshot history for the given drawing.
     *
     * @param drawing   The history will contain snapshots of
     *                  this drawing only.
     * @param maxSize   Limits the number of snapshots the
     *                  history will remember.
     **/
    public SnapshotHistory(Drawing drawing, int maxSize) {
        this.maxSize = maxSize;
        this.drawing = drawing;
        this.preparedSnapshot = null;
        this.history = new Vector<FigureSelection>(maxSize);
    }

    /**
     * Takes a snapshot of all figures which are currently part
     * of the drawing and appends it to the history.
     * If the history limit is exceeded, the oldest snapshot will
     * be removed from the history.
     **/
    public void takeSnapshot() {
        prepareSnapshot();
        commitSnapshot();
    }

    /**
     * Prepares a snapshot for commitSnapshot().
     * Any previously prepared snapshot will be forgotten.
     **/
    public void prepareSnapshot() {
        preparedSnapshot = new FigureSelection(drawing.figures());
    }

    /**
     * Takes the last prepared snapshot of all figures which are
     * currently part of the drawing and appends it to the history.
     * If the history limit is exceeded, the oldest snapshot will
     * be removed from the history.
     **/
    public void commitSnapshot() {
        if (preparedSnapshot != null) {
            history.addElement(preparedSnapshot);
            preparedSnapshot = null;
            if (history.size() > maxSize) {
                history.removeElementAt(0);
            }
        }
    }

    /**
     * Restores the latest snapshot of the drawing.
     * Replaces all figures currently contained in the drawing
     * by the figures in the snapshot.
     *
     * If the history is empty, nothing will happen.
     */
    public void restoreSnapshot() {
        if (history.size() > 0) {
            FigureSelection snapshot = history.lastElement();
            history.removeElementAt(history.size() - 1);

            drawing.removeAll();

            Vector<Figure> restoreFigures = snapshot.getData(FigureSelection.TYPE);
            drawing.addAll(restoreFigures);

            drawing.checkDamage();
        }
    }

    /**
     * Removes all snapshots from the history.
     **/
    public void clear() {
        history.removeAllElements();
    }

    /**
     * Tells whether the the history is empty.
     **/
    public boolean isEmpty() {
        return history.isEmpty();
    }
} // UndoHistory
