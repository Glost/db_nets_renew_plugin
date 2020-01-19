package de.renew.io.importFormats;

import CH.ifa.draw.io.importFormats.ImportFormatMultiAbstract;


/**
 * Implementation of ImportFormatMulti for XML files (PNML)
 */
public class XMLImportFormat extends ImportFormatMultiAbstract {
    // Attributes
    // Constructor
    public XMLImportFormat() {
        super("XML", "XML FileFilter");
        init();
    }

    // Methods


    /**
     * Initiation for XMLImportFormat
     */
    protected void init() {
        PNMLImportFormat pnml = new PNMLImportFormat();
        addImportFormat(pnml);
    }
}