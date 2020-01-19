/*
 * Created: 06.05.2004
 *
 */
package de.renew.application;

import de.renew.net.loading.PathlessFinder;

import de.renew.plugin.load.AbstractPluginLoader;

import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetSystem;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/**
 * Special Extension that allows a plugin to add its Nets to a running
 * Simulation. Nets are loaded from the resources of the plugin.
 * <p>
 * The feature provided by this extension is experimental! The plugin's nets are
 * inserted into the simulation during the first call to
 * {@link SimulatorPlugin#insertNets}, which leads to a recursive call of that
 * method. As a result, other {@link SimulatorExtension} implementations may
 * observe the wrong order of net insertions.
 * </p>
 * <p>
 * TODO: It would be better if this extension uses the
 * {@link de.renew.net.loading.NetLoader} interface instead of compiling all
 * nets at simulation setup.
 * </p>
 *
 * @author Timo Carl
 * @author (additional comments by Michael Duvigneau)
 */
public class AddPluginNetsExtension implements SimulatorExtension,
                                               PathlessFinder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(AddPluginNetsExtension.class);
    private JarFile[] jarRes = null;
    private Map<String, ShadowNetSystem> netSnsMap = null;

    /**
         * Creates the extension.
         *
         * @param resPath
         *                the <code>URL</code> to the <code>.jar</code> file
         *                where the nets reside.
         */
    public AddPluginNetsExtension(URL resPath) {
        logger.debug("AddPluginNetsExtension with URL " + resPath);
        try {
            URL[] urls = AbstractPluginLoader.unifyURL(resPath);

            jarRes = new JarFile[urls.length];

            for (int i = 0; i < urls.length; i++) {
                jarRes[i] = new JarFile(new File(urls[i].toURI()));
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Resource path " + resPath
                            + " is not a valid jar file: " + e, e);
            } else {
                logger.warn("Resource path " + resPath
                            + " is not a valid jar file.");
            }
        } catch (URISyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Resource path " + resPath
                            + " is not a valid jar file: " + e, e);
            } else {
                logger.warn("Resource path " + resPath
                            + " is not a valid jar file.");
            }
        }
    }

    public void simulationSetup(SimulationEnvironment env) {
        // reset sns index so it can be rebuilt on first use.
        synchronized (this) {
            netSnsMap = null;
        }
    }

    public void netsCompiled(ShadowLookup oldLookup) {
        // no longer interested in this event?
    }

    protected synchronized Map<String, ShadowNetSystem> getNetSnsMap() {
        if (netSnsMap == null) {
            netSnsMap = new HashMap<String, ShadowNetSystem>();
            for (JarFile jf : jarRes) {
                logger.debug("AddPluginNetsExtension: Building net-to-sns map for "
                             + jf.getName());


                // assumes that jarRes is valid because Plugin would not
                // be functional.
                Enumeration<JarEntry> entries = jf.entries();
                String fileName = null;
                while (entries.hasMoreElements()) {
                    try {
                        ZipEntry element = entries.nextElement();
                        fileName = element.toString();
                        if (!element.getName().endsWith(".sns")) {
                            continue;
                        }
                        Object o = new ObjectInputStream(jf.getInputStream(element))
                                   .readObject();
                        if (!(o instanceof ShadowNetSystem)) {
                            continue;
                        }
                        logger.debug("AddPluginNetsExtension: adding sns file "
                                     + fileName);
                        ShadowNetSystem newSns = (ShadowNetSystem) o;
                        for (ShadowNet net : newSns.elements()) {
                            String netName = net.getName();
                            if (netSnsMap.containsKey(netName)) {
                                logger.warn("AddPluginNetsExtension: Skipping duplicate net definition for "
                                            + netName + " in sns file "
                                            + fileName + " of jar " + jarRes
                                            + ".");
                            } else {
                                netSnsMap.put(net.getName(), newSns);
                            }
                        }
                        fileName = null;
                    } catch (ClassNotFoundException ce) {
                        logger.error("AddPluginNetsExtension: Could not read shadow net system from file "
                                     + fileName + " within jar  " + jarRes
                                     + " due to " + ce + ".", ce);
                    } catch (IOException ioe) {
                        logger.error("AddPluginNetsExtension: Could not read shadow net system from file "
                                     + fileName + " within jar  " + jarRes
                                     + " due to " + ioe + ".", ioe);
                    }
                }
            }
        }
        return netSnsMap;
    }

    public void simulationTerminated() {
        // no longer interested in this event?
    }

    public void simulationTerminating() {
        // Nothing to do ?
    }

    public ShadowNetSystem findNetFile(final String name) {
        if (logger.isDebugEnabled() && getNetSnsMap().containsKey(name)) {
            logger.debug("AddPluginNetsExtension: providing sns for net "
                         + name + ": " + getNetSnsMap().get(name) + ".");
        }
        return getNetSnsMap().get(name);
    }
}