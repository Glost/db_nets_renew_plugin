/*
 * @(#)EllipseFigure.java 5.1
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.ChopEllipseConnector;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.BoxHandleKit;

import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.IOException;

import java.util.Vector;


/**
 * An ellipse figure.
 */
public class HalfMoonFigure extends AttributeFigure {
    /*
     * Serialization support.
     */
    static final long serialVersionUID = -6856203289355118951L;

    /**
     * Determines position and size of the ellipse by specifying position and
     * size of its bounding box.
     *
     * @serial
     */
    private Rectangle fDisplayBox;

    public HalfMoonFigure() {
        this(new Point(0, 0), new Point(0, 0));
    }

    public HalfMoonFigure(Point origin, Point corner) {
        basicDisplayBox(origin, corner);
    }

    @Override
    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin);
        fDisplayBox.add(corner);
    }

    @Override
    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
    }

    @Override
    public Insets connectionInsets() {
        Rectangle r = fDisplayBox;
        int cx = r.width / 2;
        int cy = r.height / 2;
        return new Insets(cy, cx, cy, cx);
    }

    @Override
    public Connector connectorAt(int x, int y) {
        return new ChopEllipseConnector(this);
    }

    /**
     * Checks if a point is inside the figure.
     */
    @Override
    public boolean containsPoint(int x, int y) {
        if (super.containsPoint(x, y)) {
            return Geom.ellipseContainsPoint(displayBox(), x, y);
        } else {
            return false;
        }
    }

    @Override
    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    @Override
    public void drawBackground(Graphics g) {
        Rectangle r = displayBox();

        //g.fillOval(r.x, r.y, r.width, r.height);
        //        System.out.println("====================================== ");
        //        System.out.println("x "+displayBox().x);
        //        System.out.println("y "+displayBox().y);
        //        System.out.println("X "+getXabs());
        //        System.out.println("Y "+getYabs());
        //        System.out.println("W "+r.width % 2);
        //        System.out.println("H "+new Double(getYprime()).intValue());
        //        System.out.println("YPrine "+ getYprime());
        g.fillArc(r.x, r.y, (r.width - 1), r.height - 1, 120, 120);
        g.setColor(Color.white);
        g.fillRect(getXabs() + displayBox().width / 4, getYabs(),
                   new Double(displayBox().width / 2).intValue(),
                   new Double(getYprime() * 2).intValue());


        //g.fillArc(getXabs(),getYabs(),new Double(r.width / 2).intValue(), new
        // Double(getYprime()*2).intValue(),110,160);
    }

    @Override
    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();

        //g.drawOval(r.x, r.y, r.width - 1, r.height - 1);
        //g.drawArc(r.x,r.y,r.width - 1, r.height - 1,90,180);
        g.drawArc(r.x, r.y, (r.width - 1), r.height - 1, 120, 120);
        //g.setColor(Color.white);
        g.drawLine(getXabs() + displayBox().width / 4, getYabs(),
                   getXabs() + displayBox().width / 4,
                   new Double(getYprime() * 2).intValue());
    }

    private int getXabs() {
        //float a = displayBox().width / 2;
        return displayBox().x;
    }

    private int getYabs() {
        //float a = displayBox().width / 2;
        float b = displayBox().height / 2;

        return new Double(displayBox().y + b - getYprime()).intValue();
    }

    private double getYprime() {
        //float a = displayBox().width / 2;
        float b = displayBox().height / 2;
        return Math.sqrt(((1 - (.25)) * b * b));
    }

    @Override
    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        BoxHandleKit.addHandles(this, handles);
        return handles;
    }

    @Override
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(),
                                    dr.readInt());
    }

    @Override
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fDisplayBox.x);
        dw.writeInt(fDisplayBox.y);
        dw.writeInt(fDisplayBox.width);
        dw.writeInt(fDisplayBox.height);
    }
}