/*
 * @(#)TransitionFigureCreationTool.java 5.1
 *
 */
package de.renew.tablet.tools;

import CH.ifa.draw.figures.AttributeFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.CreationTool;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNDrawingHelper;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * A more efficient version of the generic creation
 * tool that is not based on cloning.
 *
 * @author Lawrence Cabac
 */
public class PTToggleCreationTool extends CreationTool {
    private boolean toggle = true;
    private Figure lastCreated;
    private Figure target = null;

    public PTToggleCreationTool(DrawingEditor editor) {
        super(editor);
    }

    @Override
    public void mouseDrag(MouseEvent e, int x, int y) {
        if (target == null) {
            super.mouseDrag(e, x, y);
        }
    }

    @Override
    public void deactivate() {
        lastCreated = null;
        super.deactivate();
    }

    @Override
    public void mouseDown(MouseEvent e, int x, int y) {
        target = findConnectionTarget(new ArcConnection(), lastCreated, x, y,
                                      view().drawing());
        if (target == null) {
            super.mouseDown(e, x, y);
        }
    }


    /**
    * Creates a new TransitionFigure.
    */
    protected Figure createFigure() {
        if (toggle) {
            toggle = (!toggle);
            return new PlaceFigure();
        }
        toggle = (!toggle);
        return new TransitionFigure();
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        Figure created = null;
        if (target != null) {
            created = target;
            target = null;
            toggle = (!toggle);
        } else {
            created = createdFigure();

            if ((created.displayBox().width < 10)
                        || (created.displayBox().height < 10)) {
                Point loc = created.displayBox().getLocation();
                Dimension d;
                if (toggle) {
                    d = TransitionFigure.defaultDimension();
                } else {
                    d = PlaceFigure.defaultDimension();
                }
                int w2 = d.width / 2;
                int h2 = d.height / 2;
                created.displayBox(new Point(loc.x - w2, loc.y - h2),
                                   new Point(loc.x - w2 + d.width,
                                             loc.y - h2 + d.height));
            }
        }

        //super.mouseUp(e, x, y);
        //System.out.println(lastCreated+ " "+ created);
        if (lastCreated != null) {
            //System.out.println("lastCreated is not null");
            ArcConnection arc = new CPNDrawingHelper().createArcConnection((AttributeFigure) lastCreated,
                                                                           (AttributeFigure) created,
                                                                           1);
            view().add(arc);
            view().checkDamage();
        }
        lastCreated = created;
    }

    @Override
    public void activate() {
        toggle = true; //start tool with PlaceFigures
        target = null;
        super.activate();
    }

    /**
     * Finds a connection end figure.
     */
    protected Figure findConnectionTarget(ArcConnection arc, Figure owner,
                                          int x, int y, Drawing drawing) {
        Figure target = findConnectableFigure(arc, x, y, drawing);
        if (owner == null) {
            if (target instanceof TransitionFigure) {
                toggle = (!toggle);
            }
            return target;
        }
        if ((target != null) && target.canConnect() && !target.includes(owner)
                    && arc.canConnect(owner, target)) {
            return target;
        }
        return null;
    }

    protected Figure findConnectableFigure(ArcConnection arc, int x, int y,
                                           Drawing drawing) {
        FigureEnumeration k = drawing.figuresReverse();
        while (k.hasMoreElements()) {
            Figure figure = k.nextFigure();
            if (!figure.includes(arc) && figure.canConnect()) {
                if (figure.containsPoint(x, y)) {
                    return figure;
                }
            }
        }
        return null;
    }
}