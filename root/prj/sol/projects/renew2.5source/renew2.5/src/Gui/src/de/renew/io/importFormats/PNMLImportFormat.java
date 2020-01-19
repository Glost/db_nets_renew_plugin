package de.renew.io.importFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.importFormats.ImportFormatAbstract;

import de.renew.gui.pnml.PNMLFormat;

import de.renew.io.PNMLFileFilter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.Iterator;
import java.util.Vector;


/**
 * Implementation of ImportFormat for PNML files.
 */
public class PNMLImportFormat extends ImportFormatAbstract {
    // Attributes
    // Constructor
    public PNMLImportFormat() {
        super("PNML", new PNMLFileFilter());
    }


    // Methods


    /**
    * @see de.renew.io.ImportFormat#importFiles(java.io.File[])
     */
    public Drawing[] importFiles(URL[] files) throws Exception {
        Drawing[] result = null;
        Vector<Drawing[]> list = new Vector<Drawing[]>();
        int count = 0;
        if (files != null) {
            for (int pos = 0; pos < files.length; pos++) {
                Drawing[] temp = importFile(files[pos]);
                count = count + temp.length;
                list.add(temp);
            }
        }
        Iterator<Drawing[]> iterator = list.iterator();
        result = new Drawing[count];
        while (iterator.hasNext()) {
            Drawing[] element = iterator.next();
            for (int pos = 0; pos < element.length; pos++) {
                result[--count] = element[pos];
            }
        }
        assert (result != null) : "Failure in SNSImportFormat: result == null";
        return result;
    }

    /**
     * Returns a list of all drawings in the file.
     * @require file != null
     * @ensure result != null
     * @param file the PNML file to be imported.
     * @return Enumeration, list of all imported drawings.
     * @throws Exception, is thrown in case of failure during import of file.
     */
    protected Drawing[] importFile(URL file) throws Exception {
        Drawing[] result = null;
        if (file != null) {
            InputStream stream = file.openStream();
            PNMLFormat format = new PNMLFormat();
            result = format.parse(stream);
            stream.close();
        }
        assert (result != null) : "Failure in PNMLImportFormat: result == null";
        return result;
    }

    /**
     * Returns a list of all drawings in the file.
     * @require file != null or != ""
     * @ensure result != null
     * @param src the PNML file to be imported.
     * @return Enumeration, list of all imported drawings.
     * @throws Exception, is thrown in case of failure during import of file.
     */
    public Drawing[] importFile(String src) throws Exception {
        Drawing[] result = null;
        if (src != null || src != "") {
            PNMLFormat format = new PNMLFormat();
            result = format.parse(new ByteArrayInputStream(src.getBytes()));
        }
        assert (result != null) : "Failure in PNMLImportFormat: result == null";
        return result;
    }
}