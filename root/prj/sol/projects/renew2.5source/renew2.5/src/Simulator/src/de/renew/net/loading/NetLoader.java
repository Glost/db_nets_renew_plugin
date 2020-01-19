package de.renew.net.loading;

import de.renew.net.Net;
import de.renew.net.NetNotFoundException;


/**
 *
 * <p>
 * </p>
 * NetLoader.java
 * Created: Tue Dec  4  2001
 * @author Michael Duvigneau
 **/
public interface NetLoader {

    /**
     * Tries to load a net from an external source (for example a
     * file). This method is called by the static part of {@link
     * Net} if and only if there is no known net for the given
     * name. When the loader returns the net, it has already been
     * compiled and added to the static <code>Net</code> lookup.
     *
     * <p>
     * A net loader implementation may safely assume that it is
     * executed within a simulation thread. This means that callers
     * have to ensure this.
     * </p>
     *
     * @param netName  the name of the missing net.
     *
     * @return
     *   the new <code>Net</code> object, if the loader was able to
     *   find and load the net from an external source.
     *   May not return <code>null</code> - the loader has to throw
     *   a <code>NetNotFoundException</code> instead.
     *
     * @throws NetNotFoundException
     *   if the loader could not find a source for a net with the
     *   given name.
     **/
    public Net loadNet(String netName) throws NetNotFoundException;
}