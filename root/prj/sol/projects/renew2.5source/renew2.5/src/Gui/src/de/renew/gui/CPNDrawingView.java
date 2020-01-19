/*
 * @(#)CPNApplication.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.standard.StandardDrawingView;

import java.awt.Color;


public class CPNDrawingView extends StandardDrawingView {
    public CPNDrawingView(DrawingEditor editor, int width, int height) {
        super(editor, width, height);
        setBackground(Color.white);
    }
}