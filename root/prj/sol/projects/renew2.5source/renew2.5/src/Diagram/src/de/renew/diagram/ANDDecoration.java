/*
 * @(#)ANDDecoration.java
 *
 */
package de.renew.diagram;

import java.awt.Color;


/**
 *
 *
 */
public class ANDDecoration extends SplitDecoration implements FigureDecoration {
    // protected int size;
    //protected int halfSize;
    public ANDDecoration() {
        setSize(20);

        setFillColor(Color.black);
        setFrameColor(Color.black);
    }
}