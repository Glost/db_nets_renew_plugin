/*
 * @(#)HandleTracker.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Handle;

import java.awt.event.MouseEvent;


/**
 * HandleTracker implements interactions with the handles
 * of a Figure.
 *
 * @see SelectionTool
 */
public class HandleTracker extends AbstractTool {
    private Handle fAnchorHandle;

    public HandleTracker(DrawingEditor editor, Handle anchorHandle) {
        super(editor);
        fAnchorHandle = anchorHandle;
    }

    public void mouseDown(MouseEvent e, int x, int y) {
        super.mouseDown(e, x, y);
        fAnchorHandle.invokeStart(e, x, y, view());
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
        super.mouseDrag(e, x, y);
        fAnchorHandle.invokeStep(e, x, y, fAnchorX, fAnchorY, view());
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        super.mouseDrag(e, x, y);
        fAnchorHandle.invokeEnd(e, x, y, fAnchorX, fAnchorY, view());
    }
}