package de.renew.fa.service;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import de.renew.fa.FADrawing;
import de.renew.fa.XFAFileFilter;

import java.io.File;
import java.io.FileOutputStream;


/**
 * Defines the export XFA format for a finite automata drawing (<code>FADrawing</code>).
 * @see de.renew.fa.service.XFAFormat
 * @author jo
 *
 */
public class XFAExportFormat extends ExportFormatAbstract {
    public XFAExportFormat() {
        super("XFA ", new XFAFileFilter());

    }

    /**
     * @see CH.ifa.draw.io.exportFormats.ExportFormat#canExportDrawing(CH.ifa.draw.framework.Drawing)
     */
    @Override
    public boolean canExportDrawing(Drawing drawing) {
        boolean result = false;
        if (drawing instanceof FADrawing) {
            result = true;
        }
        return result;
    }

    /**
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    @Override
    public boolean canExportNto1() {
        return false;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing,
     *      java.net.URI)
     */
    @Override
    public File export(Drawing drawing, File path) throws Exception {
        File result = null;
        if (drawing != null && path != null) {
            result = path;
            FileOutputStream stream = new FileOutputStream(result);

            FAFileParser.writeToXFA(stream, drawing);
        }
        assert (result != null) : "Failure in XFAExportFormat: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing[],
     *      java.net.URI)
     */
    @Override
    public File export(Drawing[] drawings, File path) throws Exception {
        File file = null;
        for (int i = 0; i < drawings.length; i++) {
            file = export(drawings[i], path);
        }
        return file;
    }
}