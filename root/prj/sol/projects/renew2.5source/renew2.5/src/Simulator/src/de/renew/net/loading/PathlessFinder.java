package de.renew.net.loading;

import de.renew.shadow.ShadowNetSystem;


/**
 * Interface for implementing classes to find nets without iterating
 *  the <code>netPath</code>.
 * For example, plug-ins might register implementations of this interface
 * to include their nets in the simulation.
 **/
public interface PathlessFinder {

    /**
    * Looks for a net of a given name in a given file
    * @param name the name of the net
    * @param path the file to look in
    * @return the found <code>ShadowNet</code> or <code>null</code> if none was found
    */
    public ShadowNetSystem findNetFile(String name);
}