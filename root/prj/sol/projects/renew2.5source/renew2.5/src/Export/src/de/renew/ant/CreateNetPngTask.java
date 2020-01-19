package de.renew.ant;

import de.renew.io.exportFormats.PNGExportFormat;


/**
 * Ant task to create PNG graphics from Renew drawings
 *
 * @author Michael Duvigneau
 */
public class CreateNetPngTask extends AbstractExportTask {
    public CreateNetPngTask() {
        super(new PNGExportFormat());
    }
}