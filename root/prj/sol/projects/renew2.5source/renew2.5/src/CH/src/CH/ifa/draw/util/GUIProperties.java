package CH.ifa.draw.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * This helper class contains static functions which
 * can be queried for a default behaviour of some GUI
 * elements.
 * <p></p>
 * GUIProperties.java
 * Created: Wed Mar  7  2001
 * @author Michael Duvigneau
 */
public class GUIProperties {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(GUIProperties.class);
    private static Properties _properties = new Properties();
    private static Map<String, Boolean> lastBoolResults = new HashMap<String, Boolean>();

    /** This class cannot be instantiated. **/
    private GUIProperties() {
    }

    public static void setProperties(Properties props) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting GUI properties:" + props,
                         new Throwable("StackTrace"));
        }
        _properties = props;
    }

    public static Properties getProperties() {
        return _properties;
    }

    protected static boolean returnBool(String property, String setMessage) {
        String propName = "de.renew." + property;
        String result = _properties.getProperty(propName);
        if (result == null) {
            result = System.getProperty(propName);
            if (result != null) {
                _properties.setProperty(property, result);
            }
        }
        if (result == null) {
            result = "false";
            _properties.setProperty(property, result);
        } else if ("".equals(result)) {
            result = "true";
            _properties.setProperty(property, result);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("evaluated GUI property: " + propName + "=" + result);
        }
        Boolean value = Boolean.valueOf(result);
        printOptionalSetMessage(propName, value, setMessage);
        return value.booleanValue();
    }

    protected static void printOptionalSetMessage(String propName,
                                                  Boolean newValue,
                                                  String setMessage) {
        Boolean prevValue = lastBoolResults.get(propName);
        if (prevValue == null) {
            prevValue = Boolean.FALSE;
        }
        lastBoolResults.put(propName, newValue);
        if (!prevValue && newValue && setMessage != null) {
            logger.debug(setMessage + " activated.");
        } else if (prevValue && !newValue && setMessage != null) {
            logger.debug(setMessage + " deactivated.");
        }
    }

    protected static int returnInt(String property) {
        return returnInt(property, -1);
    }

    protected static int returnInt(String property, int default_) {
        String propName = "de.renew." + property;
        String str = _properties.getProperty(propName);
        int result = default_;
        if (str == null) {
            str = System.getProperty(propName);
        }
        if (str != null) {
            try {
                result = Integer.parseInt(str);
                _properties.setProperty(property, Integer.toString(result));
            } catch (NumberFormatException e) {
                logger.error("Error reading property " + propName + "=" + str
                             + ": " + e);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("evaluated GUI property: " + propName + "=" + result);
        }
        return result;
    }

    /**
     * Interprets the value of a system property to determine
     * if frames may be positioned or sized. This function should
     * be queried before each call to <code>setSize()</code>,
     * <code>setLocation()</code> or <code>setBounds()</code> of an
     * instance of <code>java.awt.Window</code>. Subsequent calls to
     * <code>pack()</code> or <code>setVisible(true)</code> should
     * also be avoided, if this function returns <code>true</code>.
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.avoidFrameReshape</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Program-controlled positioning and sizing of AWT windows.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT><code>false</code> (default):</DT>
     *     <DD>All windows will be positioned and sized at our needs.
     *     </DD>
     *     <DT><code>true</code>:</DT>
     *     <DD>Sacrifices the comfort of perfectly positioned windows
     *         to get windows that display their contents correctly.
     *         <br>
     *         This may be needed if under some circumstances (JDK1.3
     *         and twm, for example) the default behaviour will lead to
     *         "jumping" windows or windows with hidden areas.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static boolean avoidFrameReshape() {
        boolean result = returnBool("avoidFrameReshape", "Frame reshaping");
        return result;
    }

    /**
     * Interprets the value of a system property to determine
     * if a drawing load server should be started.
     * This function returns a positive integer value that
     * specifies the port, if the property is set.
     * It returns <code>-1</code>, if no load server should
     * be started.
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.loadServerPort</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Setup of a drawing load server.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT>&lt;not set&gt;:</DT>
     *     <DD>A drawing load server will be started at port 65111
     *     </DD>
     *     <DT>-1:</DT>
     *     <DD>No drawing load server will be started.
     *     </DD>
     *     <DT>positive integer:</DT>
     *     <DD>A drawing load server will be started.
     *         The integer value is interpreted as the port number
     *         where the server will listen for requests.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static int loadServerPort() {
        return returnInt("loadServerPort", 65111);
    }

    /**
     * Interprets the value of a system property to determine
     * the font size for the application menus.
     * This function returns a positive integer value that
     * specifies the font size, if the property is set.
     * It returns <code>-1</code>, if the default size should
     * be used.
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.menuFontSize</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Application menu font size.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT>&lt;not set&gt; (default):</DT>
     *     <DD>The AWT-default font size will be used..
     *     </DD>
     *     <DT>positive integer:</DT>
     *     <DD>The integer value is interpreted as a font size
     *         to be used for menus.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static int menuFontSize() {
        return returnInt("menuFontSize");
    }

    /**
     * Interprets the value of a system property to determine
     * if the main menu frame should be resizable.
     * <p>
     * <b>This property is no longer in use!</b>
     * The menu frame is resizable on all platforms since the
     * toolbars can be rearranged to fit the frame.
     * </p>
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.windowResizable</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Resizability of the menu frame.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT><code>true</code> (default on platform Windows):</DT>
     *     <DD>The menu frame will be resizable.
     *         This is needed on the Windows platform because the size
     *         of the menu frame does not match the size of its contents.
     *     </DD>
     *     <DT><code>false</code> (default on other platforms):</DT>
     *     <DD>The menu frame has a fixed size.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static boolean windowResizable() {
        String result = _properties.getProperty("de.renew.windowResizable");
        if (result == null) {
            result = System.getProperty("de.renew.windowResizable");
            if (result != null) {
                _properties.setProperty("de.renew.windowResizable", result);
            }
        }
        if (result == null) {
            // If no parameter is given, the window is
            // resizable on Windows-Systems only.
            if (System.getProperty("os.name").indexOf("Win") >= 0) {
                result = "true";
                _properties.setProperty("de.renew.windowResizable", result);
            }
        } else if ("".equals(result)) {
            result = "true";
            _properties.setProperty("de.renew.windowResizable", result);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("evaluated GUI property: de.renew.windowResizable="
                         + result);
        }
        Boolean value = Boolean.valueOf(result);
        printOptionalSetMessage("de.renew.windowResizable", value,
                                "Menu frame resizability");
        return value.booleanValue();
    }

    /**
     * Interprets the value of a system property to determine
     * the font size for new text figures.
     * This function returns a positive integer value that
     * specifies the font size, if the property is set.
     * It returns <code>-1</code>, if the default size should
     * be used.
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.defaultFontSize</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Default font size for new text figures.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT>&lt;not set&gt; (default):</DT>
     *     <DD>The AWT-default font size will be used..
     *     </DD>
     *     <DT>positive integer:</DT>
     *     <DD>The integer value is interpreted as a font size
     *         to be used for new text figures.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static int defaultFontSize() {
        return returnInt("defaultFontSize");
    }

    /**
     * Interprets the value of a system property to determine
     * if the drawing graphics should be updated in a simple,
     * but flickering way.
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>CH.ifa.draw.specialUpdate</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Graphics update strategy for drawings.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT><code>false</code> (default):</DT>
     *     <DD>The buffered update strategy will be used.
     *         This avoids flickering while the drawing is updated.
     *     </DD>
     *     <DT><code>true</code> (default for other JDKs):</DT>
     *     <DD>The simple update strategy will be used.
     *         This is needed for JDKs since version 1.2 because
     *         the buffered update causes errors.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static boolean specialUpdate() {
        String result = _properties.getProperty("CH.ifa.draw.specialUpdate");
        if (result == null) {
            result = System.getProperty("CH.ifa.draw.specialUpdate");
            if (result != null) {
                _properties.setProperty("CH.ifa.draw.specialUpdate", result);
            }
        }
        if (result == null) {
            String javaVersion = System.getProperty("java.version");
            if (javaVersion == null) {
                // This should not happen.
                javaVersion = "0.0";
            }
            String majorRelease = javaVersion.substring(0, 3);
            result = new Boolean(!majorRelease.equals("1.0")
                                 && !majorRelease.equals("1.1")).toString();
            _properties.setProperty("CH.ifa.draw.specialUpdate", result);
        } else if ("".equals(result)) {
            result = Boolean.TRUE.toString();
            _properties.setProperty("CH.ifa.draw.specialUpdate", result);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("evaluated GUI property: CH.ifa.draw.specialUpdate="
                         + result);
        }
        Boolean value = Boolean.valueOf(result);
        printOptionalSetMessage("CH.ifa.draw.specialUpdate", value,
                                "Special update strategy");
        return value.booleanValue();
    }

    /**
     * Interprets the value of a system property to determine if toolbar
     * buttons should be painted with selfmade shadow borders.  If the
     * property is set to false (default), default Swing borders are used.
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.customToolBorders</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Appearance of toolbar buttons.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT><code>false</code> (default):</DT>
     *     <DD>Default Swing borders around each button.
     *     </DD>
     *     <DT><code>true</code>:</DT>
     *     <DD>Shadow borders that mimic the old Renew tool button style.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static boolean customToolBorders() {
        return returnBool("customToolBorders",
                          "Drawing old-style Renew tool buttons.");
    }

    /**
     * Interprets the value of a system property to determine
     * if the close behaviour of the swing menu bar should be
     * fixed. This affects two bugs in the swing implementation
     * of Java 1.4:
     * <ul>
     * <li>Swing Menu doesn't unpost when focus change to anther window
     *     (Java bug 4218084).
     *     The counter measure is that all Renew windows will explicitly
     *     close the menu when they are activated. This will not help
     *     other applications, though.
     * </li>
     * <li>JMenu disappears if opening a modal dialog with JMenuItem
     *     (Java bug 4911422).
     *     The counter measure is a modified
     * </li>
     * </ul>
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.fixMenus</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Menu close behaviour.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT><code>false</code> (default for other JDKs):</DT>
     *     <DD>The behaviour of menus will not be altered.
     *     </DD>
     *     <DT><code>true</code> (default for JDK 1.4.x):</DT>
     *     <DD>Special care is taken to reduce the impact of these
     *         Java bugs.
     *     </DD></DL>
     * </DD></DL>
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4218084">Bug 4218084</a>
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4911422">Bug 4911422</a>
     **/
    public static boolean fixMenus() {
        String result = _properties.getProperty("de.renew.fixMenus");
        if (result == null) {
            result = System.getProperty("de.renew.fixMenus");
            if (result != null) {
                _properties.setProperty("de.renew.fixMenus", result);
            }
        }
        if (result == null) {
            String javaVersion = System.getProperty("java.version");
            if (javaVersion == null) {
                // This should not happen.
                javaVersion = "0.0";
            }
            String majorRelease = javaVersion.substring(0, 3);
            result = new Boolean(majorRelease.equals("1.4")).toString();
            _properties.setProperty("de.renew.fixMenus", result);
        } else if ("".equals(result)) {
            result = Boolean.TRUE.toString();
            _properties.setProperty("de.renew.fixMenus", result);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("evaluated GUI property: de.renew.fixMenus=" + result);
        }
        Boolean value = Boolean.valueOf(result);
        printOptionalSetMessage("de.renew.fixMenus", value, "Close menu fix");
        return value.booleanValue();
    }

    /**
     * Interprets the value of a system property to determine
     * whether graphics will be in use when loading a drawing.
     * <DL>
     * <DT><b>System property:</b></DT>
     * <DD><code>de.renew.noGraphics</code>
     * </DD>
     * <DT><b>Influences:</b></DT>
     * <DD>Graphics update when a drawing is loaded.
     * </DD>
     * <DT><b>Valid values:</b></DT>
     * <DD><DL><DT>&lt;not set&gt; (default):</DT>
     *     <DD>The display of figures will be updated on load.
     *     </DD>
     *     <DT>set:</DT>
     *     <DD>The display won't be actualized. This allows
     *         a programmer to load a drawing without starting
     *         the GUI. But it will cause exceptions if you
     *         try to draw the figures in this mode.
     *     </DD></DL>
     * </DD></DL>
     **/
    public static boolean noGraphics() {
        return returnBool("noGraphics", null);
    }
}