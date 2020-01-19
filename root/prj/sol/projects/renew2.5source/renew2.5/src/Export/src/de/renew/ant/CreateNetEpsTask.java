package de.renew.ant;

import de.renew.io.exportFormats.EPSExportFormat;


/**
 * Ant task to create EPS graphics from Renew drawings
 *
 * @author Kolja Markwardt (original code, moved to AbstractExportTask)
 * @author Michael Duvigneau (refactored)
 */
public class CreateNetEpsTask extends AbstractExportTask {
    public CreateNetEpsTask() {
        super(new EPSExportFormat());
    }
}