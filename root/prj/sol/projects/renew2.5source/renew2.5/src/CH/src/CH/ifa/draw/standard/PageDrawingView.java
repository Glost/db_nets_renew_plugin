/*
 * @(#)PageDrawingView.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;


/**
 *
 */
public class PageDrawingView extends StandardDrawingView {
    public PageDrawingView(DrawingEditor editor, int width, int height) {
        super(editor, width, height);
    }

    public void drawBackground(Graphics g) {
        Rectangle b = getBounds();
        g.setColor(getBackground());
        g.fillRect(0, 0, b.width, b.height);
        g.setColor(Color.gray);
        Dimension d = getMinimumSize();
        g.fillRect(48, 48, d.width - 64, d.height - 64);
        g.setColor(Color.white);
        g.fillRect(32, 32, d.width - 64, d.height - 64);
        g.setColor(Color.lightGray);
        g.drawRect(64, 64, d.width - 129, d.height - 129);
    }
}