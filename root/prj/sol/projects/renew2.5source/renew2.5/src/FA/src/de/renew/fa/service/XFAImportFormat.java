/*
 * Created on Sep 15, 2005
 *
 */
package de.renew.fa.service;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.importFormats.ImportFormat;
import CH.ifa.draw.io.importFormats.ImportFormatAbstract;

import de.renew.fa.XFAFileFilter;

import de.renew.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;

import java.net.URL;

import java.util.Iterator;
import java.util.Vector;


/**
 * Defines the import XFA format for a finite automata drawing (<code>FADrawing</code>).
 * @see de.renew.fa.service.XFAFormat
 * @author cabac
 *
 */
public class XFAImportFormat extends ImportFormatAbstract
        implements ImportFormat {
    public XFAImportFormat() { //String name, FileFilter fileFilter) {
        super("XFA", new XFAFileFilter());
    }

    /**
     * Returns a list of all drawings in the file.
     *
     * @require file != null
     * @ensure result != null
     * @param file - The XFA file to be imported.
     * @return Array of <code>Drawing</code>, list of all imported drawings.
     * @throws Exception,
     *             is thrown in case of failure during import of file.
     */
    protected Drawing[] importFile(URL file) throws Exception {
        Drawing[] result = null;
        if (file != null) {
            FileInputStream stream = new FileInputStream(new File(file.getFile()));
            XFAFormat format = new XFAFormat();
            result = format.parse(stream, StringUtil.getFilename(file.getPath()));
            stream.close();
        }
        assert (result != null) : "Failure in XFAImportFormat: result == null";
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.io.importFormats.ImportFormat#importFiles(java.net.URL[])
     */
    @Override
    public Drawing[] importFiles(URL[] paths) throws Exception {
        Vector<Drawing[]> list = new Vector<Drawing[]>();
        for (int i = 0; i < paths.length; i++) {
            URL url = paths[i];
            Drawing[] temp = importFile(url);
            list.add(temp);
        }
        Iterator<Drawing[]> iterator = list.iterator();
        int count = list.size();
        Drawing[] result = new Drawing[list.size()];
        while (iterator.hasNext()) {
            Drawing[] element = iterator.next();
            for (int pos = 0; pos < element.length; pos++) {
                result[--count] = element[pos];
            }
        }
        assert (result != null) : "Failure in XFAImportFormat: result == null";
        return result;
    }
}