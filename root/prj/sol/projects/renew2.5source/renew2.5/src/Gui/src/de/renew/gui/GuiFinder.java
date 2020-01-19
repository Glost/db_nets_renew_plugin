/*
 * Created on Dec 29, 2004
 *
 */
package de.renew.gui;

import CH.ifa.draw.DrawPlugin;
import CH.ifa.draw.IOHelper;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.PositionedDrawing;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.net.loading.Finder;

import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetSystem;

import de.renew.util.ClassSource;
import de.renew.util.StringUtil;

import java.awt.EventQueue;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;

import javax.swing.SwingUtilities;


/**
 *
 * This class is responsible for loading <code>.rnw</code> net
 * files either from a file (using {@link #findNetFile(String, StringBuffer)})
 * or from a classpath source (using {@link #findNetClasspathRel(String, StringBuffer)}).
 * It is used in {@link de.renew.shadow.DefaultCompiledNetLoader} and instantiated
 * when a simulation is set up from the gui (see {@link de.renew.gui.CPNSimulation}.
 *
 * @author Till Kothe
 */
public class GuiFinder extends Finder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(GuiFinder.class);

    /**
     * this is used to retrieve drawings currently open in the
     * GUI.
     */
    CPNDrawingLoader loader;

    /**
     * Creates a new <code>GuiFinder</code> with the given
     * {@link CPNDrawingLoader}.
     *
     * @param loader the drawing loader to use with this <code>GuiFinder</code>.
     */
    public GuiFinder(CPNDrawingLoader loader) {
        this.loader = loader;
    }

    /* (non-Javadoc)
     * @see de.renew.net.loading.Finder#findNetFile(java.lang.String, java.lang.StringBuffer)
     */
    public ShadowNetSystem findNetFile(String name, String path) {
        ShadowNetSystem netSystem = null;
        CPNDrawing drawing = this.getDrawingFromLoader(name);
        if (drawing == null) {
            drawing = this.getDrawingFromFile(path);
        }

        if (drawing != null) {
            netSystem = compileDrawing(drawing);
        } else {
            logger.debug("GuiFinder: No drawing found.");
        }
        return netSystem;
    }


    /* (non-Javadoc)
     * @see de.renew.net.loading.Finder#findNetClasspathRel(java.lang.String, java.lang.StringBuffer)
     */
    public ShadowNetSystem findNetClasspathRel(String name, String path) {
        ShadowNetSystem netSystem = null;
        CPNDrawing drawing = this.getDrawingFromLoader(name);
        if (drawing == null) {
            drawing = this.getDrawingFromFileClasspathRel(path);
        }

        if (drawing != null) {
            netSystem = compileDrawing(drawing);
        } else {
            logger.debug("GuiFinder: No drawing found.");
        }
        return netSystem;
    }

    /**
     * Asks the {@link CPNDrawingLoader} for all loaded Drawings by
     * calling {@link CPNDrawingLoader#loadedDrawings()} and searches
     * for a net with the name of the given parameter.
     * @param name the name of the <code>CPNDrawing</code> to look for.
     * @return the <code>CPNDrawing</code> if one is found or else
     * <code>null</code>.
     */
    CPNDrawing getDrawingFromLoader(String name) {
        synchronized (loader) {
            Iterator<CPNDrawing> it = loader.loadedDrawings();
            while (it.hasNext()) {
                CPNDrawing currentDrawing = it.next();
                String currentName = currentDrawing.getName();
                if (name.equals(currentName)) {
                    return currentDrawing;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link CPNDrawing} of the given name from a file
     * source and opens it in the GUI.
     *
     * @param pathBuffer path to the CPNDrawing to retrieve.
     * @return the retrieved <code>CPNDrawing</code> if successful or else
     *  <code>null</code>.
     */
    CPNDrawing getDrawingFromFile(String path) {
        URL url = null;
        File file = null;
        boolean error;

        String fullFileName = path + ".rnw";
        logger.debug("looking for: " + fullFileName);
        try {
            file = new File(fullFileName);
            error = !file.canRead();
            if (!error) {
                try {
                    url = file.toURI().toURL();
                } catch (MalformedURLException e) {
                    logger.error("Could not transform drawing filename to URL: "
                                 + e.getMessage());
                    error = true;
                }
            }
            if (error) {
                file = null;
            }
        } catch (Exception e) {
            logger.error("GuiFinder: " + fullFileName + " caused " + e);
            error = true;
        }
        if (!error) {
            // open the drawing in the gui
            assert url != null : "error should be set if url==null.";
            StatusRememberer statusRememberer = new StatusRememberer();
            final PositionedDrawing posDrawing;
            if (file != null) {
                posDrawing = DrawingFileHelper.loadPositionedDrawing(file,
                                                                     statusRememberer);
            } else {
                posDrawing = DrawingFileHelper.loadPositionedDrawing(url,
                                                                     statusRememberer);
            }
            Drawing newDrawing = null;
            if (posDrawing != null) {
                newDrawing = posDrawing.getDrawing();
            }
            if (newDrawing != null && newDrawing instanceof CPNDrawing) {
                final DrawApplication gui = DrawPlugin.getGui();
                if (gui != null) {
                    // TODO: Fix responsibilities for GuiPlugin, DrawPlugin and IOHelper!!!
                    if (SwingUtilities.isEventDispatchThread()) {
                        gui.openDrawing(posDrawing);
                    } else {
                        EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    gui.openDrawing(posDrawing);
                                }
                            });
                    }
                }
                return (CPNDrawing) newDrawing;
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link CPNDrawing} of the given name from a classpath
     * source and opens it in the GUI.
     * @param pathBuffer path to the CPNDrawing to retrieve.
     * @return the retrieved <code>CPNDrawing</code> if successful or else
     *  <code>null</code>.
     */
    CPNDrawing getDrawingFromFileClasspathRel(String path) {
        URL url;
        boolean error;

        String fullFileName = path + ".rnw";

        logger.debug("looking for: " + fullFileName);

        url = ClassSource.getClassLoader()
                         .getResource(StringUtil.convertToSlashes(fullFileName));
        error = (url == null);

        if (!error) {
            // open the drawing in the GUI
            Drawing newDrawing;
            StatusRememberer statusRememberer = new StatusRememberer();
            newDrawing = DrawingFileHelper.loadDrawing(url, statusRememberer);
            if (newDrawing != null && newDrawing instanceof CPNDrawing) {
                final IOHelper ioHelper = DrawPlugin.getCurrent().getIOHelper();
                if (ioHelper != null) {
                    final URL toOpen = url;

                    // TODO: Fix responsibilities for GuiPlugin, DrawPlugin and IOHelper!!!
                    if (SwingUtilities.isEventDispatchThread()) {
                        ioHelper.loadAndOpenDrawing(toOpen);
                    } else {
                        EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    ioHelper.loadAndOpenDrawing(toOpen);
                                }
                            });
                    }
                }
                return (CPNDrawing) newDrawing;
            }
        }
        return null;
    }

    /**
     * Compiles a given {@link CPNDrawing} to a
     * new {@link ShadowNet}.
     * @param drawing the <code>CPNDrawing</code> to compile
     * @return the compiled <code>ShadowNetSystem</code> if successful.
     * <code>null</code> otherwise.
     */
    private ShadowNetSystem compileDrawing(CPNDrawing drawing) {
        ShadowNetSystem netSystem = null;
        logger.debug("GuiFinder: Trying to build shadow for drawing " + drawing
                     + ".");
        try {
            // we build our ShadowNet into a new SNS
            // using the current compiler factory.
            ShadowCompilerFactory compilerFactory = ModeReplacement.getInstance()
                                                                   .getDefaultCompilerFactory();
            netSystem = new ShadowNetSystem(compilerFactory);
            drawing.buildShadow(netSystem);
            logger.debug("GuiFinder: Successful.");
            return netSystem;
        } catch (Exception e) {
            logger.debug("GuiFinder: Problem " + e + ".");
        }
        return netSystem;
    }

    private class StatusRememberer implements StatusDisplayer {
        public void showStatus(String message) {
            logger.debug("GuiFinder: " + message);
        }
    }
}