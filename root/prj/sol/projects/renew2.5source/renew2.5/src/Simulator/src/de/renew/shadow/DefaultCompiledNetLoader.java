package de.renew.shadow;

import de.renew.application.SimulatorPlugin;

import de.renew.net.Net;
import de.renew.net.NetNotFoundException;
import de.renew.net.loading.NetLoader;


/**
 * A generic net loader implementation which delegates to a
 * {@link ShadowNetLoader}.
 * <p>
 * </p>
 * DefaultCompiledNetLoader.java
 * Created: Tue Dec  5 2001
 * @author Michael Duvigneau
 **/
public class DefaultCompiledNetLoader implements NetLoader {
    private ShadowNetLoader netLoader;

    /**
     * Creates a default compiled net loader for the given shadow
     * net system, delegating to the given shadow net loader.
     *
     * @exception NullPointerException
     *  if either <code>netSystem</code> or <code>netLoader</code>
     *  is <code>null</code>.
     **/
    public DefaultCompiledNetLoader(ShadowNetLoader netLoader) {
        if (netLoader == null) {
            throw new NullPointerException("Missing ShadowNetLoader.");
        }
        this.netLoader = netLoader;
    }

    public Net loadNet(String netName) throws NetNotFoundException {
        try {
            ShadowNetSystem netSystem = netLoader
                                            .loadShadowNetSystem(netName);
            ShadowLookup lookup = SimulatorPlugin.getCurrent()
                                                 .insertNets(netSystem);
            if (lookup != null) {
                Net result = lookup.getNet(netName);
                if (result != null) {
                    return result;
                }
            }
        } catch (NetNotFoundException e) {
            // Just forward the information from the shadow net loader...
            throw e;
        } catch (Exception e) {
            throw new NetNotFoundException(netName, e);
        }
        throw new NetNotFoundException(netName);
    }
}