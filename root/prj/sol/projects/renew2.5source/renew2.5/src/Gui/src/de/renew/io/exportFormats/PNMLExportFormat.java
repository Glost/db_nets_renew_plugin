package de.renew.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import de.renew.gui.CPNDrawing;
import de.renew.gui.pnml.PNMLFormat;
import de.renew.gui.pnml.converter.Converter;

import de.renew.io.PNMLFileFilter;

import java.io.File;
import java.io.FileOutputStream;


public abstract class PNMLExportFormat extends ExportFormatAbstract {
    private String _type;

    public PNMLExportFormat(String type) {
        super("PNML-" + type, new PNMLFileFilter());
        setType(type);
    }

    protected void setType(String type) {
        _type = type;
    }

    protected String getType() {
        return _type;
    }


    /**
         * @see de.renew.io.ExportFormat#canExportNto1()
         */
    public boolean canExportNto1() {
        return true;
    }

    /**
         * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
         */
    public File export(Drawing drawing, File path) throws Exception {
        File result = null;
        if (drawing != null && path != null) {
            result = path;
            FileOutputStream stream = new FileOutputStream(result);
            Converter.instance().setType(getType());
            PNMLFormat format = new PNMLFormat();
            format.write(stream, (CPNDrawing) drawing);
        }
        assert (result != null) : "Failure in PNMLExportFormat: result == null";
        return result;
    }

    /**
         * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing[], java.net.URI)
         */
    public File export(Drawing[] drawings, File path) throws Exception {
        File result = null;
        if (drawings != null && path != null) {
            result = path;
            FileOutputStream stream = new FileOutputStream(result);
            Converter.instance().setType(getType());
            PNMLFormat format = new PNMLFormat();
            CPNDrawing[] cpn = new CPNDrawing[drawings.length];
            for (int pos = 0; pos < cpn.length; pos++) {
                cpn[pos] = (CPNDrawing) drawings[pos];
            }
            format.write(stream, cpn);
        }
        assert (result != null) : "Failure in PNMLExportFormat: result == null";
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