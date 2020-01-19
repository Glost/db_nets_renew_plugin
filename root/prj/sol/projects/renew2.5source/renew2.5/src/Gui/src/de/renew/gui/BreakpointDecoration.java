package de.renew.gui;

import org.apache.log4j.Logger;

import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;


/**
 * Static helper class to decorate figures with a breakpoint icon.
 * <p>
 * </p>
 * @author Michael Duvigneau
 * @since Renew 2.1
 **/
public class BreakpointDecoration {
    public static Logger logger = Logger.getLogger(BreakpointDecoration.class);
    private static ImageIcon icon = null;
    private static boolean failed = false;
    private static int iconWidth = 0;

    /**
     * This class is not instantiatable.
     **/
    private BreakpointDecoration() {
    }

    private static ImageIcon getIcon() {
        if ((icon == null) && !failed) {
            String name = CPNApplication.CPNIMAGES + "BP_TAG.gif";
            URL url = BreakpointDecoration.class.getResource(name);
            if (url == null) {
                logger.warn("Could not retrieve icon via class loader, trying file system: "
                            + name);
                File file = new File(name);
                if (!file.isFile()) {
                    logger.warn("Icon file is not a regular file: " + file);
                } else {
                    try {
                        url = file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        logger.warn("Icon file is not valid as URL: " + file, e);
                    }
                }
            }
            if (url == null) {
                failed = true;
            } else {
                icon = new ImageIcon(url);
                iconWidth = icon.getImage().getWidth(null);
            }
        }
        return icon;
    }

    public static void draw(Graphics g, Rectangle box) {
        ImageIcon imageIcon = getIcon();
        if (imageIcon != null) {
            imageIcon.paintIcon(null, g, box.x + box.width - iconWidth, box.y);
        }
    }
}