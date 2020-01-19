package de.renew.splashscreen;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.load.PluginLoaderComposition;

import java.net.URL;


/**
 * @author Dominic Dibbern
 * @date Jan 23, 2012
 * @version 0.1
 */
public class SplashscreenPlugin extends PluginAdapter {
    private static final String KEY_SPLASHSCREEN_ENABLED = "de.renew.splashscreen.enabled";
    private static SplashscreenPlugin _instance;

    public SplashscreenPlugin(URL url) throws PluginException {
        super(url);
        makeInstance();
        showScreen();
    }

    public SplashscreenPlugin(PluginProperties props) {
        super(props);
        makeInstance();
        showScreen();
    }

    private void makeInstance() {
        if (_instance == null) {
            _instance = this;
        }
    }

    private void showScreen() {
        if (getProperties().getBoolProperty(KEY_SPLASHSCREEN_ENABLED)) {
            RenewSplashScreen renewSplashScreen = RenewSplashScreen
                                                      .getInstance();
            if (renewSplashScreen != null) {
                renewSplashScreen.showSplashScreen(PluginManager
                    .getLoaderLocation());
                PluginLoaderComposition.setProgressBar(ExtendedProgressBar
                    .getInstance());
            }
        } else {
            logger.debug("Splash screen disabled by user choice.");
        }
    }

    public static SplashscreenPlugin getInstance() {
        return _instance;
    }
}