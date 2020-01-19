package de.renew.gui;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.util.ColorMap;

import de.renew.remote.NetInstanceAccessor;

import java.awt.Color;
import java.awt.Rectangle;


public class NetInstanceHandle extends ClickHandle {
    NetInstanceAccessor netInstance;

    /**
     * Creates a new handle within the given rectangle, surrounded by a blue
     * border, displaying the name of the instance.
     **/
    public NetInstanceHandle(Figure owner, Rectangle box,
                             NetInstanceAccessor netInstance) {
        super(owner, ColorMap.NONE, Color.blue, box);
        this.netInstance = netInstance;
    }

    public void invokeStart(int x, int y, DrawingView view) {
        super.invokeStart(x, y, view);
        noChangesMade();

        ((CPNApplication) view.editor()).openInstanceDrawing(netInstance);
    }
    // protected void drawInner(Graphics g) {
    // g.setColor(Color.blue);
    // g.drawLine(box.x,box.y+box.height-1,box.x+box.width,box.y+box.height-1);
    // }
}