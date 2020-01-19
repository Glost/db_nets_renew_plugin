package de.renew.io.exportFormats;

import org.apache.log4j.Logger;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsbase.util.UserProperties;
import org.freehep.graphicsio.ps.EPSGraphics2D;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.EPSFileFilter;
import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import CH.ifa.draw.util.ColorMap;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import java.io.File;


public class EPSExportFormat extends ExportFormatAbstract {
    public static final Logger logger = Logger.getLogger(EPSExportFormat.class);

    // Attributes
    // Constructor
    public EPSExportFormat() {
        super("EPS", new EPSFileFilter());
    }

    // Methods


    /**
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    public boolean canExportNto1() {
        return false;
    }

    /**
     * Export drawing as eps image.
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
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
     */
    public File internalExport(Drawing drawing, File path, Rectangle bounds,
                               boolean removeWhiteSpace)
            throws Exception {
        File result = null;

        //JPanel drawingPanel = (JPanel) DrawPlugin.getGui().getView(drawing);
        //Rectangle r = drawing.displayBox();
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        Dimension d = new Dimension(width, height);

        VectorGraphics graphics = new EPSGraphics2D(path, d);

        //Remove unnecessary margins and don't scale to international paper size
        final UserProperties defaultProperties = new UserProperties();
        defaultProperties.setProperty(EPSGraphics2D.PAGE_MARGINS,
                                      new Insets(00, 00, 00, 00));
        defaultProperties.setProperty(EPSGraphics2D.FIT_TO_PAGE, true);
        String fontHandling = ExportPlugin.EPS_FONT_HANDLING_SHAPES;
        ExportPlugin exportPlugin = ExportPlugin.getCurrent();
        if (exportPlugin != null) {
            fontHandling = exportPlugin.getEpsFontHandling();
        }
        if (ExportPlugin.EPS_FONT_HANDLING_EMBED.equals(fontHandling)) {
            defaultProperties.setProperty(EPSGraphics2D.EMBED_FONTS, true);
            defaultProperties.setProperty(EPSGraphics2D.TEXT_AS_SHAPES, false);
        } else if (ExportPlugin.EPS_FONT_HANDLING_NONE.equals(fontHandling)) {
            defaultProperties.setProperty(EPSGraphics2D.EMBED_FONTS, false);
            defaultProperties.setProperty(EPSGraphics2D.TEXT_AS_SHAPES, false);
        } else {
            if (!ExportPlugin.EPS_FONT_HANDLING_SHAPES.equals(fontHandling)) {
                logger.warn("Unknown EPS font handling configured: "
                            + fontHandling);
            }
            defaultProperties.setProperty(EPSGraphics2D.EMBED_FONTS, false);
            defaultProperties.setProperty(EPSGraphics2D.TEXT_AS_SHAPES, true);
        }
        if (exportPlugin != null) {
            defaultProperties.setProperty(EPSGraphics2D.TRANSPARENT,
                                          exportPlugin.getEpsTransparency());
        } else {
            defaultProperties.setProperty(EPSGraphics2D.TRANSPARENT, true);
        }
        defaultProperties.setProperty(EPSGraphics2D.CLIP, true);
        defaultProperties.setProperty(EPSGraphics2D.PAGE_SIZE,
                                      EPSGraphics2D.CUSTOM_PAGE_SIZE);
        defaultProperties.setProperty(EPSGraphics2D.CUSTOM_PAGE_SIZE, d);
        defaultProperties.setProperty(EPSGraphics2D.BACKGROUND, false);
        defaultProperties.setProperty(EPSGraphics2D.BACKGROUND_COLOR,
                                      ColorMap.NONE);
        graphics.setProperties(defaultProperties);

        //Start exporting image to eps
        graphics.startExport();

        if (removeWhiteSpace) {
            //Move to coordinates 0,0 
            graphics.translate(x * -1, y * -1);
        }
        //Set clipping to the region that was the original image
        //Otherwise we would export white regions around the image
        graphics.clipRect(x, y, width, height);
        //Paint the picture
        //drawingPanel.print(graphics);
        drawing.draw(graphics);
        //End exporting
        graphics.endExport();

        result = path;
        assert (result != null) : "Failure in EPSExportFormat: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing[], java.net.URI)
     */
    public File export(Drawing[] drawings, File path) throws Exception {
        File result = null;
        assert (result != null) : "Failure in EPSExportFormat: result == null";
        return result;
    }

    public boolean canExportDrawing(Drawing drawing) {
        boolean result = false;
        result = true;
        return result;
    }

    public int getShortCut() {
        return KeyEvent.VK_E;
    }
}