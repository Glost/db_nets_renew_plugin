package de.renew.splashscreen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;


/**
 * This class is an extension of {@link JPanel}.<br>
 * It draws an image as background image and resizes the {@link JPanel} to the
 * specified {@link Dimension}.
 *
 * @author Eva Mueller
 * @date Nov 27, 2010
 * @version 0.1
 */
public class ImagePanel extends JPanel {
    private Image img;

    /**
     * Create {@link ImagePanel} with given <b>size</b> and<br>
     * use given <b>image</b> as background image.
     *
     * @param image [{@link Image}] The background image
     * @param size [{@link Dimension}] The size of the image panel
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    public ImagePanel(Image image, Dimension size) {
        this.img = image;
        setSize(size);
        setBackground(Color.WHITE);
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        if (img != null) {
            g.drawImage(img, 10, 10, null);
        } else {
            super.paintComponent(g);
        }
    }
}