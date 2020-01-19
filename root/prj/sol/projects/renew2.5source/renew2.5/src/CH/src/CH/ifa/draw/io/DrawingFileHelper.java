package CH.ifa.draw.io;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.io.importFormats.ImportFormat;

import CH.ifa.draw.util.StorableOutput;

import de.renew.util.StringUtil;

import java.awt.Dimension;
import java.awt.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;


/**
 * A collection of functions to load drawings from and save them to files.
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public class DrawingFileHelper {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DrawingFileHelper.class);

    // Changes version 0->1:
    //  - PolyLineFigure is now an AttributeFigure.
    // Changes version 4->5:
    //  - Saving parameters of arrow tips.
    // Changes version 5->6:
    //  - FSFigures are now CPNTextFigures.
    // Changes version 6->7:
    // - FSFigures now store closed nodes (as path list)
    // Changes version 7->8:
    // - PolylineFigures now store arrow tip type.
    // - Default font changed from "Helvetica" to "SansSerif"
    //   (patching of old files needed)
    // Changes version 8->9:
    // - AssocArrowTip and IsaArrowTip moved from package fs
    //   to package gui (patching of old files needed)
    // Changes version 9->10
    // - fixed storing of NetComponentFigure
    // Changes version 10->11
    // - alpha value of colors is saved.
    static public final int FILEVERSION = 11;
    private static StorableInputDrawingLoader loader = new StorableInputDrawingLoader();

    /**
     * This class is not intended to be instantiated.
     **/
    private DrawingFileHelper() {
    }

    /**
     * Loads a <code>Drawing</code> from the given file.
     * The {@link CH.ifa.draw.util.Storable} data format is used.
     * The respective loader can be configured via
     * {@link #setStorableInputDrawingLoader}.
     *
     * @param file  the name of the file to read.
     * @param sd    an object where diagnostic messages can be sent to.
     * @return      the resulting <code>Drawing</code> object retrieved
     *              from the given file. Returns <code>null</code>, if the
     *              file could not be read for any reason.
     **/
    public static Drawing loadDrawing(File file, StatusDisplayer sd) {
        PositionedDrawing posDrawing = loadPositionedDrawing(file, sd);
        if (posDrawing != null) {
            return posDrawing.getDrawing();
        }
        return null;
    }

    public static Drawing loadDrawing(InputStream stream, String name)
            throws FileNotFoundException, IOException {
        PositionedDrawing posDrawing = loadPositionedDrawing(stream, name);
        if (posDrawing != null) {
            return posDrawing.getDrawing();
        }
        return null;
    }

    /**
     * Loads a <code>Drawing</code> from the given URL.
     * The {@link CH.ifa.draw.util.Storable} data format is used.
     * The respective loader can be configured via
     * {@link #setStorableInputDrawingLoader}.
     *
     * @param location  the URL of the drawing to retrieve.
     * @param sd    an object where diagnostic messages can be sent to.
     * @return      the resulting <code>Drawing</code> object retrieved
     *              from the given location. Returns <code>null</code>, if
     *              the drawing could not be retrieved for any reason.
     **/
    public static Drawing loadDrawing(URL location, StatusDisplayer sd) {
        PositionedDrawing posDrawing = loadPositionedDrawing(location, sd);
        if (posDrawing != null) {
            return posDrawing.getDrawing();
        }
        return null;
    }

    /**
     * Loads a <code>Drawing</code> and its view parameters from the
     * given file. The {@link CH.ifa.draw.util.Storable} data format is used.
     * The respective loader can be configured via
     * {@link #setStorableInputDrawingLoader}.
     *
     * @param file  the name of the file to read.
     * @param sd    an object where diagnostic messages can be sent to.
     * @return      a <code>PositionedDrawing</code> containing the
     *              <code>Drawing</code> retrieved from the given file,
     *              along with its positioning information. Returns
     *              <code>null</code>, if the file could not be read for
     *              any reason.
     **/
    public static PositionedDrawing loadPositionedDrawing(File file,
                                                          StatusDisplayer sd) {
        if (sd == null) {
            sd = DrawPlugin.getGui();
        }
        try {
            Drawing drawing = null;
            URL url = file.toURI().toURL();
            PositionedDrawing posDrawing = loadPositionedDrawing(url, sd);
            if (posDrawing != null) {
                drawing = posDrawing.getDrawing();
                if (drawing != null) {
                    drawing.setFilename(file);
                }
            }
            return posDrawing;
        } catch (IOException e) {
            sd.showStatus("Error " + e);
        }
        return null;
    }

    public static PositionedDrawing loadPositionedDrawing(InputStream stream,
                                                          String name)
            throws FileNotFoundException, IOException {
        logger.debug("Loading drawing from " + stream + "...");
        PositionedDrawing posDrawing = loader.readFromStorableInput(stream);
        if (posDrawing != null) {
            Drawing drawing = posDrawing.getDrawing();
            if (drawing != null) {
                drawing.setName(name);
                if (drawing instanceof Figure) {
                    ((Figure) drawing).invalidate();
                }
            }
        }
        return posDrawing;
    }


    /**
     * Loads a <code>Drawing</code> and its view parameters from the
     * given URL. The {@link CH.ifa.draw.util.Storable} data format is used.
     * The respective loader can be configured via
     * {@link #setStorableInputDrawingLoader}.
     *
     * @param location  the URL of the drawing to retrieve.
     * @param sd    an object where diagnostic messages can be sent to.
     * @return      a <code>PositionedDrawing</code> containing the
     *              <code>Drawing</code> retrieved from the given location,
     *              along with its positioning information. Returns
     *              <code>null</code>, if the file could not be read for
     *              any reason.
     **/
    public static PositionedDrawing loadPositionedDrawing(URL location,
                                                          StatusDisplayer sd) {
        logger.debug("Loading drawing from " + location + "...");
        if (sd == null) {
            sd = DrawPlugin.getGui();
        }
        if (DrawPlugin.getCurrent() != null) {
            ImportFormat[] allImportFormats = DrawPlugin.getCurrent()
                                                        .getImportHolder()
                                                        .allImportFormats();
            for (ImportFormat importFormat : allImportFormats) {
                if (importFormat.canImport(location)) {
                    try {
                        Drawing[] drawings = importFormat.importFiles(new URL[] { location });
                        if (drawings.length > 0) {
                            PositionedDrawing posDrawing = new PositionedDrawing(null,
                                                                                 null,
                                                                                 drawings[0]);
                            return posDrawing;
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        logger.debug(e);
                    }
                }
            }
        }
        PositionedDrawing posDrawing = null;
        try {
            posDrawing = loader.readFromStorableInput(location, sd);
            if (posDrawing != null) {
                Drawing drawing = posDrawing.getDrawing();
                if (drawing != null) {
                    String file = location.getFile();
                    drawing.setName(StringUtil.getFilename(file));
                    drawing.setFilename(new File(file));
                    if (drawing instanceof Figure) {
                        ((Figure) drawing).invalidate();
                    }
                }
            }
            return posDrawing;
        } catch (IOException e) {
            logger.error("Could not open Drawing: " + location.toString());
            if (logger.isDebugEnabled()) {
                logger.debug(DrawingFileHelper.class.getSimpleName() + ": " + e);
            }
            sd.showStatus("Error " + e);
        }
        return null;
    }

    /**
     * Computes the backup file name for the given file.
     *
     * @return the abstract representation of the backup file name. Returns
     *         <code>null</code>, if no backup file can be determined. This
     *         may happen when the given file name is identical to its
     *         backup file name.
     **/
    public static File deriveBackupFile(File file) {
        File backupFile = new File(file.getParent(),
                                   StringUtil.stripFilenameExtension(file
                              .getName()) + ".bak");
        if (backupFile.equals(file)) {
            return null;
        }
        return backupFile;
    }

    /**
     * Saves a <code>Drawing</code> to the given file.
     * The {@link CH.ifa.draw.util.Storable} data format is used.
     * The dirty flag of the drawing is cleared if the operation was
     * successful.
     *
     * @param drawing  the drawing to store.
     * @param file     the name of the file to write to.
     * @param sd       an object where diagnostic messages can be sent to.
     * @return         <code>true</code>, if the drawing could be saved. <br>
     *                 Returns <code>false</code>, if a failure occurred.
     **/
    public static boolean saveDrawing(Drawing drawing, File file,
                                      StatusDisplayer sd) {
        return savePositionedDrawing(new PositionedDrawing(null, null, drawing),
                                     file, sd);
    }

    /**
     * Saves a <code>Drawing</code> and its view parameters to the given
     * file. The {@link CH.ifa.draw.util.Storable} data format is used.
     * The dirty flag of the drawing is cleared if the operation was
     * successful.
     *
     * @param positionedDrawing  the drawing combined with its positioning
     *                 information to store.
     * @param file     the name of the file to write to.
     * @param sd       an object where diagnostic messages can be sent to.
     * @return         <code>true</code>, if the drawing could be saved. <br>
     *                 Returns <code>false</code>, if a failure occurred.
     **/
    public static boolean savePositionedDrawing(PositionedDrawing positionedDrawing,
                                                File file, StatusDisplayer sd) {
        Drawing drawing = positionedDrawing.getDrawing();
        if (!drawing.getBackupStatus()) {
            // Create a backup.
            File backupFile = deriveBackupFile(file);
            if (backupFile != null && file.exists()) {
                logger.debug("Creating backup file " + backupFile + "...");
                if (file.renameTo(backupFile)) {
                    drawing.setBackupStatus(true);
                } else {
                    logger.error("Could not create backup file " + backupFile
                                 + "!");
                    logger.error("Original filename is " + file);
                }
            }
        }
        logger.debug("Saving drawing as " + file + "...");
        try {
            saveAsStorableOutput(drawing, file,
                                 positionedDrawing.getWindowLocation(),
                                 positionedDrawing.getWindowDimension(), true);
            return true;
        } catch (IOException e) {
            sd.showStatus("Error " + e);
        }
        return false;
    }

    /**
     * Saves a <code>Drawing</code> and its view parameters to the given
     * file. The {@link CH.ifa.draw.util.Storable} data format is used.
     * if requested, the dirty flag of the drawing is cleared if the
     * operation was successful.
     *
     * @param drawing  the drawing to store.
     * @param file     the name of the file to write to.
     * @param loc      the window position to store along with the drawing.
     * @param size     the window width and height to store along with the
     *                 drawing.
     * @param clearModified  whether or not the dirty flag of the drawing
     *                 should be cleared after a successful save operation.
     * @throws IOException if an I/O-failure occurred. The dirty flag of
     *                 the drawing is unchanged. Whether or not anything
     *                 has been written cannot be guaranteed.
     **/
    public static void saveAsStorableOutput(Drawing drawing, File file,
                                            Point loc, Dimension size,
                                            boolean clearModified)
            throws IOException {
        drawing.lock();
        try {
            // TBD: should write a MIME header
            StorableOutput output = new StorableOutput(file);

            // Write the current version. This is a
            // substitute for a MIME header.
            output.writeInt(FILEVERSION);

            // Write the drawing.
            output.writeStorable(drawing);

            // Write window size and position (if given)
            if (loc != null && size != null) {
                output.writeInt(loc.x);
                output.writeInt(loc.y);
                output.writeInt(size.width);
                output.writeInt(size.height);
            }
            output.close();

            if (clearModified) {
                drawing.clearModified();
            }
        } finally {
            drawing.unlock();
        }
    }


    /**
     * Checks if the given file name ends with an extension accepted
     * by the given file filter and adds one, if this was not
     * the case.
     *
     * @param file the filename to check.
     * @param ff   f filefilter to query for valid extensions.
     *             If the given path has none of these extensions
     *             appended to its end, the filter's default
     *             extension is automatically appended.
     *             The given extension set is enforced, e.g.
     *             the result is guaranteed to end with one
     *             of these extensions.
     *             If no file filter is specified,
     *             no extension is required or appended.
     * @return     Completed filename (with one of the
     *             given file filter's extensions).
     **/
    public static File checkAndAddExtension(File file, SimpleFileFilter ff) {
        File result = file;
        if ((file != null) && (ff != null)) {
            if (!ff.accept(file)) {
                result = new File(file.getParentFile(),
                                  file.getName() + "." + ff.getExtension());
            }
        }
        return result;
    }

    /**
     * Configures the <code>StorableInputDrawingLoader</code> used by the
     * load functions of this class. This method can be used to replace the
     * default loader by a customized one.
     *
     * @param newloader  the new <code>StorableInputDrawingLoader</code>
     *                   that replaces the old one.
     * @throws NullPointerException  if <code>newloader</code> is null.
     **/
    public static void setStorableInputDrawingLoader(StorableInputDrawingLoader newloader) {
        if (newloader != null) {
            loader = newloader;
        } else {
            throw new NullPointerException("Cannot set null reference as loader.");
        }
    }
}