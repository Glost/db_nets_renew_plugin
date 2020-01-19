package de.renew.shadow;

import de.renew.net.NetNotFoundException;
import de.renew.net.loading.Finder;
import de.renew.net.loading.PathlessFinder;

import de.renew.util.PathEntry;
import de.renew.util.StringUtil;

import java.io.File;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;


/**
 * Tries to load shadow nets from registered {@link de.renew.net.loading.Finder}
 * sources. The loader lets the registered finders look
 * for these files in the directories specified by the
 * <code>de.renew.netPath</code> system property.
 * <p>
 * </p>
 * DefaultShadowNetLoader.java <br>
 * Created: Thu Jan 17  2002 <br>
 * Modified: 12/29/2004 to enable use of finders by Till Kothe <br>
 * @author Michael Duvigneau
 * @author Till Kothe
 **/
public class DefaultShadowNetLoader implements ShadowNetLoader {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DefaultShadowNetLoader.class);

    //We need to use a LinkedHashSet otherwise we can not guaranty 
    //that the first registered finder (SNS) is asked first.
    private Set<Finder> finders = new LinkedHashSet<Finder>();
    private Set<PathlessFinder> pathlessFinders = new LinkedHashSet<PathlessFinder>();

    /**
     * An array of path entries denoting the directories where to
     * look for serialized <code>ShadowNetSystem</code> files.
     **/
    private PathEntry[] netSource;

    public DefaultShadowNetLoader(Properties props) {
        // TODO: the following maybe needs to go somewhere else
        // register a new default SNSFinder
        registerFinder(new SNSFinder());

        if (props != null) {
            configureNetPath(props);
        } else {
            configureNetPath(System.getProperties());
        }
    }

    /**
     * Configures the net search path from a given property set.
     *
     * @param props the property set to extract the
     *              <code>de.renew.netPath</code> property from.
     * @see #setNetPath(String)
     **/
    public void configureNetPath(Properties props) {
        setNetPath(props.getProperty("de.renew.netPath",
                                     System.getProperty("user.dir")));
    }

    /**
     * Sets a search path (like the CLASSPATH) to look for shadow
     * net system files when a net is missing. The directories in
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
        for (int i = 0; i < netSource.length; ++i) {
            logger.debug("Shadow net loader source"
                         + (netSource[i].isClasspathRelative
                            ? " (relative to CLASSPATH): " : ": ")
                         + netSource[i].path);
        }
    }

//    /**
//     * Creates an array of path entries from an array of path strings.
//     * Strings starting with the reserved word <code>"CLASSPATH"</code> are
//     * converted to classpath-relative path entries. All other path strings
//     * are converted to path entries that point to canonized directory
//     * names.
//     *
//     * @param paths  the <code>String</code> array with path names.
//     * @return an array of <code>PathEntry</code> objects.
//     **/
//    public static PathEntry[] canonizePaths(String[] paths) {
//        if (paths == null) {
//            return new PathEntry[0];
//        }
//        PathEntry[] canonizedEntries = new PathEntry[paths.length];
//        for (int i = 0; i < paths.length; ++i) {
//            if (paths[i].trim().startsWith("CLASSPATH" + File.separator)) {
//                canonizedEntries[i] = new PathEntry(paths[i].trim()
//                                                            .substring(9
//                                                                       + File.separator
//                                                                         .length()),
//                                                    true);
//            } else if (paths[i].trim().equals("CLASSPATH")) {
//                canonizedEntries[i] = new PathEntry("", true);
//            } else {
//                canonizedEntries[i] = new PathEntry(StringUtil.makeCanonical(paths[i]),
//                                                    false);
//            }
//        }
//        return canonizedEntries;
//    }

    /**
     * Creates an OS-dependent path string from the given array of path
     * entries. This is the reverse function of
     * <code>paths = canonizePaths(StringUtil.splitPaths(pathString))</code>.
     *
     * @param paths an array of <code>PathEntry[]</code> objects.
     * @return a path <code>String</code> including all entries of the
     * given array. Returns <code>""</code> if the array is empty or
     * non-existant.
     **/
    public static String asPathString(PathEntry[] paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }
        boolean first = true;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < paths.length; ++i) {
            if (paths[i] != null) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(File.pathSeparator);
                }
                if (paths[i].isClasspathRelative) {
                    buffer.append("CLASSPATH");
                    if (!"".equals(paths[i].path)) {
                        buffer.append(File.separator);
                    }
                }
                buffer.append(paths[i].path);
            }
        }
        return buffer.toString();
    }

    public ShadowNetSystem loadShadowNetSystem(String netName)
            throws NetNotFoundException {
        ShadowNetSystem loaded = findShadowNetSystemFile(netName);
        if (loaded != null) {
            return loaded;
        }
        throw new NetNotFoundException(netName);
    }

    public ShadowNet loadShadowNet(String netName, ShadowNetSystem netSystem)
            throws NetNotFoundException {
        ShadowNetSystem foundNetSystem = loadShadowNetSystem(netName);
        ShadowNet foundNet = foundNetSystem.elements().iterator().next();
        if (foundNet != null && netName.equals(foundNet.getName())) {
            foundNet.switchNetSystem(netSystem);
            return foundNet;
        }
        throw new NetNotFoundException(netName);
    }

    /**
     * Tries to find and load a net file for the given net name
     * from the configured net path or registered pathless finders.
     * The {@link Finder}s registered with this <code>
     * DefaultShadowNetLoader</code> are used to accomplish
     * the path-relative search.
     * For each net path entry the finders are asked
     * for the given net until one is found.
     * FIRST, the {@link PathlessFinder}s are queried.
     * (FIXME: this should be afterwards or inbetween).
     * The shadow net is loaded and returned.
     *
     * @param name the name of the net to load
     * @return the fresh shadow net, if one was loaded -
     *   <code>null</code>, if no matching file could be found.
     * @see #configureNetPath
     */
    public ShadowNetSystem findShadowNetSystemFile(String name) {
        ShadowNetSystem theNetSystem = null; // this will be returned. will be null if none is found.

        // forall pathless finders
        for (PathlessFinder finder : pathlessFinders) {
            logger.debug("Searching for net " + name + " in pathless finder "
                         + finder);
            theNetSystem = finder.findNetFile(name);
            if (theNetSystem != null && !theNetSystem.elements().isEmpty()) {
                // Net has been found. Return the containing net system!
                return theNetSystem;
            }
        }

        // forall netPath
        for (int i = 0; i < netSource.length; ++i) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(netSource[i].path);
            if (!"".equals(netSource[i].path)) {
                buffer.append(File.separator);
            }
            buffer.append(name);
            String path = buffer.toString();

            // path is net's name WITHOUT extension!
            // forall finder
            for (Finder finder : finders) {
                logger.debug("Searching for net " + buffer + " in finder "
                             + finder);
                if (netSource[i].isClasspathRelative) {
                    theNetSystem = finder.findNetClasspathRel(name, path);
                } else {
                    theNetSystem = finder.findNetFile(name, path);
                }
                if (theNetSystem != null && !theNetSystem.elements().isEmpty()) {
                    // Net has been found. Return the containing net system!
                    return theNetSystem;
                }
            }
        }

        return null;
    }

    /**
     * registers a new <code>Finder</code> which will be used when nets are
     * requested.
     * At most one instance of every <code>Finder</code> implementation will be
     * registered.
     * @param finder the <code>Finder</code> to register.
     * @return <code>true</code> if the finder was added. <code>false</code>
     * if it wasn't i.e. a finder of the same type was already registered.
     */
    public boolean registerFinder(Finder finder) {
        return this.finders.add(finder);
    }

    /**
     * Removes a <code>Finder</code> from the list of <code>Finder</code>s
     * which are used to load nets.
     *
     * @param finder the <code>Finder</code> to remove.
     * @return <code>true</code> if the finder has been successfully removed -
     * <code>false</code> if it wasn't in the list of the registered finders.
     */
    public boolean removeFinder(Finder finder) {
        return this.finders.remove(finder);
    }

    /**
     * registers a new <code>PathlessFinder</code> which will be used when nets are
     * requested.
     * At most one instance of every <code>PathlessFinder</code> implementation will be
     * registered.
     * @param finder the <code>PathlessFinder</code> to register.
     * @return <code>true</code> if the finder was added. <code>false</code>
     * if it wasn't i.e. a finder of the same type was already registered.
     */
    public boolean registerPathlessFinder(PathlessFinder finder) {
        return this.pathlessFinders.add(finder);
    }

    /**
     * Removes a <code>PathlessFinder</code> from the list of <code>Finder</code>s
     * which are used to load nets.
     *
     * @param finder the <code>PathlessFinder</code> to remove.
     * @return <code>true</code> if the finder has been successfully removed -
     * <code>false</code> if it wasn't in the list of the registered finders.
     */
    public boolean removePathlessFinder(PathlessFinder finder) {
        return this.pathlessFinders.remove(finder);
    }
}