package de.renew.shadow;

import de.renew.net.NetNotFoundException;


/**
 *
 * <p>
 * </p>
 * ShadowNetLoader.java
 * Created: Wed Dec  5  2001
 * @author Michael Duvigneau
 **/
public interface ShadowNetLoader {

    /**
     * Tries to load a shadow net from an external source (for
     * example a file) and creates a new <code> ShadowNetSystem</code>.
     * <p>
     * This method is called by the {@link DefaultCompiledNetLoader}
     * as a reaction of a call to {@link de.renew.net.Net#forName} if there
     * is no known net for the given name.
     * </p>
     *
     * @param netName    the name of the missing net.
     *
     * @return
     *   the new <code>ShadowNetSystem</code> object, if the loader was
     *   able to find and load the net from an external source.
     *   The net system must include information about the compiler factory
     *   to use.
     *   May not return <code>null</code> - the loader has to throw
     *   a <code>NetNotFoundException</code> instead.
     *
     * @throws NetNotFoundException
     *   if the loader could not find a source for a net with the
     *   given name.
     **/
    public ShadowNetSystem loadShadowNetSystem(String netName)
            throws NetNotFoundException;

    /**
     * Tries to load a shadow net from an external source (for
     * example a file) and inserts it into the given
     * <code>ShadowNetSystem</code>.
     * <p>
     * This method is called by the loopback net loader of a {@link ShadowNetSystem}
     * if it is already in the progress of loading a net, that net depends
     * on a net with the given name, and there is no known net for the
     * given name.
     * </p>
     *
     * @param netName    the name of the missing net.
     *
     * @param netSystem  the net system to put the loaded net into.
     *
     * @return
     *   the new <code>ShadowNet</code> object, if the loader was
     *   able to find and load the net from an external source.
     *   May not return <code>null</code> - the loader has to throw
     *   a <code>NetNotFoundException</code> instead.
     *
     * @throws NetNotFoundException
     *   if the loader could not find a source for a net with the
     *   given name.
     **/
    public ShadowNet loadShadowNet(String netName, ShadowNetSystem netSystem)
            throws NetNotFoundException;
}