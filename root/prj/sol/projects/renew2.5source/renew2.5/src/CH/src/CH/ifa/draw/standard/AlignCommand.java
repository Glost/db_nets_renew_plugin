/*
 * @(#)AlignCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import java.awt.Rectangle;


/**
 * Align a selection of figures relative to each other.
 */
public class AlignCommand extends UndoableCommand {

    /**
     * align left sides
     */
    public final static int LEFTS = 0;

    /**
     * align centers (horizontally)
     */
    public final static int CENTERS = 1;

    /**
     * align right sides
     */
    public final static int RIGHTS = 2;

    /**
     * align tops
     */
    public final static int TOPS = 3;

    /**
     * align middles (vertically)
     */
    public final static int MIDDLES = 4;

    /**
     * align bottoms
     */
    public final static int BOTTOMS = 5;
    public final static int ANCHOR_LAST = 0;
    public final static int ANCHOR_FIRST = 1;
    public final static int ANCHOR_BIGGEST = 2;
    public final static int ANCHOR_SMALLEST = 3;
    public final static int ANCHOR_SELECTION = 4;

    // protected DrawingEditor fEditor;
    private int fOp;
    private int fAnchor;

    /**
     * Constructs an alignment command.
     *
     * @param name
     *            the command name
     * @param op
     *            the alignment operation (LEFTS, CENTERS, RIGHTS, etc.)
     */
    public AlignCommand(String name, int op) {
        this(name, op, ANCHOR_FIRST);
    }

    /**
     * Constructs an alignment command.
     *
     * @param name
     *            the command name
     * @param op
     *            the alignment operation (LEFTS, CENTERS, RIGHTS, etc.)
     * @param anchor
     *                           the anchor for the alignment (ANCHOR_LAS, ANCHOR_FIRST,
     *                           ANCHOR_BIGGEST, ANCHOR_SMALLEST, ANCHOR_SELECTION)
     */
    public AlignCommand(String name, int op, int anchor) {
        super(name);
        fOp = op;
        fAnchor = anchor;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 1;
    }

    public boolean executeUndoable() {
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            DrawingView view = getEditor().view();
            Rectangle r = getAnchor(view, fAnchor, fOp);

            FigureEnumeration selection = view.selectionElements();
            while (selection.hasMoreElements()) {
                Figure f = selection.nextFigure();
                Rectangle rr = f.displayBox();
                switch (fOp) {
                case LEFTS:
                    f.moveBy(r.x - rr.x, 0);
                    break;
                case CENTERS:
                    f.moveBy((r.x + r.width / 2) - (rr.x + rr.width / 2), 0);
                    break;
                case RIGHTS:
                    f.moveBy((r.x + r.width) - (rr.x + rr.width), 0);
                    break;
                case TOPS:
                    f.moveBy(0, r.y - rr.y);
                    break;
                case MIDDLES:
                    f.moveBy(0, (r.y + r.height / 2) - (rr.y + rr.height / 2));
                    break;
                case BOTTOMS:
                    f.moveBy(0, (r.y + r.height) - (rr.y + rr.height));
                    break;
                }
            }
            view.checkDamage();
            return true;
        }
        return false;
    }

    private Rectangle getAnchor(DrawingView view, int anchor, int op) {
        Rectangle result = null;
        FigureEnumeration selection = view.selectionElements();

        if (selection.hasMoreElements()) {
            Rectangle first = selection.nextFigure().displayBox();
            Rectangle biggest = first;
            Rectangle smallest = first;
            Rectangle selectionBox = new Rectangle(first);
            Rectangle last = first;

            while (selection.hasMoreElements()) {
                Rectangle displayBox = selection.nextFigure().displayBox();
                if (op == LEFTS || op == CENTERS || op == RIGHTS) {
                    if (displayBox.width > biggest.width) {
                        biggest = displayBox;
                    }
                    if (displayBox.width < smallest.width) {
                        smallest = displayBox;
                    }
                } else if (op == TOPS || op == MIDDLES || op == BOTTOMS) {
                    if (displayBox.height > biggest.height) {
                        biggest = displayBox;
                    }
                    if (displayBox.height < smallest.height) {
                        smallest = displayBox;
                    }
                }
                selectionBox.add(displayBox);
                if (!selection.hasMoreElements()) {
                    last = displayBox;
                }
            }

            switch (anchor) {
            case (ANCHOR_BIGGEST):
                result = biggest;
                break;
            case (ANCHOR_SMALLEST):
                result = smallest;
                break;
            case (ANCHOR_FIRST):
                result = first;
                break;
            case (ANCHOR_LAST):
                result = last;
                break;
            case (ANCHOR_SELECTION):
                result = selectionBox;
                break;
            default:
                result = first;
                break;
            }
        }
        return result;
    }
}