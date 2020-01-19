package de.renew.io.importFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.importFormats.ImportFormatAbstract;

import de.renew.lola.LolaFileFilter;
import de.renew.lola.parser.LolaParser;

import java.io.File;
import java.io.FileInputStream;

import java.net.URL;


public class LolaImportFormat extends ImportFormatAbstract {
    public static final String FORMAT_NAME = "Lola net file";
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LolaImportFormat.class);

    public LolaImportFormat() {
        super(FORMAT_NAME, new LolaFileFilter());
    }

    @Override
    public Drawing[] importFiles(URL[] paths) throws Exception {
        logger.info("[Lola] Lola Import: Starting");
        Drawing[] result = null;
        if (paths != null) {
            Drawing[] drawings = new Drawing[1];
            logger.info("[Lola] Lola Import: Importing file "
                        + paths[0].toString());
            drawings[0] = importFile(paths[0]);
            result = drawings;
        }
        assert (result != null) : "Failure in LolaImportFormat: result == null";
        return result;
    }

    public Drawing importFile(URL file) throws Exception {
        Drawing result = null;
        if (file != null) {
            FileInputStream stream = new FileInputStream(new File(file.getFile()));
            result = new LolaParser().importNet(stream);
            stream.close();
        }
        assert (result != null) : "Failure in LolaImportFormat: result == null";
        return result;
    }
}