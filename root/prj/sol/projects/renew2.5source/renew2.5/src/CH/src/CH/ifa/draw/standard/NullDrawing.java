/*
 * @(#)Drawing.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingChangeListener;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeAdapter;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.io.NoFileFilter;
import CH.ifa.draw.io.SimpleFileFilter;

import CH.ifa.draw.util.Storable;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.File;
import java.io.Serializable;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;


public class NullDrawing extends FigureChangeAdapter implements Drawing,
                                                                Storable,
                                                                Serializable {
    public static NullDrawing INSTANCE = new NullDrawing();

    // I cannot make the constructor private, because I must
    // be storable.
    public NullDrawing() {
    }

    public String getName() {
        return "";
    }

    public void setName(String name) {
    }

    public File getFilename() {
        return null;
    }

    public void setFilename(File filename) {
    }

    // Always claim to backuped, so that no backups are ever made.
    public boolean getBackupStatus() {
        return true;
    }

    public void setBackupStatus(boolean status) {
    }

    public void release() {
    }

    public Rectangle displayBox() {
        return new Rectangle();
    }

    public FigureEnumeration figures() {
        return new FigureEnumerator(new Vector<Figure>());
    }

    public FigureEnumeration figuresReverse() {
        return figures();
    }

    public Figure findFigure(int x, int y) {
        return null;
    }

    public Figure findFigure(Rectangle r) {
        return null;
    }

    public Figure findFigureWithout(int x, int y, Figure without) {
        return null;
    }

    public Figure findFigure(Rectangle r, Figure without) {
        return null;
    }

    public Figure findFigureInside(int x, int y) {
        return null;
    }

    public Figure findFigureInsideWithout(int x, int y, Figure without) {
        return null;
    }

    public void addDrawingChangeListener(DrawingChangeListener listener) {
    }

    public void removeDrawingChangeListener(DrawingChangeListener listener) {
    }

    public Enumeration<DrawingChangeListener> drawingChangeListeners() {
        return new Vector<DrawingChangeListener>().elements();
    }

    public void checkDamage() {
    }

    public Figure add(Figure figure) {
        return figure;
    }

    public Drawing add(Drawing drawing) {
        return drawing;
    }

    public Drawing add(Drawing drawing, int x, int y) {
        return drawing;
    }

    public void addAll(Vector<?extends Figure> newFigures) {
    }

    public Figure remove(Figure figure) {
        return figure;
    }

    public Figure orphan(Figure figure) {
        return figure;
    }

    public void orphanAll(Vector<?extends Figure> newFigures) {
    }

    public void removeAll(Vector<?extends Figure> figures) {
    }

    public void removeAll() {
    }

    public void replace(Figure figure, Figure replacement) {
    }

    public void sendToBack(Figure figure) {
    }

    public void bringToFront(Figure figure) {
    }

    public void draw(Graphics g) {
    }

    public void lock() {
    }

    public void unlock() {
    }

    public void write(StorableOutput so) {
    }

    public void read(StorableInput si) {
    }

    public boolean isModified() {
        return false;
    }

    public void clearModified() {
    }

    public Rectangle getBounds() {
        return new Rectangle(0, 0, 0, 0);
    }

    public Dimension defaultSize() {
        return new Dimension(0, 0);
    }

    public String getWindowCategory() {
        return "<BUG> Null drawings";
    }

    //------------------------------------------------------------------------
    public SimpleFileFilter getDefaultFileFilter() {
        return new NoFileFilter();
    }

    /* (non-Javadoc)
    * @see CH.ifa.draw.framework.Drawing#getFileFilters()
    */
    public HashSet<SimpleFileFilter> getImportFileFilters() {
        HashSet<SimpleFileFilter> hs = new HashSet<SimpleFileFilter>();

        return hs;
    }

    public HashSet<SimpleFileFilter> getExportFileFilters() {
        HashSet<SimpleFileFilter> hs = new HashSet<SimpleFileFilter>();

        //hs.add(getDefaultFileFilter());
        return hs;
    }

    /* (non-Javadoc)
    * @see CH.ifa.draw.framework.Drawing#getDefaultExtension()
    */
    public String getDefaultExtension() {
        return "";
    }

    public void init() {
    }

    @Override
    public boolean isStorable() {
        return false;
    }
}