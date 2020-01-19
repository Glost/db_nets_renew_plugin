package de.renew.gui;

import CH.ifa.draw.framework.Drawing;

import java.io.InputStream;
import java.io.OutputStream;


// Present an abstract way of generating and parsing XML
// files. This interface allows us to compile the main
// part of Renew without referencing any XML parser
// libraries. Furthermore, we can easily swap XML file
// formats.
public interface XMLFormat {
    public boolean canExport();

    public boolean canImport();

    // Throw a general exception, so that no reference
    // to e.g. SAXException is required.
    public Drawing[] parse(InputStream stream) throws Exception;

    public void write(OutputStream stream, CPNDrawing drawing)
            throws Exception;

    public void write(OutputStream stream, CPNDrawing[] drawings)
            throws Exception;
}