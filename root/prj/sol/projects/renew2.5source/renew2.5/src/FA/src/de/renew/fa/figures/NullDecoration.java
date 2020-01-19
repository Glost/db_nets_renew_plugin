/**
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.IOException;


/**
 * @author jo
 *
 */
public class NullDecoration implements FigureDecoration {
    static final long serialVersionUID = -212579720182061310L;

    /**
     *
     */
    public NullDecoration() {
        super();

    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.fa.figures.FigureDecoration#draw(java.awt.Graphics,
     *      java.awt.Rectangle, java.awt.Color, java.awt.Color)
     */
    @Override
    public void draw(Graphics g, Rectangle r, Color fillColor, Color lineColor) {
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.fa.figures.FigureDecoration#equals(de.renew.fa.figures.FigureDecoration)
     */
    @Override
    public boolean equals(FigureDecoration fd) {
        // TODO Auto-generated method stub
        return fd instanceof NullDecoration;
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.util.Storable#read(CH.ifa.draw.util.StorableInput)
     */
    @Override
    public void read(StorableInput dr) throws IOException {
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.util.Storable#write(CH.ifa.draw.util.StorableOutput)
     */
    @Override
    public void write(StorableOutput dw) {
    }
}