package de.renew.gui;

import de.renew.plugin.PropertyHelper;

import de.renew.remote.RemoteExtension;
import de.renew.remote.Server;

import java.awt.Component;

import java.util.Properties;


/**
 * An option panel controller to configure remote access properties.
 * @author Michael Duvigneau
 */
class ConfigureRemoteAccessController
        implements ConfigureSimulationTabController {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConfigureRemoteAccessController.class);
    private ConfigureRemoteAccessTab tab;

    public ConfigureRemoteAccessController() {
        this.tab = new ConfigureRemoteAccessTab(this);
    }

    public Component getTab() {
        return tab;
    }

    public void commitTab(Properties props) {
        boolean enabled = tab.getRemoteEnabled();
        props.setProperty(RemoteExtension.ENABLE_PROP_NAME,
                          Boolean.toString(enabled));
        String publicName = tab.getPublicName().trim();
        if (publicName.equals("")
                    || publicName.equals(Server.DEFAULT_SERVER_NAME)) {
            props.remove(RemoteExtension.NAME_PROP_NAME);
            publicName = null;
        } else {
            props.setProperty(RemoteExtension.NAME_PROP_NAME, publicName);
        }
        String className = tab.getServerClass().trim();
        if (className.equals("")
                    || className.equals(RemoteExtension.DEFAULT_SERVER_CLASS
                        .getName())) {
            props.remove(RemoteExtension.CLASS_PROP_NAME);
            className = null;
        } else {
            props.setProperty(RemoteExtension.CLASS_PROP_NAME, className);
        }

        logger.debug("ConfigureRemoteAccessController: "
                     + "Configured enabled=" + enabled + ", name=" + publicName
                     + ", class=" + className + ".");
    }

    public void updateTab(Properties props) {
        tab.setRemoteEnabled(PropertyHelper.getBoolProperty(props,
                                                            RemoteExtension.ENABLE_PROP_NAME));
        String className = props.getProperty(RemoteExtension.CLASS_PROP_NAME);
        if (className == null) {
            className = RemoteExtension.DEFAULT_SERVER_CLASS.getName();
        }
        tab.setServerClass(className);
        String publicName = props.getProperty(RemoteExtension.NAME_PROP_NAME);
        if (publicName == null) {
            publicName = Server.DEFAULT_SERVER_NAME;
        }
        tab.setPublicName(publicName);
        String factoryName = props.getProperty("de.renew.remote.socketFactory");
        if (factoryName == null) {
            factoryName = "";
        }
        tab.setSocketFactory(factoryName);
        enabledStateChanged();
    }


    /**
     * This method is called by the associated Swing component
     * whenever the user changes the enabledness of the remote
     * access.
     * It instructs the dialog tab to en- or disable the detailed
     * options group.
     **/
    void enabledStateChanged() {
        tab.enableDetailOptions(tab.getRemoteEnabled());
    }
}