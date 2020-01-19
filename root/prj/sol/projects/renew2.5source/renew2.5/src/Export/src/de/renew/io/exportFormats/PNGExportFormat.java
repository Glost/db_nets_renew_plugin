package de.renew.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.PNGFileFilter;
import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class PNGExportFormat extends ExportFormatAbstract {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(PNGExportFormat.class);

    // Attributes
    // Constructor
    public PNGExportFormat() {
        super("PNG", new PNGFileFilter());
    }

    // Methods
    /**
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    public boolean canExportNto1() {
        return false;
    }

    /**
     * Export drawing as png image.
     * The full canvas is exported.
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
     *
     * @param drawing -- the drawing to be exported
     * @param file -- the file to contain the exported image
     */
    public File export(Drawing drawing, File file) throws Exception {
        Rectangle bounds = drawing.getBounds();

        return internalExport(drawing, file, bounds, true);
    }

    /**
     * Export drawing as png image.
     * The full canvas is exported.
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
     *
     * @param drawing -- the drawing to be exported
     */
    public ByteArrayOutputStream export(Drawing drawing)
            throws Exception {
        Rectangle bounds = drawing.getBounds();

        return internalExport(drawing, bounds, true);
    }

    /**
     * Internal PNG export.
     * @param drawing -- the drawing to be exported
     * @param file -- the file to contain the exported image
     * @param bounds -- the bounds of the drawing.
     *
     * @return the exported file
     * @throws IOException
     */
    public File internalExport(Drawing drawing, File file, Rectangle bounds,
                               boolean removeWhiteSpace)
            throws IOException {
        File pngOutput = new File(file.getAbsolutePath());

        BufferedImage netImage = createImage(drawing, bounds, removeWhiteSpace);

        ImageIO.write(netImage, "png", pngOutput);

        return pngOutput;
    }

    /**
     * Internal PNG export.
     * @param drawing -- the drawing to be exported
     * @param bounds -- the bounds of the drawing.
     *
     * @return the exported PNG as ByteArrayOutputStream.
     * @throws IOException
     */
    public ByteArrayOutputStream internalExport(Drawing drawing,
                                                Rectangle bounds,
                                                boolean removeWhiteSpace)
            throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        BufferedImage netImage = createImage(drawing, bounds, removeWhiteSpace);

        ImageIO.write(netImage, "png", byteStream);

        return byteStream;
    }

    /**
     * Convert drawing to buffered image.
     *
     * @param drawing -- the drawing to be converted.
     * @param bounds -- the bounds of the drawing.
     * @param removeWhiteSpace.
     * @return the image as BufferedImage.
     */
    private BufferedImage createImage(Drawing drawing, Rectangle bounds,
                                      boolean removeWhiteSpace) {
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        // png-generation
        logger.info(PNGExportFormat.class.getName()
                    + ": creating image. Size: " + width + " " + height);
        // create raster-image
        BufferedImage netImage = new BufferedImage(width, height,
                                                   BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = netImage.createGraphics();
        // set renderer to render fast, quality as in Renew
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                  RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                  RenderingHints.VALUE_COLOR_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                  RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // white background
        graphics.setColor(Color.WHITE);
//        graphics.setBackground(new Color(1,1,1,0));
        graphics.fillRect(0, 0, width, height);


        if (removeWhiteSpace) {
            graphics.translate(x * -1, y * -1);
        }
        // render drawing
        drawing.draw(graphics);
        return netImage;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing[], java.net.URI)
     */
    public File export(Drawing[] drawings, File path) throws Exception {
        File result = null;
        assert (result != null) : "Failure in PNGExportFormat: result == null";
        return result;
    }

    public boolean canExportDrawing(Drawing drawing) {
        boolean result = false;
        result = true;
        return result;
    }

    public int getShortCut() {
        return KeyEvent.VK_0;
    }
}