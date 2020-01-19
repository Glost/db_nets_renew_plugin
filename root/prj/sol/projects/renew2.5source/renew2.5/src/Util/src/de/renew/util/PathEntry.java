package de.renew.util;

public class PathEntry {

    /**
     * A path to use when looking for nets. The path is stored using
     * OS-specific file system conventions.
     **/
    public final String path;

    /**
     * Indicates that the <code>path</code> component is considered to
     * be relative against the Java classpath. This means that this
     * <code>path</code> should not be looked up in the filesystem
     * directly, but used for a {@link ClassLoader#getResource} query.
     * The <code>CLASSPATH</code> keyword used in the String notation
     * of the net path has been removed from the path component.
     **/
    public final boolean isClasspathRelative;

    public PathEntry(String path, boolean isClasspathRelative) {
        this.path = path;
        this.isClasspathRelative = isClasspathRelative;
    }
}