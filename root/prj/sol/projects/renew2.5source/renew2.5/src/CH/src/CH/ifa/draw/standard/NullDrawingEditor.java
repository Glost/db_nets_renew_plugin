/*
 * @(#)DrawingEditor.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.application.DrawingViewContainer;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.framework.UndoRedoManager;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;


public class NullDrawingEditor implements DrawingEditor {
    public static NullDrawingEditor INSTANCE = new NullDrawingEditor();

    private NullDrawingEditor() {
    }

    public DrawingView view() {
        return NullDrawingView.INSTANCE;
    }

    public DrawingView previousView() {
        return NullDrawingView.INSTANCE;
    }

    public Drawing drawing() {
        return NullDrawing.INSTANCE;
    }

    public Tool tool() {
        return NullTool.INSTANCE;
    }

    public Tool defaultTool() {
        return null;
    }

    public void setStickyTools(boolean sticky) {
    }

    public boolean isStickyTools() {
        return false;
    }

    public void dispatchEvent(KeyEvent evt) {
    }

    public Image getIconImage() {
        return null;
    }

    public void toolDone() {
    }

    public void selectionChanged(DrawingView view) {
    }

    public void menuStateChanged() {
    }

    public void showStatus(String string) {
    }

    /**
     *
     * @param drawing
     * @return
     */
    public CH.ifa.draw.application.DrawingViewFrame showDrawingViewFrame(Drawing drawing) {
        // No such frame available.
        return null;
    }

    public void drawingViewContainerActivated(DrawingViewContainer viewContainer) {
    }

    public void drawingViewContainerClosing(DrawingViewContainer viewContainer) {
    }

    public void prepareUndoSnapshot() {
    }

    public void commitUndoSnapshot() {
    }

    public UndoRedoManager getUndoRedoManager() {
        return null;
    }

    public void setCurrentDrawing(DrawingViewContainer dvc) {
    }

    public Dimension getSize() {
        return new Dimension(0, 0);
    }

    public Point getLocationOnScreen() {
        return new Point(0, 0);
    }
}