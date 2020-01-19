package de.renew.io.exportFormats;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsbase.util.UserProperties;
import org.freehep.graphicsio.svg.SVGGraphics2D;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.SVGFileFilter;
import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.io.File;


/**
 * @author Benjamin Schleinzer
 *
 */
public class SVGExportFormat extends ExportFormatAbstract {
    // Attributes
    // Constructor
    public SVGExportFormat() {
        super("SVG", new SVGFileFilter());
    }

    // Methods


    /**
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    public boolean canExportNto1() {
        return false;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
     */
    public File export(Drawing drawing, File path) throws Exception {
        File result = null;

//        JPanel drawingPanel;
//        DrawApplication app = DrawPlugin.getGui();
//        if (app !=null){
//        	 drawingPanel = (JPanel) app.getView(drawing);
//        } else{
//        	 drawingPanel = new StandardDrawingView(null, drawing);
//        }
        Rectangle r = drawing.displayBox();
        Dimension d = new Dimension(r.width, r.height);
        VectorGraphics graphics = new SVGGraphics2D(path, d);


        final UserProperties defaultProperties = new UserProperties();

        defaultProperties.setProperty(SVGGraphics2D.EMBED_FONTS, true);
        graphics.setProperties(defaultProperties);

        //Start exporting image to SVG
        graphics.startExport();

        //Move to coordinates 0,0 
        graphics.translate(r.x * -1, r.y * -1);

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
}