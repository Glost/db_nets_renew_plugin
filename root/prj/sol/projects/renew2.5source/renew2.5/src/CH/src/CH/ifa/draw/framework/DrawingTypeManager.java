/*
 * Created on Apr 18, 2003
 */
package CH.ifa.draw.framework;

import CH.ifa.draw.io.IFAFileFilter;
import CH.ifa.draw.io.SimpleFileFilter;

import de.renew.plugin.PluginManager;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Lawrence Cabac
 */
public class DrawingTypeManager {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DrawingTypeManager.class);
    private static DrawingTypeManager dtm;
    private Hashtable<String, SimpleFileFilter> drawingTypes;
    private SimpleFileFilter defaultFileFilter;
    private Set<DrawingTypeManagerListener> listeners;

    private DrawingTypeManager() {
        drawingTypes = new Hashtable<String, SimpleFileFilter>();
        listeners = new HashSet<DrawingTypeManagerListener>();
    }

    public static DrawingTypeManager getInstance() {
        //this should be made more save fix!
        //i.e. the existence of the Class should be checked 
        //and maybe whether this implements Diagram (possible?)
        if (dtm == null) {
            dtm = new DrawingTypeManager();
            IFAFileFilter ifa = new IFAFileFilter();
            dtm.register("CH.ifa.draw.standard.StandardDrawing", ifa);
            dtm.setDefaultFileFilter(ifa);
            return dtm;
        }
        return dtm;
    }

    public Object register(String drawingName, SimpleFileFilter ff) {
        logger.debug("DrawingTypeManager: registering " + ff
                     + " for drawing type " + drawingName);
        Object previous = drawingTypes.put(drawingName, ff);
        for (Iterator<DrawingTypeManagerListener> listener = listeners.iterator();
                     listener.hasNext();) {
            listener.next().typeRegistered(drawingName, ff);
        }

        return previous;
    }

    static public Drawing getDrawingFromName(String name) {
        Drawing d = null;

        //logger.debug("The name of the Drawing dtm "+name);
        try {
            d = (Drawing) Class.forName(name, true,
                                        PluginManager.getInstance()
                                                     .getBottomClassLoader())
                               .newInstance();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return d;
    }

    public static Drawing getDrawingForFilter(SimpleFileFilter ff) {
        Hashtable<String, SimpleFileFilter> hash = getInstance()
                                                       .getDrawingTypes();
        Enumeration<String> enumeration = hash.keys();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            SimpleFileFilter fileFilter = hash.get(key);
            if (fileFilter.equals(ff)) {
                return getDrawingFromName(key);
            }
        }
        return null;
    }

    public Hashtable<String, SimpleFileFilter> getDrawingTypes() {
        return drawingTypes;
    }

    //    public void setDrawings(Hashtable tab) {
    //        drawingNames = tab;
    //    }
    public boolean contains(String name) {
        return drawingTypes.containsKey(name);
    }

    public SimpleFileFilter getFilter(String name) {
        return drawingTypes.get(name);

    }

    public SimpleFileFilter getDefaultFileFilter() {
        return defaultFileFilter;
    }

    public void setDefaultFileFilter(SimpleFileFilter filter) {
        if (!drawingTypes.contains(filter)) {
            throw new IllegalArgumentException("Channot choose unknown drawing type as default: "
                                               + filter);
        }
        logger.debug("DrawingTypeManager: choosing " + filter
                     + " as default drawing type.");
        defaultFileFilter = filter;
        for (Iterator<DrawingTypeManagerListener> listener = listeners.iterator();
                     listener.hasNext();) {
            listener.next().defaultTypeChanged(filter);
        }
    }

    public void addListener(DrawingTypeManagerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DrawingTypeManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Retracts a drawing type from the set of known types.
     * @param drawingName the name of the retracted drawing type
     */
    public void unregister(String drawingName) {
        logger.debug("DrawingTypeManager: unregistering "
                     + " for drawing type " + drawingName);
        drawingTypes.remove(drawingName);
        if (drawingName.equals(defaultFileFilter.getDescription())) {
            Enumeration<String> enu = drawingTypes.keys();
            if (enu.hasMoreElements()) {
                SimpleFileFilter ff = drawingTypes.get(enu.nextElement());
                setDefaultFileFilter(ff);
            } else {
                defaultFileFilter = null;
            }
        }

        for (Iterator<DrawingTypeManagerListener> listener = listeners.iterator();
                     listener.hasNext();) {
            listener.next().defaultTypeChanged(defaultFileFilter);
        }
    }
}