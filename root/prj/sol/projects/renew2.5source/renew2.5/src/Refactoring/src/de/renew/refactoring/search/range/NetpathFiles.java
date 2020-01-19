package de.renew.refactoring.search.range;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;

import de.renew.util.PathEntry;
import de.renew.util.StringUtil;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Provides static methods that return files from the netpath.
 *
 * @author 2mfriedr
 */
public class NetpathFiles {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(NetpathFiles.class);

    /**
     * Should not be instantiated
     */
    private NetpathFiles() {
    }

    /**
     * Returns a list of files in the netpath deemed reasonable to search
     * regarding the specified filename extension and the pattern.
     *
     * @param extension the filename extension
     * @param pattern (unused) a search pattern
     * @return a list of files
     */
    public static List<File> files(final String extension, final String pattern) {
        // TODO `grep` in file system to reduce number of matches
        // TODO option to remove build/ and gensrc/ folders
        List<File> files = new ArrayList<File>();

        for (PathEntry pathEntry : netPathEntries()) {
            logger.debug("Processing net path entry: " + pathEntry.path);
            File path = new File(pathEntry.path);
            files.addAll(listFiles(path, extension));
        }
        return files;
    }

    /**
     * Returns a list of files in the netpath deemed reasonable to search
     * regarding the specified filename extensions and the pattern.
     *
     * @param extension a list of filename extensions
     * @param pattern (unused) a search pattern
     * @return a list of files
     */
    public static List<File> files(final List<String> extensions,
                                   final String pattern) {
        List<File> files = new ArrayList<File>();
        for (String extension : extensions) {
            files.addAll(files(extension, pattern));
        }
        return files;
    }

    private static List<PathEntry> netPathEntries() {
        // Copied from Navigator GUI
        IPlugin simulatorPlugin = PluginManager.getInstance()
                                               .getPluginsProviding("de.renew.simulator")
                                               .iterator().next();
        PathEntry[] entries = StringUtil.canonizePaths(StringUtil.splitPaths(simulatorPlugin.getProperties()
                                                                                            .getProperty("de.renew.netPath")));

        List<PathEntry> result = new ArrayList<PathEntry>(entries.length);

        // filter classpath entries
        for (int i = 0; i < entries.length; i++) {
            logger.debug("Processing net path entry: " + entries[i].path);
            if (!entries[i].isClasspathRelative) {
                // FIXME get resource from class loader (should classpath files be editable?)
                result.add(entries[i]);
            }
        }
        return result;
    }

    private static List<File> listFiles(final File directory,
                                        final String extension) {
        File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return StringUtil.getExtension(name).toLowerCase()
                                     .equals(extension.toLowerCase());
                }
            });

        if (files == null) {
            // path doesn't exist or there was an IO error
            return Collections.emptyList();
        }
        return Arrays.asList(files);
    }
}