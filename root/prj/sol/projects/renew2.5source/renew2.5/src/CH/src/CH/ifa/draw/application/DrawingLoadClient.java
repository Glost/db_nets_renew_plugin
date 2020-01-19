package CH.ifa.draw.application;

import CH.ifa.draw.util.GUIProperties;

import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import de.renew.util.StringUtil;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Class to load a list of drawings.
 */
public class DrawingLoadClient {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DrawingLoadClient.class);

    /**
     * Connects to localhost ("127.0.0.1") and tries to open the drawings given in the array.
     * @param fileName drawing names which should be opened.
     */
    public static void loadDrawings(String[] fileName) {
        int serverPort = GUIProperties.loadServerPort();
        if (serverPort == -1) {
            logger.error("No port specified, please set property de.renew.loadServerPort.");
            System.exit(1);
        }

        // connect to the server:
        try {
            Socket s = new Socket("127.0.0.1", serverPort);
            PrintWriter toSocket = new PrintWriter(s.getOutputStream());
            logger.info("Passing file names to " + s.getInetAddress() + ":"
                        + s.getPort() + "...");
            for (int i = 0; i < fileName.length; ++i) {
                toSocket.println(StringUtil.makeCanonical(fileName[i]));
            }
            toSocket.close();
            s.close();
        } catch (UnknownHostException e1) {
            logger.error("localhost (127.0.0.1) not found. Please set up TCP/IP correctly.");
            System.exit(2);
        } catch (IOException e2) {
            logger.error("Probably the DrawingLoadServer is not running on port "
                         + serverPort + "? Exception:\n" + e2);
            System.exit(2);
        }
    }

    /**
     * This is a workaround to get the plugin system's property service
     * without doing all the plugin initialization stuff.
     * Therefore, this main method should NOT be called when the plugin
     * system is running.
     *
     * @param argc array of drawings that should be loaded.
     */
    public static void main(String[] argc) {
        PluginManager.configureLogging();
        GUIProperties.setProperties(PluginProperties.getUserProperties());

        loadDrawings(argc);
    }
}