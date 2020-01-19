package de.renew.gui;

import CH.ifa.draw.DrawPlugin;
import CH.ifa.draw.IOHelper;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.SimpleFileFilter;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.shadow.DefaultShadowNetLoader;

import de.renew.util.ClassSource;
import de.renew.util.PathEntry;
import de.renew.util.StringUtil;

import java.awt.EventQueue;

import java.io.File;

import java.lang.reflect.InvocationTargetException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.SwingUtilities;


/**
 * Keeps track of open <code>CPNDrawing</code>s and allows to get
 * a drawing for a known net.
 * <p>
 * </p>
 * CPNDrawingLoader.java
 * Created: Mon Dec  3  2001
 * @author Michael Duvigneau
 **/
public class CPNDrawingLoader {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CPNDrawingLoader.class);

    /**
     * An array of path entries denoting the directories where to
     * look for drawing files.
     **/
    private PathEntry[] netSource;

    /**
     * The list of all known drawings. It is kept up to date by
     * calls from the application. All actions of the loader are
     * synchronized against this object.
     **/
    protected Vector<CPNDrawing> drawings = new Vector<CPNDrawing>();

    /**
     * Creates a new drawing loader connected to the given
     * application. The net load path is extracted from the system
     * property <code>de.renew.netPath</code>.
     **/
    public CPNDrawingLoader() {
        configureNetPath(System.getProperties());
    }

    /**
     * Configures the net search path from a given property set.
     *
     * @param props the property set to extract the
     *              <code>de.renew.netPath</code> property from.
     * @see #setNetPath(String)
     **/
    void configureNetPath(Properties props) {
        setNetPath(props.getProperty("de.renew.netPath",
                                     System.getProperty("user.dir")));
    }

    /**
     * Sets a search path (like the CLASSPATH) to look for net
     * drawing files when a drawing is missing. The directories in
     * the path are separated by {@link File#pathSeparatorChar}.
     **/
    void setNetPath(String path) {
        setNetPath(StringUtil.splitPaths(path));
    }

    /**
     * Sets search paths (like the CLASSPATH) to look for net
     * drawing files when a drawing is missing. Each String in the
     * array denotes exactly one directory to search.
     **/
    void setNetPath(String[] paths) {
        this.netSource = StringUtil.canonizePaths(paths);
        if (logger.isDebugEnabled()) {
            for (int i = 0; i < netSource.length; ++i) {
                logger.debug("Drawing loader source"
                             + (netSource[i].isClasspathRelative
                                ? " (relative to CLASSPATH): " : ": ")
                             + netSource[i].path);
            }
        }
    }

    /**
     * Informs the drawing loader that the given drawing has been
     * loaded by the application.
     *
     * Only called from CPNApplication!
     *
     * @param drawing  the drawing to be registered.
     **/
    public void addDrawing(CPNDrawing drawing) {
        synchronized (drawings) {
            drawings.addElement(drawing);
            logger.debug("CPNDrawingLoader: added " + drawing.getName());
        }
    }

    /**
     * Informs the drawing loader that the given drawing has been
     * closed in the application.
     *
     * Only called from CPNApplication!
     *
     * @param drawing  the drawing to be unregistered.
     */
    protected void releaseDrawing(CPNDrawing drawing) {
        synchronized (drawings) {
            drawings.removeElement(drawing);
        }
        drawing.discardShadow();
        logger.debug("CPNDrawingLoader: released " + drawing.getName());
    }

    /**
     * Returns an iterator over all drawings currently loaded and
     * known to the loader.
     **/
    public Iterator<CPNDrawing> loadedDrawings() {
        if (logger.isDebugEnabled()) {
            StringBuffer allNames = new StringBuffer();
            for (Iterator<CPNDrawing> i = drawings.iterator(); i.hasNext();) {
                allNames.append(' ');
                allNames.append(i.next().getName());
            }
            logger.debug("CPNDrawingLoader: request for all drawings:"
                         + allNames);
        }
        return drawings.iterator();
    }

    /**
     * Returns a drawing for the given net name. If no matching
     * drawing is known and <code>useLoader</code> is set to true,
     * tries to load the drawing from a matching file (see
     * {@link #findDrawingFile}).
     *
     * @param name       the name of the drawing to look for
     *
     * @param useLoader  whether to use the drawing loader or not.
     *
     * @return
     *   the drawing with the given name -
     *   <code>null</code>, if it no matching drawing or file could
     *   be found.
     **/
    public CPNDrawing getDrawing(String name, boolean useLoader) {
        logger.debug("CPNDrawingLoader: request for " + name + " (loader "
                     + (useLoader ? "en" : "dis") + "abled)");
        if (name == null) {
            return null;
        }
        synchronized (drawings) {
            for (int i = 0; i < drawings.size(); i++) {
                CPNDrawing currentDrawing = drawings.elementAt(i);
                String currentName = currentDrawing.getName();

                if (name.equals(currentName)) {
                    return currentDrawing;
                }
            }
            if (useLoader) {
                CPNDrawing loaded = findDrawingFile(name);
                if (loaded != null) {
                    return loaded;
                }
            }
        }
        return null;
    }

    /**
     * Returns a drawing for the given net name. If no matching
     * drawing is known, tries to load the drawing from a matching
     * file. This method delegates to
     * {@link #getDrawing(String, boolean) getDrawing(name, true)}.
     *
     * @return
     *   the drawing with the given name -
     *   <code>null</code>, if it no matching drawing or file could
     *   be found.
     **/
    CPNDrawing getDrawing(String name) {
        return getDrawing(name, true);
    }

    public CPNDrawing findDrawing(String name, SimpleFileFilter type) {
        if (!drawingLoaded(name)) {
            for (PathEntry e : netSource) {
                File searchFile = new File(e.path + File.separator + name + "."
                                           + type.getExtension());

                //logger.debug("searchFile: "+searchFile);
                if (searchFile.canRead()) {
                    StatusRememberer sr = new StatusRememberer();
                    Drawing newDrawing = DrawingFileHelper.loadDrawing(searchFile,
                                                                       sr);

                    if (newDrawing != null && newDrawing instanceof CPNDrawing
                                && newDrawing.getName().equals(name)) {
                        return (CPNDrawing) newDrawing;
                    }
                }
            }
        }
        return null;
    }

    private boolean drawingLoaded(String name) {
        for (int i = 0; i < drawings.size(); i++) {
            CPNDrawing currentDrawing = drawings.elementAt(i);
            String currentName = currentDrawing.getName();

            if (name.equals(currentName)) {
                return true;
            }
        }
        return false;
    }

    /**
    * Tries to find and load a drawing for the given net name.
    * The drawing is loaded and opened in the application.
    * It is not checked whether a net drawing with the given name
    * is already loaded.
    *
    * @return
    *   <code>true</code>, if a drawing was loaded -
    *   <code>false</code>, if it no matching file could be found.
    **/
    CPNDrawing findDrawingFile(String name) {
        CPNDrawing tmp = getDrawing(name, false);
        if (tmp != null) {
            return tmp;
        }
        String path;
        URL url;
        File file = null;
        boolean error;
        for (int i = 0; i < netSource.length; ++i) {
            error = false;
            url = null;
            StringBuffer buffer = new StringBuffer();
            buffer.append(netSource[i].path);
            if (!"".equals(netSource[i].path)) {
                buffer.append(File.separator);
            }
            buffer.append(name);
            buffer.append(".rnw");
            path = buffer.toString();
            logger.debug("looking for: " + path);
            try {
                if (netSource[i].isClasspathRelative) {
                    url = ClassSource.getClassLoader()
                                     .getResource(StringUtil.convertToSlashes(path));
                    error = (url == null);
                } else {
                    file = new File(path);
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
                }
            } catch (Exception e) {
                logger.error("CPN drawing loader: " + path + " caused " + e);
                error = true;
            }
            if (!error) {
                assert url != null : "error should be set if url==null.";
                StatusRememberer statusRememberer = new StatusRememberer();
                Drawing newDrawing;
                if (file != null) {
                    newDrawing = DrawingFileHelper.loadDrawing(file,
                                                               statusRememberer);
                } else {
                    newDrawing = DrawingFileHelper.loadDrawing(url,
                                                               statusRememberer);
                }
                if (newDrawing != null && newDrawing instanceof CPNDrawing) {
                    final IOHelper ioHelper = DrawPlugin.getCurrent()
                                                        .getIOHelper();
                    if (ioHelper != null) {
                        final URL toOpen = url;

                        // TODO: Fix responsibilities for GuiPlugin, DrawPlugin and IOHelper!!!
                        if (SwingUtilities.isEventDispatchThread()) {
                            ioHelper.loadAndOpenDrawing(toOpen);
                        } else {
                            try {
                                EventQueue.invokeAndWait(new Runnable() {
                                        public void run() {
                                            ioHelper.loadAndOpenDrawing(toOpen);
                                        }
                                    });
                            } catch (InterruptedException e) {
                                logger.error("Caught interrupt while loading drawing",
                                             e);
                            } catch (InvocationTargetException e) {
                                logger.error("Caught InvocationTargetException while loading drawing",
                                             e);
                            }
                        }
                    }
                    return getDrawing(name, false);
                }
            }
        }
        LoadFileFromJarHelper.loadRnwFileFromJar(name);
        CPNDrawing drawing = getDrawing(name, false);
        return drawing;
    }

    private class StatusRememberer implements StatusDisplayer {
        public void showStatus(String message) {
            logger.debug("CPNDrawingLoader: " + message);
        }
    }
}