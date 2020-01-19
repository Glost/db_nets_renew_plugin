package de.renew.gui.pnml;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNDrawing;
import de.renew.gui.XMLFormat;
import de.renew.gui.pnml.creator.PNMLCreator;
import de.renew.gui.pnml.parser.PNMLParser;

import java.io.InputStream;
import java.io.OutputStream;


public class PNMLFormat implements XMLFormat {
    public boolean canExport() {
        return true;
    }

    public boolean canImport() {
        return true;
    }

    public Drawing[] parse(InputStream stream) throws Exception {
        return PNMLParser.instance().parse(stream);
    }

    public void write(OutputStream stream, CPNDrawing drawing)
            throws Exception {
        PNMLCreator creator = new PNMLCreator();
        creator.write(stream, drawing);
    }

    public void write(OutputStream stream, CPNDrawing[] drawings)
            throws Exception {
        PNMLCreator creator = new PNMLCreator();
        creator.write(stream, drawings);
    }
}