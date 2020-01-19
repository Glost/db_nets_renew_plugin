/**
 *
 */
package de.renew.imagenetdiff;

import org.apache.log4j.Logger;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.ExportHolderImpl;
import CH.ifa.draw.io.StatusDisplayer;
import CH.ifa.draw.io.exportFormats.ExportFormat;

import de.renew.io.exportFormats.EPSExportFormat;

import de.renew.util.StringUtil;

import java.awt.Rectangle;

import java.io.File;
import java.io.IOException;


/**
 * Command to Export images to eps and run compare (ImageMagick) on them.
 * Needs installed ImageMagick in the system.
 * Produces eps files from selected drawings in temp folder.
 * Also produces eps image as difference image of both. Results receives the same
 * name as the first selected image with "-diff.eps" extension.
 * Images remain temporarily in temp folder until removed by the system.
 *
 * @author Lawrence Cabac
 *
 */
public class EPSDiffCommand extends AbstractDiffCommand {
    private static final String IMAGE_EXTENSION = ".eps";
    static final Logger logger = Logger.getLogger(EPSDiffCommand.class);

    public EPSDiffCommand() {
        super("Simple EPS Diff");
        setImageExtension(IMAGE_EXTENSION);
    }

    @Override
    public File doDiff(StatusDisplayer sd, Drawing drawing1, Drawing drawing2,
                       boolean quite) {
        if (drawing1 != null && drawing2 != null) {
            // try exporting drawings to eps
            // and run compare on them
            //EPSExportFormat export = new EPSExportFormat();
            ExportHolderImpl exporter = (ExportHolderImpl) DrawPlugin.getCurrent()
                                                                     .getExportHolder();
            ExportFormat[] exportFormats = exporter.allExportFormats();
            EPSExportFormat export = null;
            int i = 0;
            while (i < exportFormats.length) {
                if (exportFormats[i] instanceof EPSExportFormat) {
                    export = (EPSExportFormat) exportFormats[i];
                    break;
                }
                i++;
            }
            if (export == null) {
                System.out.println("sorry");
                return null;
            }
            String fileName1 = drawing1.getName();
            String fileName2 = drawing2.getName();
            if (logger.isInfoEnabled()) {
                logger.info(PNGDiffCommand.class.getSimpleName()
                            + ": fileName1 " + fileName1);
                logger.info(PNGDiffCommand.class.getSimpleName()
                            + ": fileName2 " + fileName2);
            }

            Rectangle bounds1 = drawing1.getBounds();
            Rectangle bounds2 = drawing2.getBounds();
            int width = Math.max(bounds1.width + bounds1.x,
                                 bounds2.width + bounds2.x);
            int height = Math.max(bounds1.height + bounds1.y,
                                  bounds2.height + bounds2.y);
            Rectangle bounds = new Rectangle(0, 0, width, height);

            // create temporary files
            File tempFile1 = null;
            File tempFile2 = null;
            try {
                tempFile1 = File.createTempFile("export-",
                                                fileName1 + getImageExtension());
                tempFile2 = File.createTempFile("export-",
                                                fileName2 + getImageExtension());
            } catch (IOException e2) {
                logger.error(e2.getMessage());
                logger.debug(e2.getMessage(), e2);
            }
            if (logger.isInfoEnabled()) {
                logger.info(PNGDiffCommand.class.getSimpleName()
                            + ": tempFile1 " + tempFile1.getAbsolutePath());
            }

            // do the export
            try {
                export.internalExport(drawing1, tempFile1, bounds, true);
                export.internalExport(drawing2, tempFile2, bounds, true);
            } catch (Exception e1) {
                logger.error(e1.getMessage());
                logger.debug(e1.getMessage(), e1);
            }

            String name1 = StringUtil.stripFilenameExtension(tempFile1
                               .getAbsolutePath());
            String name2 = StringUtil.stripFilenameExtension(tempFile2
                               .getAbsolutePath());

            String nameDiffNoExt = name1 + "-diff";
            String nameDiff = name1 + "-diff" + getImageExtension();
            exchangeColor(name1, 10, "white", "white");
            exchangeColor(name2, 10, "white", "white");

            if (drawing1.getFilename() != null
                        && (drawing1.getFilename().getName().endsWith(".aip")
                                   || drawing1.getFilename().getName()
                                                      .endsWith(".rnw"))) {
                logger.debug("Doing AIP/RNW conversion.");
                exchangeColor(name1, 10, "lightgray", "white");
                exchangeColor(name1, 10, "lightgreen", "white");
                exchangeColor(name2, 10, "lightgray", "white");
                exchangeColor(name2, 10, "lightgreen", "white");
                exchangeColor(name1, 10, "seagreen1", "white");
                exchangeColor(name2, 10, "seagreen1", "white");
                exchangeColor(name1, 10, "yellow", "white");
                exchangeColor(name2, 10, "yellow", "white");
            }

            // compare the two images with im
            Process process = null;
            try {
                process = Runtime.getRuntime()
                                 .exec("compare " + name1 + getImageExtension()
                                       + " " + name2 + getImageExtension()
                                       + " " + nameDiff); // newer version of im knows options: -highlight-color and -lowlight-color 
            } catch (Exception e1) {
                logger.error("Error while executing imagemagick compare: "
                             + e1.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(PNGDiffCommand.class.getSimpleName() + ": "
                                 + e1.getMessage(), e1);
                }
            }
            int exit = 0;
            if (process != null) {
                try {
                    exit = process.waitFor();
                    if (logger.isDebugEnabled()) {
                        logger.debug(EPSDiffCommand.class.getSimpleName()
                                     + ": process' exit code = " + exit);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // inversed color
            exchangeColor(nameDiffNoExt, 10, "#d81030", "green");

            // Finally show diff image if exists, if no quite flag is set.
            File diffFile = new File(nameDiff);
            if (!diffFile.exists()) {
                sd.showStatus("Could not create diff image. ImageMagick installed?");
            } else if (!quite) {
                try {
                    Runtime.getRuntime().exec("gv " + diffFile);
                } catch (IOException e1) {
                    logger.error(e1.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug(EPSDiffCommand.class.getName() + ": ", e1);
                    }
                }
            } else {
                sd.showStatus("Diff image created successfully. Name: "
                              + diffFile.getName());
            }
            return diffFile;
        } else {
            sd.showStatus("Operation canceled.");
        }
        return null;
    }
}