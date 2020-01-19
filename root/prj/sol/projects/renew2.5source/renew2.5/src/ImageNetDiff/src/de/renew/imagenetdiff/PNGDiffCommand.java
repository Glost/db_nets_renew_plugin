/**
 *
 */
package de.renew.imagenetdiff;

import org.apache.log4j.Logger;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.io.StatusDisplayer;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.DrawingHelper;

import de.renew.io.exportFormats.PNGExportFormat;

import de.renew.plugin.PluginManager;

import de.renew.util.StringUtil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.JFrame;
import javax.swing.JScrollPane;


/**
 * Command to Export images to png and run compare (ImageMagick) on them.
 * Needs installed ImageMagick in the system.
 * Produces png files from selected drawings in temp folder.
 * Also produces png image as difference image of both. Results receives the same
 * name as the first selected image with "-diff.png" extension.
 * Images remain temporarily in temp folder until reoved by the system.
 *
 * @author Lawrence Cabac
 *
 */
public class PNGDiffCommand extends AbstractDiffCommand {
    static final String IMAGE_EXTENSION = ".png";
    static final Logger logger = Logger.getLogger(PNGDiffCommand.class);

    public PNGDiffCommand() {
        super("Simple PNG Diff");
        setImageExtension(IMAGE_EXTENSION);
    }

    @Override
    public File doDiff(StatusDisplayer sd, Drawing drawinga, Drawing drawingb,
                       boolean quite) {
        if (drawinga != null && drawingb != null) {
            // try exporting drawings to png
            // and run compare on them
            PluginManager pluginManager = PluginManager.getInstance();
            boolean oldstyle = false;
            if (pluginManager != null) {
                ImageNetDiffPlugin diffplugin = (ImageNetDiffPlugin) pluginManager
                                                .getPluginByName("ImageNetDiff");
                if (diffplugin != null) {
                    oldstyle = diffplugin.getProperties()
                                         .getBoolProperty("de.renew.imagenetdiff.background");
                }
            }
            Drawing drawing1;
            Drawing drawing2;
            if (!oldstyle) {
                try {
                    drawing1 = removeBackground(drawinga);
                    drawing2 = removeBackground(drawingb);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug(PNGDiffCommand.class.getSimpleName()
                                     + ": " + e);
                    }
                    drawing1 = drawinga;
                    drawing2 = drawingb;
                    oldstyle = true;
                }
            } else {
                drawing1 = drawinga;
                drawing2 = drawingb;
            }

            PNGExportFormat export = new PNGExportFormat();

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
                export.internalExport(drawing1, tempFile1, bounds, false); // no clipping of white border (up/left)
                export.internalExport(drawing2, tempFile2, bounds, false); //no clipping
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

            if (oldstyle) {
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
                final ImageNetDiffPlugin plugin = (ImageNetDiffPlugin) PluginManager.getInstance()
                                                                                    .getPluginByName("ImageNetDiff");
                plugin.addBlock();
                final JFrame f = new JFrame("Diff Image: " + drawing1.getName()
                                            + " (green) " + " and "
                                            + drawing2.getName() + " (red)");

                f.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            f.dispose();
                            plugin.removeBlock();
                        }
                    });

                BufferedImage img = null;
                try {
                    img = ImageIO.read(diffFile);
                } catch (IOException e1) {
                    logger.error(e1.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug(EPSDiffCommand.class.getName() + ": ", e1);
                    }
                }
                ImageComponent comp = new ImageComponent(img, width, height);
                JScrollPane pane = new JScrollPane(comp);
                f.add(pane);
                f.setSize(new Dimension(width, height));
                f.pack();
                f.setVisible(true);
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

    static private Drawing removeBackground(Drawing drawing)
            throws Exception {
        Drawing clone = DrawingHelper.cloneDrawing(drawing);
        FigureEnumeration figures = clone.figures();
        while (figures.hasMoreElements()) {
            Figure figure = (Figure) figures.nextElement();
            figure.setAttribute("FillColor", ColorMap.NONE);
        }
        return clone;
    }

    // will only be used for the temporary displayed image
    @SuppressWarnings("serial")
    static public class ImageComponent extends Component {
        BufferedImage img;
        Dimension dim;

        public ImageComponent(BufferedImage image, int dx, int dy) {
            img = image;
            dim = new Dimension(dx, dy);
        }

        @Override
        public Dimension getPreferredSize() {
            return dim;
        }

        public void paint(Graphics g) {
            g.drawImage(img, 0, 0, null);
        }
    }
}