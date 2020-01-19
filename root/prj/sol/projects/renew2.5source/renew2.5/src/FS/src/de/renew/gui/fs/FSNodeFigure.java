package de.renew.gui.fs;

import CH.ifa.draw.figures.ShortestDistanceConnector;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.util.ColorMap;

import de.renew.gui.PlaceNodeFigure;
import de.renew.gui.TransitionNodeFigure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import java.util.Vector;


public class FSNodeFigure extends TextFigure implements PlaceNodeFigure,
                                                        TransitionNodeFigure {
    private static final int DEFAULT_ARC = 8;
    private String typeStr = "";

    public FSNodeFigure(String type) {
        super(type);
        typeStr = type;
        setAttribute("FrameColor", ColorMap.color("Black"));
        setAttribute("FillColor", ColorMap.color("White"));
        setAlignment(CENTER);
    }

    public FSNodeFigure() {
        this("");
    }

    public void drawBackground(Graphics g) {
        Rectangle r = displayBox();
        Shape s = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height,
                                             DEFAULT_ARC, DEFAULT_ARC);
        ((Graphics2D) g).fill(s);
    }

    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();
        Shape s = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height,
                                             DEFAULT_ARC, DEFAULT_ARC);
        ((Graphics2D) g).draw(s);
    }

    public Insets connectionInsets() {
        return new Insets(DEFAULT_ARC / 2, DEFAULT_ARC / 2, DEFAULT_ARC / 2,
                          DEFAULT_ARC / 2);
    }

    public Connector connectorAt(int x, int y) {
        return new ShortestDistanceConnector(this); // just for demo purposes
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new FeatureConnectionHandle(this));
        return handles;
    }

    public void setText(String text) {
        typeStr = text;
        super.setText(typeStr);
    }

    public Rectangle displayBox() {
        Rectangle box = super.displayBox();
        return new Rectangle(box.x - 5, box.y, box.width + 10, box.height);
    }

    /** Build a shadow in the given shadow net.
      *  This shadow is stored as well as returned.
      */
    public ShadowNetElement buildShadow(ShadowNet net) {
        return null;
    }

    /** Get the associated shadow, if any.
     */
    public ShadowNetElement getShadow() {
        return null;
    }

    public String getName() {
        return typeStr;
    }

    public void release() {
        super.release();
    }

    public boolean canBeParent(ParentFigure figure) {
        return figure == null;
    }
}