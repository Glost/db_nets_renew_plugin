package de.renew.fa.service;

import CH.ifa.draw.framework.Drawing;

import de.renew.fa.util.FAHelper;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * Defines the XFA format for a finite automata drawing (<code>FADrawing</code>).
 *
 * @see de.renew.fa.util.FAHelper
 * @see de.renew.fa.service.FAFileParser
 * @author jo
 *
 */
public class XFAFormat {
    public boolean canExport() {
        return true;
    }

    public boolean canImport() {
        return true;
    }

    /**
     * Parse a XFA and convert the parsed model into a <code>FADrawing</code>.
     *
     * @param stream - The input stream.
     * @param name - The name of the model.
     * @return An array of Drawings, in which is only one drawing.
     *        (To maintain compartability with <code>XMLFormat</code>.)
     * @throws Exception
     */
    public Drawing[] parse(InputStream stream, String name)
            throws Exception {
        Drawing[] result = new Drawing[1];
        result[0] = FAHelper.convertModelToDrawing(new FAFileParser().parseXFA(stream,
                                                                               name));
        return result;
    }

    /**
     * Writes a FADrawing to an output stream in XFA format.
     *
     * @param stream - The output stream.
     * @param drawing - The Drawing,
     * @throws Exception
     */
    public void write(OutputStream stream, Drawing drawing)
            throws Exception {
        FAFileParser.writeToXFA(stream, drawing);
    }

    public void write(OutputStream stream, Drawing[] drawings)
            throws Exception {
        //TODO fix this; only one drawing is written to the output stream, yet
        write(stream, drawings[0]);
    }
}