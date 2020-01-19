/*
 * @(#)DrawingView.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingChangeListener;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureSelection;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Painter;
import CH.ifa.draw.framework.PointConstrainer;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.util.NullGraphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.util.Vector;


// A class that provides a drawing view that does nothing.
// Useful to erase fields that must not be null.
public class NullDrawingView implements DrawingView, ImageObserver,
                                        DrawingChangeListener {
    private static Dimension nullDimension = new Dimension(0, 0);
    private static Point nullPoint = new Point(0, 0);
    public static NullDrawingView INSTANCE = new NullDrawingView();

    private NullDrawingView() {
    }

    public void setEditor(DrawingEditor editor) {
    }

    public Tool tool() {
        return NullTool.INSTANCE;
    }

    public Drawing drawing() {
        return NullDrawing.INSTANCE;
    }

    public void setDrawing(Drawing d) {
    }

    public DrawingEditor editor() {
        return NullDrawingEditor.INSTANCE;
    }

    public Figure add(Figure figure) {
        return figure;
    }

    public Figure remove(Figure figure) {
        return figure;
    }

    public void addAll(Vector<Figure> figures) {
    }

    public void removeAll(Vector<Figure> figures) {
    }

    public Dimension getSize() {
        return nullDimension;
    }

    public Dimension getMinimumSize() {
        return nullDimension;
    }

    public Dimension getPreferredSize() {
        return nullDimension;
    }

    public void setDisplayUpdate(Painter updateStrategy) {
    }

    public Vector<Figure> inZOrder(Vector<Figure> figures) {
        return (new Vector<Figure>(figures));
    }

    public Vector<Figure> selection() {
        return new Vector<Figure>();
    }

    public FigureEnumeration selectionElements() {
        return new FigureEnumerator(new Vector<Figure>());
    }

    public Vector<Figure> selectionZOrdered() {
        return new Vector<Figure>();
    }

    public int selectionCount() {
        return 0;
    }

    public void addToSelection(Figure figure) {
    }

    public void addToSelectionAll(Vector<Figure> figures) {
    }

    public void addToSelectionAll(FigureEnumeration figures) {
    }

    public void removeFromSelection(Figure figure) {
    }

    public void removeFromSelectionAll(Vector<Figure> figures) {
    }

    public void removeFromSelectionAll(FigureEnumeration figures) {
    }

    public void toggleSelection(Figure figure) {
    }

    public void toggleSelectionAll(Vector<Figure> figures) {
    }

    public void toggleSelectionAll(FigureEnumeration figures) {
    }

    public void clearSelection() {
    }

    public void selectionInvalidateHandles() {
    }

    public FigureSelection getFigureSelection() {
        return new FigureSelection(new Vector<Figure>());
    }

    public Handle findHandle(int x, int y) {
        return null;
    }

    public Point lastClick() {
        return nullPoint;
    }

    public void setConstrainer(PointConstrainer p) {
    }

    public PointConstrainer getConstrainer() {
        return null;
    }

    public void checkDamage() {
    }

    public void repairDamage() {
    }

    public void paint(Graphics g) {
    }

    public Image createImage(int width, int height) {
        // Supposedly this method is only called by the update
        // strategy. But I do not call the update strategy and
        // nobody else is supposed to call it. Therefore it
        // should be safe to return a null. 
        return null;
    }

    public Graphics getGraphics() {
        return new NullGraphics();
    }

    public Color getBackground() {
        return Color.white;
    }

    public void setBackground(Color c) {
    }

    public void drawAll(Graphics g) {
    }

    public void invalidateHandles() {
    }

    public void drawHandles(Graphics g) {
    }

    public void drawDrawing(Graphics g) {
    }

    public void drawBackground(Graphics g) {
    }

    public void setCursor(Cursor c) {
    }

    public void freezeView() {
    }

    public void unfreezeView() {
    }

    public void moveSelection(int dx, int dy) {
    }

    public boolean imageUpdate(Image img, int inf, int x, int y, int w, int h) {
        return false;
    }

    public void drawingInvalidated(DrawingChangeEvent event) {
    }

    public void drawingRequestUpdate(DrawingChangeEvent event) {
    }

    public void showElement(Figure fig) {
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        return Printable.NO_SUCH_PAGE;
    }
}