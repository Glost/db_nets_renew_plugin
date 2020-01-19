package de.renew.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import de.renew.gui.CPNDrawing;
import de.renew.gui.ModeReplacement;

import de.renew.io.SNSFileFilter;

import de.renew.shadow.ShadowNetSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;


public class SNSExportFormat extends ExportFormatAbstract {
    // Attributes
    // Construktor
    public SNSExportFormat() {
        super("ShadowNetSystem", new SNSFileFilter());
    }

    // Methods


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
            ObjectOutput output = new ObjectOutputStream(stream);
            ShadowNetSystem netSystem = new ShadowNetSystem(ModeReplacement.getInstance()
                                                                           .getDefaultCompilerFactory());
            CPNDrawing cpndrawing = (CPNDrawing) drawing;
            cpndrawing.buildShadow(netSystem);
            output.writeObject(netSystem);
            output.close();
        }
        assert (result != null) : "Failure in SNSExportFormat: result == null";
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
            ObjectOutput output = new ObjectOutputStream(stream);
            ShadowNetSystem system = new ShadowNetSystem(ModeReplacement.getInstance()
                                                                        .getDefaultCompilerFactory());
            for (int pos = 0; pos < drawings.length; pos++) {
                CPNDrawing drawing = (CPNDrawing) drawings[pos];
                drawing.buildShadow(system);
            }
            output.writeObject(system);
            output.close();
        }
        assert (result != null) : "Failure in SNSExportFormat: result == null";
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