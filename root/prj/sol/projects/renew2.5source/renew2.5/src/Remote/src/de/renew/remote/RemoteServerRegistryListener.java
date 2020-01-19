package de.renew.remote;

import de.renew.remote.RemoteServerRegistry.ServerDescriptor;


/**
 * Allows to listen at the {@link RemoteServerRegistry} for
 * addition and removal of server entries.
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public interface RemoteServerRegistryListener {

    /**
     * Informs this listener that a server has been added to the
     * registry.
     *
     * @param desc the new server registry entry.
     */
    public void connectedTo(ServerDescriptor desc);

    /**
     * Informs this listener that a server has been removed from
     * the registry.
     *
     * @param desc the former server registry entry.
     */
    public void disconnectedFrom(ServerDescriptor desc);
}