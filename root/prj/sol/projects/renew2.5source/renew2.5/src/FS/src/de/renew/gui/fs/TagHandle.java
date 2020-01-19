package de.renew.gui.fs;

import de.uni_hamburg.fs.Node;

import CH.ifa.draw.framework.DrawingView;

import CH.ifa.draw.util.ColorMap;

import de.renew.gui.ClickHandle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;


public class TagHandle extends ClickHandle {
    Drawable highlight;
    Node node;
    boolean selected;

    public TagHandle(FSFigure owner, Rectangle box, Drawable highlight,
                     Node node, boolean selected) {
        super(owner, ColorMap.NONE, Color.red, box);
        setHighlight(highlight);
        this.node = node;
        this.selected = selected;
    }

    public void setHighlight(Drawable highlight) {
        this.highlight = highlight;
    }

    /**
     * Draws this handle.
     */
    public void draw(Graphics g) {
        if (selected) {
            Rectangle fsbox = owner().displayBox();
            g.translate(fsbox.x, fsbox.y);
            g.setColor(Color.red);
            highlight.draw(g);
            g.translate(-fsbox.x, -fsbox.y);
        }
    }

    public void invokeStart(int x, int y, DrawingView view) {
        super.invokeStart(x, y, view);
        ((FSFigure) owner()).setSelectedTag(node);
    }
}