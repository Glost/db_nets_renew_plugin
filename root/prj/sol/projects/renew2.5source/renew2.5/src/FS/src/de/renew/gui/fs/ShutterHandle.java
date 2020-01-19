package de.renew.gui.fs;

import de.uni_hamburg.fs.Node;

import CH.ifa.draw.framework.DrawingView;

import de.renew.gui.ClickHandle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


public class ShutterHandle extends ClickHandle implements de.renew.gui.TokenHandle {
    Node node;
    boolean isClosed;

    public ShutterHandle(FSFigure owner, Rectangle box, Node node,
                         boolean isClosed) {
        super(owner, Color.yellow, Color.black, box);
        this.node = node;
        this.isClosed = isClosed;
    }

    protected void drawInner(Graphics g) {
        int ym = box.y + box.height / 2;


        // draw minus:
        g.drawLine(box.x + box.width / 4, ym, box.x + box.width * 3 / 4, ym);
        if (isClosed) {
            // draw plus:
            int xm = box.x + box.width / 2;
            g.drawLine(xm, box.y + box.height / 4, xm,
                       box.y + box.height * 3 / 4);
        }
    }

    public void invokeStart(MouseEvent e, int x, int y, DrawingView view) {
        super.invokeStart(e, x, y, view);
        if (e.getClickCount() == 1) {
            isClosed = !isClosed;
        }
        ((FSFigure) owner()).setNodeShutState(node, isClosed,
                                              e.getClickCount() > 1);

        // Despite the fact that the appearance of the FSFigure changes
        // when nodes are opened or closed, the semantics of the figure do
        // not change. More important, the FSFigure does currently not
        // serialize information about open nodes in its undo snapshot.
        // So we inform the AbstractTool that nothing happened.
        noChangesMade();
    }
}