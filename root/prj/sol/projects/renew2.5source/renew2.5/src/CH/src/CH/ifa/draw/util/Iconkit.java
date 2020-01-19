/*
 * @(#)Iconkit.java 5.1
 *
 */
package CH.ifa.draw.util;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;

import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * The Iconkit class supports the sharing of images. It maintains
 * a map of image names and their corresponding images.
 *
 * Iconkit also supports to load a collection of images in
 * synchronized way.
 * The resolution of a path name to an image is delegated to the DrawingEditor.
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld031.htm>Singleton</a></b><br>
 * The Iconkit is a singleton.
 * <hr>
 */
public class Iconkit {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Iconkit.class);
    private final static int ID = 123;
    private static Iconkit instance = null;
    private final Hashtable<String, Image> images;
    private Vector<String> fRegisteredImages;
    private final MediaTracker tracker;

    /**
     * Constructs an Iconkit that uses the given editor to
     * resolve image path names.
     */
    public Iconkit(Component component) {
        images = new Hashtable<String, Image>(53);
        fRegisteredImages = new Vector<String>(10);
        tracker = new MediaTracker(component);
        instance = this;
    }

    /**
     * Gets the singleton instance.
     */
    public static synchronized Iconkit instance() {
        // Workaround: if the Draw Plugin is not initialized yet,
        // we do not have an instance of Iconkit. This fixes this issue.
        if (instance == null) {
            return new Iconkit(new Frame());
        }

        return instance;
    }

    /**
     * Registers an image that is then loaded together with
     * the other registered images by loadRegisteredImages.
     * @see #loadRegisteredImages
     */
    public void registerImage(String fileName) {
        fRegisteredImages.addElement(fileName);
    }

    /**
     * Registers and loads an image.
     */
    public Image registerAndLoadImage(Component component, String fileName) {
        registerImage(fileName);
        loadRegisteredImages();
        return getImage(fileName);
    }

    /**
     * Loads an image with the given name.
     */
    public Image loadImage(String filename) {
        if (images.containsKey(filename)) {
            return images.get(filename);
        }

        Image image = loadImageResource(filename);
        if (image != null) {
            images.put(filename, image);
        }
        return image;
    }

    /**
     * Gets the image with the given name. If the image
     * can't be found it tries it again after loading
     * all the registered images.
     */
    public Image getImage(String filename) {
        if (images.containsKey(filename)) {
            return images.get(filename);
        }

        // Load registered images and try again
        loadRegisteredImages();

        // try again
        return images.get(filename);
    }

    /**
     * Loads an image by its resource name.
     *
     * @param resourceName The name of the image resource.
     * @return An image instance, or <code>null</code>.
     */
    private Image loadImageResource(String resourceName) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            java.net.URL url = getClass().getResource(resourceName);
            logger.debug(resourceName + "(" + url + ")");

            // If url is null: use resource name!
            if (url == null) {
                return toolkit.getImage(resourceName);
            }

            // Create an image from an image producer.
            Object content = url.getContent();
            if (content instanceof ImageProducer) {
                return toolkit.createImage((ImageProducer) content);
            }

            // Load the image from an input stream.
            if (content instanceof java.io.InputStream) {
                java.io.InputStream stream = (java.io.InputStream) content;
                return loadImageInputStream(toolkit, stream);
            }

            // Since we have no useful information available,
            // we try to go the more resource intensive route.
            return toolkit.getImage(url);
        } catch (Exception ex) {
            logger.error("While loading " + resourceName + ":");
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    private Image loadImageInputStream(Toolkit toolkit, InputStream stream)
            throws IOException {
        int max = 1024;
        if (stream.available() > max) {
            max = stream.available();
        }
        byte[] data = new byte[max];
        int pos = 0;
        boolean canReadMore;
        do {
            if (pos > max / 2) {
                max = max * 2;
                byte[] newData = new byte[max];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
            }
            int num = stream.read(data, pos, max - pos);
            canReadMore = num > 0;
            if (canReadMore) {
                pos = pos + num;
            }
        } while (canReadMore);
        stream.close();

        // Create the image.
        return toolkit.createImage(data, 0, pos);
    }

    /**
     * Loads all registered images.
     * @see #registerImage
     */
    private void loadRegisteredImages() {
        if (fRegisteredImages.size() == 0) {
            return;
        }


        // register images with MediaTracker
        Enumeration<String> k = fRegisteredImages.elements();
        while (k.hasMoreElements()) {
            String fileName = k.nextElement();
            if (images.get(fileName) == null) {
                tracker.addImage(loadImage(fileName), ID);
            }
        }
        fRegisteredImages.removeAllElements();

        // block until all images are loaded
        try {
            tracker.waitForAll();
        } catch (Exception e) {
        }
    }
}