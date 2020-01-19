/*
 * Created on 06.06.2003
 *
 */
package de.renew.plugin;

import java.io.File;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * This is a collection of static methods to be able to get typed values out
 *  java.util.Properties objects.
 *
 * @author J&ouml;rn Schumacher
 */
public class PropertyHelper {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PropertyHelper.class);

    public static int getIntProperty(Properties p, String property) {
        return getIntProperty(p, property, -1);
    }

    public static int getIntProperty(Properties p, String property,
                                     int defaultValue) {
        String str = p.getProperty(property, Integer.toString(defaultValue));
        int result = defaultValue;
        try {
            result = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            logger.warn("Invalid value for int property: " + property + "="
                        + str + "(using default: " + defaultValue + ").");
            // use the default value
        }
        return result;
    }

    /**
     * Returns the property as a bool with a given default value.
     * If the property is set to an empty String, "true" is
     * returned. This is to reflect the fact that it is true that
     * the property is set. If the property is set to anything
     * else than the empty string or "true", returns false.
     *
     * @param props      the <code>Properties</code> object where to
     *                   look up the given <code>property</code>
     *
     * @param property   the name of the property to convert to
     *                   boolean
     *
     * @param def        the default value
     *
     * @return   the boolean value of the property, false if not
     *           set.
     **/
    public static boolean getBoolProperty(Properties props, String property,
                                          boolean def) {
        String value = props.getProperty(property);
        if (value == null) {
            return def;
        } else if (value.trim().equals("")) {
            return true;
        } else {
            return Boolean.valueOf(value).booleanValue();
        }
    }

    /**
     * Returns the property as a bool with a default of "false".
     * If the property is set to an empty String, "true" is
     * returned. This is to reflect the fact that it is true that
     * the property is set. If the property is set to anything
     * else than the empty string or "true", returns false.
     *
     * @param props      the <code>Properties</code> object where to
     *                   look up the given <code>property</code>
     *
     * @param property   the name of the property to convert to
     *                   boolean
     *
     * @return   the boolean value of the property, false if not
     *           set.
     **/
    public static boolean getBoolProperty(Properties props, String property) {
        return getBoolProperty(props, property, false);
    }

    /**
     * Converts a String into a list, items separated by the given StringTokenizer
     *
     * @param list a String
     * @param tok a StringTokenizer
     * @param trim true if each token should be trimmed
     * @return Collection containing the tokens
     */
    public static Collection<String> parseListString(String list,
                                                     StringTokenizer tok,
                                                     boolean trim) {
        Collection<String> result = new Vector<String>(tok.countTokens());
        try {
            while (tok.hasMoreTokens()) {
                String currentToken = tok.nextToken();
                if (trim) {
                    currentToken = currentToken.trim();
                }
                result.add(currentToken);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("PluginLoader: " + e + " when parsing " + list
                         + " as list!");
        }
        return result;
    }

    /**
     * Converts a String into a list, items separated by commas, trims each token
     */
    public static Collection<String> parseListString(String list) {
        return parseListString(list, new StringTokenizer(list, ","), true);
    }

    /**
     * Converts a String containing paths sperarated with the system's path separator
     * (":" on Unixes, ";" on Windowses) into a collection containing the separate
     * paths.
     * @param list the list containing the paths to be returned as Collection
     * @return Collection containing the paths
     */
    public static Collection<String> parsePathListString(String list) {
        return parseListString(list,
                               new StringTokenizer(list, File.pathSeparator),
                               false);
    }

    /**
     * Returns a List containing Strings from the given properties object.
     * These are retrieved by getting properties [propName]_[index]
     * (with index starting from 0).
     */
    public static List<String> getListProperty(Properties props, String propName) {
        boolean goon = true;
        List<String> result = new Vector<String>();
        int i = 0;
        while (goon) {
            String currentProp = props.getProperty(propName + "_" + i);
            if (currentProp == null) {
                goon = false;
                break;
            } else {
                result.add(currentProp);
                i++;
            }
        }
        return result;
    }

    /**
     * Returns a <code>Class</code> object from the given
     * properties object, looking up the given property name.
     * The class given by the property must be a subclass of
     * <code>typeRequest</code>.
     * Returns <code>def</code> if the property is not set.
     * Also returns <code>def</code> if the property is set to an
     * invalid value (e.g. unknown class, incorrect type).
     **/
    public static Class<?> getClassProperty(Properties props, String propName,
                                            Class<?> typeRequest, Class<?> def) {
        Class<?> result = null;
        String className = props.getProperty(propName);
        if (className != null) {
            try {
                result = Class.forName(className, true,
                                       PluginManager.getInstance()
                                                    .getBottomClassLoader());
                if (!typeRequest.isAssignableFrom(result)) {
                    logger.error("Property " + propName
                                 + " is invalid, ignoring: " + result.getName()
                                 + " is not a subtype of "
                                 + typeRequest.getName() + ".");
                    result = null;
                }
            } catch (ClassNotFoundException e) {
                logger.error("Property " + propName
                             + " is invalid, ignoring: Class " + className
                             + " not found.");
                result = null;
            }
        }

        if (result == null) {
            result = def;
        }
        return result;
    }

    /**
     * Returns a <code>Class</code> object from the given
     * properties object, looking up the given property name.
     * Behaves in the same way as
     * {@link #getClassProperty(Properties,String,Class,Class) getClassProperty(props, propName, typeRequest, null)},
     * i.e. uses <code>null</code> as default return value.
     **/
    public static Class<?> getClassProperty(Properties props, String propName,
                                            Class<?> typeRequest) {
        return getClassProperty(props, propName, typeRequest, null);
    }
}