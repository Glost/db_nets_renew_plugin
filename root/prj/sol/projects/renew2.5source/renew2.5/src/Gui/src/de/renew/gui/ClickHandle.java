package de.renew.gui;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractHandle;

import CH.ifa.draw.util.ColorMap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;


public abstract class ClickHandle extends AbstractHandle implements TokenHandle {
    protected Rectangle box;
    protected Color fillColor;
    protected Color penColor;

    protected ClickHandle(Figure owner, Color fillColor, Color penColor,
                          Rectangle box) {
        super(owner);
        this.fillColor = fillColor;
        this.penColor = penColor;
        this.box = box;
    }

    public Point getOffset() {
        return box.getLocation();
    }

    public void setBox(Rectangle box) {
        this.box = box;
    }

    /**
     * Locates the handle on the figure. The handle is drawn
     * centered around the returned point.
     */
    public Point locate() {
        Rectangle dbox = displayBox();
        return new Point(dbox.x + dbox.width / 2, dbox.y + dbox.height / 2);
    }

    /**
     * Gets the display box of the handle.
     */
    public Rectangle displayBox() {
        Rectangle fsbox = owner().displayBox();
        return new Rectangle(box.x + fsbox.x, box.y + fsbox.y, box.width,
                             box.height);
    }

    //NOTICEredundant UNUSED....
    protected void drawInner(Graphics g) {
    }

    /**
     * Draws this handle.
     */
    public void draw(Graphics g) {
        Rectangle fsbox = owner().displayBox();
        g.translate(fsbox.x, fsbox.y);
        if (!ColorMap.NONE.equals(fillColor)) {
            g.setColor(fillColor);
            g.fillRect(box.x, box.y, box.width, box.height);
        }
        if (!ColorMap.NONE.equals(penColor)) {
            g.setColor(penColor);
            g.drawRect(box.x, box.y, box.width - 1, box.height - 1);
        }
        //NOTICEredundant
        drawInner(g);
        g.translate(-fsbox.x, -fsbox.y);
    }
}