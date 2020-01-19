package de.renew.io.importFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.importFormats.ImportFormatAbstract;

import de.renew.gui.xml.XRNParser;

import de.renew.io.XRNFileFilter;

import java.io.File;
import java.io.FileInputStream;

import java.net.URL;


/**
 * Implementation of ImportFormat for XML_renew1 files.
 */
public class OldXMLImportFormat extends ImportFormatAbstract {
    // Attributes
    // constructor
    public OldXMLImportFormat() {
        super("xrn", new XRNFileFilter());
    }

    // Methods


    /**
     * @see de.renew.io.ImportFormat#importFiles(java.io.File[])
     */
    public Drawing[] importFiles(URL[] files) throws Exception {
        Drawing[] result = null;
        if (files != null) {
            Drawing[] drawings = new Drawing[1];
            drawings[0] = importFile(files[0]);
            result = drawings;
        }
        assert (result != null) : "Failure in XRNImportFormat: result == null";
        return result;
    }

    /**
     * Returns the drawing of the file.
     * @require file != null
     * @ensure result != null
     * @param file the XML_renew1 file to be imported.
     * @return Drawing, imported drawing.
     * @throws Exception, is thrown in case of failure during import of file.
     */
    protected Drawing importFile(URL file) throws Exception {
        Drawing result = null;
        FileInputStream stream = new FileInputStream(new File(file.getFile()));
        result = XRNParser.parse(stream);
        stream.close();
        assert (result != null) : "Failure in XRNImportFormat: result == null";
        return result;
    }
}