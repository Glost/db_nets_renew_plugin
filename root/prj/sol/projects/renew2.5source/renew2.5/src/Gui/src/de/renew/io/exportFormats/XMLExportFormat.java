package de.renew.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.exportFormats.ExportFormatMultiAbstract;

import de.renew.gui.CPNDrawing;
import de.renew.gui.pnml.PNMLFormat;
import de.renew.gui.pnml.converter.Converter;

import java.io.File;
import java.io.FileOutputStream;


public class XMLExportFormat extends ExportFormatMultiAbstract {
    // Attributes
    // Constructor
    public XMLExportFormat() {
        super("XML", "XML FileFilter");
        init();
    }

    // Methods


    /**
      * Initiation for XMLImportFormat
      */
    protected void init() {
        PNMLPTExportFormat pt = new PNMLPTExportFormat();
        PNMLRefNetExportFormat ref = new PNMLRefNetExportFormat();
        addExportFormat(pt);
        addExportFormat(ref);
    }

    /**
    * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
    */
    public File export(Drawing drawing, File path) throws Exception {
        File result = path;
        FileOutputStream stream = new FileOutputStream(result);
        Converter.instance().setType("RefNet");
        PNMLFormat format = new PNMLFormat();
        format.write(stream, (CPNDrawing) drawing);
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