/*
 * @(#)FontSizeHandle.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.LocatorHandle;

import CH.ifa.draw.util.Fontkit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;


/**
 * A Handle to change the font size by direct manipulation.
 */
public class FontSizeHandle extends LocatorHandle {
    private Font fFont;
    private int fSize;
    private int fHeight;

    public FontSizeHandle(Figure owner, Locator l) {
        super(owner, l);
    }

    public void invokeStart(int x, int y, DrawingView view) {
        super.invokeStart(x, y, view);
        TextFigure textOwner = (TextFigure) owner();
        fFont = textOwner.getFont();
        fSize = fFont.getSize();
        Rectangle box = textOwner.displayBox();
        fHeight = box.height;
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        TextFigure textOwner = (TextFigure) owner();
        int newSize = fSize * (fHeight + y - anchorY) / fHeight;
        textOwner.setFont(Fontkit.getFont(fFont.getName(), fFont.getStyle(),
                                          newSize));
    }

    public void draw(Graphics g) {
        Rectangle r = displayBox();

        g.setColor(Color.yellow);
        g.fillOval(r.x, r.y, r.width, r.height);

        g.setColor(Color.black);
        g.drawOval(r.x, r.y, r.width, r.height);
    }
}