/*
 * Created on Dec 27, 2004
 *
 */
package de.renew.net.loading;

import de.renew.shadow.ShadowNetSystem;


/**
 * Abstract class for implementing classes to find nets.
 *
 * Created: Dec 27 2004
 * @author Till Kothe
 *
 */
public abstract class Finder {

    /**
     * Looks for a net of a given name in a given file
     * @param name the name of the net
     * @param path the file to look in
     * @return the found <code>ShadowNet</code> or <code>null</code> if none was found
     */
    public abstract ShadowNetSystem findNetFile(String name, String path);

    /**
     * Looks for a net of a given name in a given file. The file entry
     * is classpath relative.
     * @param name the name of the net
     * @param path the classpath relative filename to look for the net
     * @return the found <code>ShadowNet</code> or <code>null</code> if none was found
     */
    public abstract ShadowNetSystem findNetClasspathRel(String name, String path);


    /**
     * The <code>equals</code> method is overwritten, so that no two <code>Finder</code>
     * objects of the same implementation can be inserted into a <code>java.util.Set</code>.
     * This method returns true if and only if the parameter if of the same type as the
     * <code>Finder</code> whos <code>equals</code> method is called.
     *
     * @param o the Object to compare to this <code>Finder</code>.
     */
    public boolean equals(Object o) {
        return (o.getClass().getName().equals(this.getClass().getName()));
    }

    /**
     * As recommended the <code>hashCode</code> is overwritten, because the equals method is.
     * The <code>hashCode</code> returns the <code>Finder</code>'s class name's
     * <code>hashCode</code>.
     * (see {@link java.lang.String#hashCode()})
     */
    public int hashCode() {
        return (this.getClass().getName().hashCode());
    }
}