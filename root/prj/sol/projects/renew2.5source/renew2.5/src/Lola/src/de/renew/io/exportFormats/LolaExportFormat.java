package de.renew.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import de.renew.gui.CPNDrawing;

import de.renew.lola.LolaFileCreator;
import de.renew.lola.LolaFileFilter;

import java.io.File;
import java.io.FileOutputStream;


public class LolaExportFormat extends ExportFormatAbstract {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(LolaExportFormat.class);

    // Attributes
    // Construktor
    public LolaExportFormat() {
        super("Lola", new LolaFileFilter());
    }

    // Methods

    /**
     * Only CPNDrawing in PT formalism can be exported to lola format.
     */
    public boolean canExportDrawing(Drawing drawing) {
        boolean result = false;
        if (drawing instanceof CPNDrawing) { // Simulator should be in PTnet mode
            result = true;
        }
        return result;
    }

    /**
     * n PT-Net drawings can't be exported to one lola file
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    public boolean canExportNto1() {
        return false;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
     */
    public File export(Drawing drawing, File path) throws Exception {
        LolaFileCreator creator = new LolaFileCreator();
        File result = null;
        if (drawing != null && path != null) {
            result = path;
            FileOutputStream stream = new FileOutputStream(result);
            // write net file with lola format
            creator.writeLolaFile(stream, (CPNDrawing) drawing);
            stream.flush();
            stream.close();

            // now create task files
            creator.createTasks((CPNDrawing) drawing, path);
        }
        if (result != null) {
            // TODO works only with GUI, let the export crash, hence commented out
//            DrawPlugin.getCurrent()
//                      .showStatus("[Lola Export] Successfully exported drawing to "
//                                  + path);
            logger.info("[Lola Export] Successfully exported drawing to "
                        + path);
        } else {
            logger.error("Failure in LolaExportFormat: result == null");
        }
        return result;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing[], java.net.URI)
     */
    public File export(Drawing[] drawings, File path) throws Exception {
        File result = null;
        assert (result != null) : "Failure in LolaExportFormat: result == null";
        return result;
    }
}