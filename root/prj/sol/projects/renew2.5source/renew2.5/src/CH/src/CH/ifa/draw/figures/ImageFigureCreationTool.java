/*
 * @(#)ImageFigureCreationTool.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.DrawPlugin;
import CH.ifa.draw.IOHelper;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.io.ImageFileFilter;

import CH.ifa.draw.standard.CreationTool;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class ImageFigureCreationTool extends CreationTool
        implements ImageObserver {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ImageFigureCreationTool.class);
    private Image image;
    private String fImageName = null;
    private JFrame frame;

    public ImageFigureCreationTool(DrawingEditor editor, JFrame frame) {
        super(editor);
        this.frame = frame;
    }

    private String showFileDialog() {
        IOHelper iohelper = DrawPlugin.getCurrent().getIOHelper();
        JFileChooser fc = new JFileChooser(iohelper.getLastPath());
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new ImageFileFilter());
        fc.setFileHidingEnabled(true);
        fc.setMultiSelectionEnabled(false);
        int returnVal = fc.showOpenDialog(null);

        // File chooser was not approved?
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = fc.getSelectedFile();
        iohelper.setLastPath(file);
        return file.getAbsolutePath();
    }

    public static Image createImage(String fileName, JFrame frame) {
        Image image = Toolkit.getDefaultToolkit().getImage(fileName);
        if (image != null) {
            MediaTracker tracker = new MediaTracker(frame);
            tracker.addImage(image, 123);
            // block until the image is loaded
            try {
                tracker.waitForAll();
            } catch (Exception e) {
                image = null;
            }
        }
        return image;
    }

    public void activate() {
        super.activate();
        image = null;
        fImageName = showFileDialog();

        if (fImageName != null) {
            image = createImage(fImageName, frame);
            if (image == null) {
                noChangesMade();
                fEditor.toolDone();
                fEditor.showStatus("Image " + fImageName
                                   + " could not be loaded!");
            }
        } else {
            noChangesMade();
            fEditor.toolDone();
            fEditor.showStatus("Image creation canceled.");
        }
    }

    /**
     * Creates a new ImageFigure.
     */
    protected Figure createFigure() {
        if (fImageName != null && image != null) {
            Point pnt = fEditor.view().lastClick();
            ImageFigure imageFigure = new ImageFigure(image, fImageName, pnt);
            Rectangle displayBox = imageFigure.displayBox();
            imageFigure.moveBy(-(displayBox.width / 2), -(displayBox.height / 2));
            return imageFigure;
        } else {
            return null;
        }
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        Figure created = createdFigure();
        if (created != null && created.isEmpty()) {
            Point loc = created.displayBox().getLocation();
            int width = image.getWidth(this);
            int height = image.getHeight(this);
            if (width == -1 || height == -1) {
                logger.error("Image not loaded properly!");
            } else {
                created.displayBox(loc, new Point(loc.x + width, loc.y + height));
            }
        }
        super.mouseUp(e, x, y);
    }

    synchronized public boolean imageUpdate(Image img, int infoflags, int x,
                                            int y, int width, int height) {
        return false;
    }
}