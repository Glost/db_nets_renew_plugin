/*
 * Created on Apr 26, 2003
 */
package de.renew.diagram.peer;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.io.RNWFileFilter;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;


/**
 * This class allows to load a netcomponent during runtime from the tools directory.
 *
 * @author Lawrence Cabac
 */
public class NCLoader {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NCLoader.class);
    static private Map<String, NCLoader> loaderMap = new HashMap<String, NCLoader>();
    static private final StatusDisplayer sd = new StatusDisplayer() {
        public void showStatus(String string) {
            logger.info(string);
        }
    };
    private String path;

    private NCLoader(String path) {
        super();
        this.path = path;
        logger.debug("Path " + path);
    }

    static public NCLoader getInstance(URL pluginLocation) {
        return getInstance(pluginLocation, "tools");
    }

    static public NCLoader getInstance(URL pluginLocation, String toolsDirectory) {
        String path = new File(pluginLocation.getPath()).getParent() + "/"
                      + toolsDirectory + "/";

        NCLoader loader;
        if (loaderMap.containsKey(path)) {
            loader = loaderMap.get(path);
        } else {
            loader = new NCLoader(path);
            loaderMap.put(path, loader);
        }
        return loader;
    }

    public FigureEnumeration getfigures(String string) {
        Drawing drawing = loadDrawing(string);
        logger.debug("Drawing : " + drawing);
        if (drawing == null) {
            return null;
        }
        return drawing.figures();
    }

    private Drawing loadDrawing(String string) {
        File file = DrawingFileHelper.checkAndAddExtension(new File(path, string),
                                                           new RNWFileFilter());
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e1) {
            if (logger.isDebugEnabled()) {
                logger.debug(NCLoader.class.getSimpleName() + ": ", e1);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(NCLoader.class.getSimpleName()
                         + " Loading component: " + url.toString());
        }
        return DrawingFileHelper.loadDrawing(url, sd);
    }
}