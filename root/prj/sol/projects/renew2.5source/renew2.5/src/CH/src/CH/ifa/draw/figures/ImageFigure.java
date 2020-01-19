/*
 * @(#)ImageFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.BoxHandleKit;

import CH.ifa.draw.util.GUIProperties;
import CH.ifa.draw.util.Iconkit;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.util.StringUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.net.URI;

import java.util.Vector;


/**
 * A Figure that shows an Image.
 * Images shown by an image figure are shared by using the Iconkit.
 * @see Iconkit
 */
public class ImageFigure extends AttributeFigure implements ImageObserver {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ImageFigure.class);

    /*
     * Serialization support.
     */
    private static final long serialVersionUID = 148012030121282439L;

    /**
     * The name of the file which contains the image.
     * <p>
     * On serialization, the file path will be stored
     * completely, but when written as <code>Storable</code>,
     * it will be stored as a relative path.
     * </p>
     * @serial
     **/
    private String fFileName;

    /**
     * The image to be shown.
     * <p>
     * This field is transient because the field
     * <code>fFileName</code> should be sufficient
     * to regain this information.
     * </p>
     **/
    private transient Image fImage;

    /**
     * Determines position and size of the image by
     * specifying position and size of its bounding box.
     * @serial
     **/
    private Rectangle fDisplayBox;
    @SuppressWarnings("unused")
    private int imageFigureSerializedDataVersion = 1;

    public ImageFigure() {
        fFileName = null;
        fImage = null;
        fDisplayBox = null;
    }

    public ImageFigure(Image image, String fileName, Point origin) {
        if (fileName != null) {
            try {
                fFileName = new File(fileName).getCanonicalPath();
            } catch (IOException e) {
                logger.error("Could not find file " + fileName + ": " + e);
            }
        } else {
            fFileName = null;
        }
        fImage = image;
        fDisplayBox = new Rectangle(origin.x, origin.y, 0, 0);
        fDisplayBox.width = fImage.getWidth(this);
        fDisplayBox.height = fImage.getHeight(this);
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin);
        fDisplayBox.add(corner);
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        BoxHandleKit.addHandles(this, handles);
        return handles;
    }

    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
    }

    public void internalDraw(Graphics g) {
        if (fImage == null && fFileName != null) {
            fImage = Iconkit.instance().getImage(fFileName);
        }
        if (fImage != null && new File(fFileName).exists()) {
            g.drawImage(fImage, fDisplayBox.x, fDisplayBox.y,
                        fDisplayBox.width, fDisplayBox.height, this);
        } else {
            drawGhost(g);
        }
    }

    private void drawGhost(Graphics g) {
        g.setColor(Color.gray);
        g.fillRect(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                   fDisplayBox.height);
    }

    /**
     * Handles asynchroneous image updates.
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
        if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
            invalidate();
            if (listener() != null) {
                listener().figureRequestUpdate(new FigureChangeEvent(this));
            }
        }
        return (flags & (ALLBITS | ABORT)) == 0;
    }

    /**
     * Writes the ImageFigure to a StorableOutput. Only a reference to the
     * image, that is its pathname is saved.
     */
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fDisplayBox.x);
        dw.writeInt(fDisplayBox.y);
        dw.writeInt(fDisplayBox.width);
        dw.writeInt(fDisplayBox.height);
        URI imageURI = new File(fFileName).toURI();
        imageURI = StringUtil.makeRelative(dw.getURI(), imageURI);
        String relativePath = imageURI.getPath();
        dw.writeString(relativePath);
        logger.debug(relativePath);
    }

    /**
     * Reads the ImageFigure from a StorableInput. It registers the
     * referenced figure to be loaded from the Iconkit.
     * @see Iconkit#registerImage
     */
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(),
                                    dr.readInt());
        fFileName = dr.readString();
        URI uri = null;
        try {
            uri = dr.getURI().resolve(fFileName);
            fFileName = new File(uri).getCanonicalPath();
            logger.debug("Including image from location: " + fFileName);
        } catch (IllegalArgumentException e) {
            logger.error("Problem while resolving image location: " + e);
        } catch (IOException e) {
            logger.error("Problem while resolving image location: " + e);
            fFileName = (uri != null ? uri.getPath() : null);
        } catch (NullPointerException e) {
            logger.debug("StorableInput has no URI!");
        }

        if (!GUIProperties.noGraphics()) {
            Iconkit.instance().registerImage(fFileName);
        }
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        Iconkit.instance().registerImage(fFileName);
        fImage = null;
    }
}