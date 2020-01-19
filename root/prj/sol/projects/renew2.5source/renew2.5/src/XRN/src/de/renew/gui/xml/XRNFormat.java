package de.renew.gui.xml;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNDrawing;
import de.renew.gui.ModeReplacement;
import de.renew.gui.XMLFormat;

import java.io.InputStream;
import java.io.OutputStream;


public class XRNFormat implements XMLFormat {
    public XRNFormat() {
        new XRNParser();
    }

    public boolean canExport() {
        return true;
    }

    public boolean canImport() {
        return true;
    }

    public Drawing[] parse(InputStream stream) throws Exception {
        return null; //XRNParser.parse(stream);
    }

    public void write(OutputStream stream, CPNDrawing drawing)
            throws Exception {
        XRNCreator.write(stream, drawing,
                         ModeReplacement.getInstance()
                                        .getDefaultCompilerFactory());
    }

    public void write(OutputStream stream, CPNDrawing[] drawings)
            throws Exception {
        //XRNCreator.write(stream, drawings, compiler);
    }
}