/*
 * @(#)FigureDecoration.java
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.util.Storable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.Serializable;


/**
 * Decorate the start or end point of a line or poly line figure. LineDecoration
 * is the base class for the different line decorations.
 *
 * @see CH.ifa.draw.figures.PolyLineFigure
 */
public interface FigureDecoration extends Storable, Cloneable, Serializable {

    /**
     * Draws the decoration in the direction specified by the two points.
     */
    public abstract void draw(Graphics g, Rectangle r, Color fillColor,
                              Color lineColor);

    public boolean equals(FigureDecoration fd);
}