/*
 * @(#)ConnectedTextTool.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.standard.TextHolder;

import java.awt.event.MouseEvent;


/**
 * Tool to create new or edit existing text figures.
 * A new text figure is connected with the clicked figure.
 *
 * @see TextHolder
 */
public class ConnectedTextTool extends TextTool {
    boolean fConnected = false;
    private boolean fMustConnect;

    public ConnectedTextTool(DrawingEditor editor, TextFigure prototype) {
        this(editor, prototype, true);
    }

    public ConnectedTextTool(DrawingEditor editor, TextFigure prototype,
                             boolean mustConnect) {
        super(editor, prototype);
        fMustConnect = mustConnect;
    }

    /**
     * If the pressed figure is a TextHolder it can be edited otherwise
     * a new text figure is created.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        if (fConnected || isTypingActive()) {
            super.mouseDown(e, x, y);
        } else {
            Figure pressedFigure = drawing().findFigureInside(x, y);
            boolean pressedEditable = (pressedFigure instanceof TextHolder)
                                      && ((TextHolder) pressedFigure)
                                          .acceptsTyping();

            if (pressedFigure instanceof ParentFigure
                        && ((ChildFigure) getPrototype()).canBeParent((ParentFigure) pressedFigure)) {
                super.mouseDown(e, x, y);
                ChildFigure child = (ChildFigure) createdFigure();
                if (child != null) {
                    child.setParent((ParentFigure) pressedFigure);
                    fConnected = true;
                }
            } else if (!fMustConnect || pressedEditable) {
                super.mouseDown(e, x, y);
            }
        }
    }

    /**
     * If the pressed figure is a TextHolder it can be edited otherwise
     * a new text figure is created.
     */
    public void activate() {
        super.activate();
        fConnected = false;
    }
}