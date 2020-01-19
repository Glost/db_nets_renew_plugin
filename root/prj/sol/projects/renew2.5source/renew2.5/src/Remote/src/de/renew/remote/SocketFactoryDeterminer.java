package de.renew.remote;

import de.renew.plugin.PluginManager;

import java.rmi.server.RMISocketFactory;


/**
 * Determines the RMI socket factory to be used for the remote package.
 * If the de.renew.remote.socketFactory Java VM property is specified,
 * this class is used, otherwise, the default factory is used.
 */
public class SocketFactoryDeterminer {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SocketFactoryDeterminer.class);

    /**
     * The determined factory.
     */
    private static RMISocketFactory factory = null;

    /**
     * Whether the socket factory property has already been read.
     */
    private static boolean propertyRead = false;

    /**
     * Determines the RMI socket factory to be used for the remote package.
     * @return The determined factory.
     */
    public static RMISocketFactory getInstance() {
        if (!propertyRead) {
            propertyRead = true;
            String socketFactoryClassName = System.getProperty("de.renew.remote.socketFactory");
            if (socketFactoryClassName != null) {
                try {
                    factory = (RMISocketFactory) Class.forName(socketFactoryClassName,
                                                               true,
                                                               PluginManager.getInstance()
                                                                            .getBottomClassLoader())
                                                      .newInstance();
                } catch (Exception e) {
                    logger.error("Cannot instantiate custom socket factory "
                                 + socketFactoryClassName + ": " + e
                                 + "\nUsing default factory.");
                }
            }
        }

        return factory;
    }
}