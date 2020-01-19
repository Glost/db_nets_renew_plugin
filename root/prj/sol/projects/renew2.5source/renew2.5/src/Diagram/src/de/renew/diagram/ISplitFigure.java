/*
 * Created on 14.05.2003
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;

import java.util.Vector;


/**
 * @author Lawrence Cabac
 */
public interface ISplitFigure {
    public abstract void setDecoration(FigureDecoration fd);

    public abstract FigureDecoration getDecoration();

    public abstract boolean hasDecoration();

    public abstract Connector connectorAt(int x, int y);

    public abstract Vector<Handle> handles();

    public abstract void setFillColor(java.awt.Color col);
}