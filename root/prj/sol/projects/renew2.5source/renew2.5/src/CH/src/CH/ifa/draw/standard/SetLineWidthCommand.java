/**
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.figures.AttributeFigure;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;


/**
 * @author Cabac
 *
 */
public class SetLineWidthCommand extends UndoableCommand {
    private int fWidth;

    public SetLineWidthCommand(String name, int i) {
        super(name);
        fWidth = i;
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.framework.UndoableCommand#executeUndoable()
     */
    @Override
    protected boolean executeUndoable() {
        DrawingView view = getEditor().view();
        FigureEnumeration k = view.selectionElements();
        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();
//            Object val = f.getAttribute(AttributeFigure.LINE_WIDTH_KEY);
//            if (val instanceof Integer) {
            f.setAttribute(AttributeFigure.LINE_WIDTH_KEY, fWidth);
//            }
        }
        view.checkDamage();
        return true;
    }

    public boolean isExecutable() {
        if (getEditor() == NullDrawingEditor.INSTANCE) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }
}