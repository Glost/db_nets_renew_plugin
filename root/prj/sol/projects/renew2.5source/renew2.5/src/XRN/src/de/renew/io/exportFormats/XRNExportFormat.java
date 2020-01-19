package de.renew.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import de.renew.gui.CPNDrawing;
import de.renew.gui.xml.XRNFormat;

import de.renew.io.XRNFileFilter;

import java.io.File;
import java.io.FileOutputStream;


public class XRNExportFormat extends ExportFormatAbstract {
    // Attributes
    // Construktor
    public XRNExportFormat() {
        super("xrn", new XRNFileFilter());
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
        if (drawing != null && path != null) {
            result = path;
            FileOutputStream stream = new FileOutputStream(result);
            XRNFormat format = new XRNFormat();
            format.write(stream, (CPNDrawing) drawing);
        }
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
        if (drawing instanceof CPNDrawing) {
            result = true;
        }
        return result;
    }
}