/*
 * @(#)BufferedUpdateStrategy.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Painter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;


/**
 * The BufferedUpdateStrategy implements an update
 * strategy that first draws a view into a buffer
 * followed by copying the buffer to the DrawingView.
 * @see DrawingView
 */
public class BufferedUpdateStrategy implements Painter {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = 6489532222954612824L;

    /**
    * The offscreen image
    */
    transient private Image fOffscreen;
    private int fImagewidth = -1;
    private int fImageheight = -1;
    @SuppressWarnings("unused")
    private int bufferedUpdateSerializedDataVersion = 1;

    /**
     * Draws the view contents.
     */
    public void draw(Graphics g, DrawingView view) {
        // create the buffer if necessary
        Dimension d = view.getSize();
        if ((fOffscreen == null) || (d.width != fImagewidth)
                    || (d.height != fImageheight)) {
            fOffscreen = view.createImage(d.width, d.height);
            fImagewidth = d.width;
            fImageheight = d.height;
        }

        // let the view draw on offscreen buffer
        Graphics g2 = fOffscreen.getGraphics();


        // Do the following two lines really speed up
        // the image generation? It seems as though 
        // they even slow it down, which is strange.
        //   Rectangle gClip=g.getClipBounds();
        //   g2.clipRect(gClip.x,gClip.y,gClip.width,gClip.height);
        view.drawAll(g2);

        g.drawImage(fOffscreen, 0, 0, view);
    }
}