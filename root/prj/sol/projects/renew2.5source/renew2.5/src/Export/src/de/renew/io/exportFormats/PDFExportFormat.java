package de.renew.io.exportFormats;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsbase.util.UserProperties;
import org.freehep.graphicsio.PageConstants;
import org.freehep.graphicsio.pdf.PDFGraphics2D;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.PDFFileFilter;
import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import java.io.File;


/**
 * @author Benjamin Schleinzer, Michael Haustermann
 *
 */
public class PDFExportFormat extends ExportFormatAbstract {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PDFExportFormat.class);

    public PDFExportFormat() {
        super("PDF", new PDFFileFilter());
    }

    /**
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    public boolean canExportNto1() {
        return false;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
     */
    public File export(Drawing drawing, File file) throws Exception {
        Rectangle bounds = drawing.getBounds();

        return internalExport(drawing, file, bounds);
    }

    public File internalExport(Drawing drawing, File path, Rectangle bounds)
            throws Exception {
        File result = null;

        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        Dimension d = new Dimension(width, height);
        VectorGraphics graphics = new PDFGraphics2D(path, d);

        String pageSize = ExportPlugin.BOUNDING_BOX_PAGE_SIZE;
        String pageOrientation = PageConstants.PORTRAIT;

        ExportPlugin plugin = ExportPlugin.getCurrent();
        if (plugin != null) {
            pageSize = plugin.getPageSize();
            pageOrientation = plugin.getPageOrientation();
        }

        final UserProperties defaultProperties = new UserProperties();
        if (pageSize.equals(ExportPlugin.BOUNDING_BOX_PAGE_SIZE)) {
            defaultProperties.setProperty(PDFGraphics2D.PAGE_SIZE,
                                          PDFGraphics2D.CUSTOM_PAGE_SIZE);
            defaultProperties.setProperty(PDFGraphics2D.CUSTOM_PAGE_SIZE, d);
            defaultProperties.setProperty(PDFGraphics2D.PAGE_MARGINS,
                                          new Insets(0, 0, 0, 0));
        } else {
            defaultProperties.setProperty(PDFGraphics2D.PAGE_SIZE, pageSize);
            defaultProperties.setProperty(PDFGraphics2D.ORIENTATION,
                                          pageOrientation);
        }

        graphics.setProperties(defaultProperties);

        //Start exporting image to PDF
        graphics.startExport();
        //Move to coordinates 0,0 
        graphics.translate(-x, -y);
        //Set clipping to the region that was the original image
        //Otherwise we would export white regions around the image
        graphics.clipRect(x, y, width, height);

        //Paint the picture
        drawing.draw(graphics);

        //End exporting
        graphics.endExport();
        result = path;
        assert (result != null) : "Failure in PDFExportFormat: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing[], java.net.URI)
     */
    public File export(Drawing[] drawings, File path) throws Exception {
        File result = null;
        assert (result != null) : "Failure in PDFExportFormat: result == null";
        return result;
    }

    public boolean canExportDrawing(Drawing drawing) {
        return true;
    }

    @Override
    public int getModifier() {
        return KeyEvent.SHIFT_DOWN_MASK
               + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    @Override
    public int getShortCut() {
        return KeyEvent.VK_P;
    }
}