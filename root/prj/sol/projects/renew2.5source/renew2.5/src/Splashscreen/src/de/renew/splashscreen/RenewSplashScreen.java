package de.renew.splashscreen;

import org.apache.log4j.Logger;

import de.renew.plugin.load.IExtendedProgressBar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;

import javax.swing.JPanel;
import javax.swing.JWindow;


/**
 * This class creates the splashscreen. The splashscreen is composed by a given
 * background image panel and the {@link ExtendedProgressBar}.
 *
 * @author Eva Mueller
 * @date Nov 27, 2010
 * @version 0.2
 * @update Jan 23, 2012 Dominic Dibbern
 */
public class RenewSplashScreen extends JPanel {
    private static final Logger logger = Logger.getLogger(RenewSplashScreen.class);

    // private static final String splashBackground = "images" + File.separator
    // + "splashscreen.png";
    private static final String splashBackground = "splashscreen.png";
    private static BufferedImage img = null;
    private static Dimension size = new Dimension(500, 500);
    private static JWindow splashScreen;
    private IExtendedProgressBar extendedProgressBar;
    private ImagePanel imgPanel;
    private static RenewSplashScreen renewSplashScreen;

    /**
     * Private constructor to create singleton instance of this class.<br>
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    private RenewSplashScreen() {
        if (renewSplashScreen == null) {
            extendedProgressBar = ExtendedProgressBar.getInstance();
            renewSplashScreen = this;
        }
    }

    /**
     * Get (singleton) instance of the {@link RenewSplashScreen}.
     *
     * @return [{@link ExtendedProgressBar}]
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    public synchronized static RenewSplashScreen getInstance() {
        if (renewSplashScreen == null) {
            try {
                renewSplashScreen = new RenewSplashScreen();
            } catch (NoClassDefFoundError e) {
                logger.trace("Splash screen setup: Caught " + e);
                // This is a hack to fail more gracefully on unix systems
                // where the DISPLAY variable is configured to an
                // unaccessible X server.
                String reason = e.getMessage();
                if (reason != null
                            && reason.contains("sun.awt.X11GraphicsEnvironment")) {
                    logger.info("Splash screen deactivated by NoClassDefFoundError: "
                                + e.getMessage()
                                + "\nProbably the DISPLAY variable points to an inaccessible X server."
                                + "\nPlease check the variable and the X server configuration.");
                    // stay with default return value: null
                } else {
                    throw e;
                }
            }
        }
        return renewSplashScreen;
    }

    /**
     * Create and show the splashscreen.
     *
     * @param url
     *            [{@link URL}] The parent location of the loader.jar
     * @return [Boolean] <b>true</b> if creation was successfull, <b>false</b>
     *         else
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    public boolean showSplashScreen(URL url) {
        try {
            logger.debug("Setting up splash screen.");
            createBackgroundImage();
            splashScreen = new JWindow();
            setDisplayLocation();
            splashScreen.setSize(size);
            splashScreen.setLayout(null);
            splashScreen.setBackground(Color.WHITE);
            Container contentPane = splashScreen.getContentPane();
            contentPane.setBackground(Color.WHITE);

            // progressbar
            extendedProgressBar.setBounds(size.width / 6, 150,
                                          (int) (size.width / 1.5), 300);
            contentPane.add((Component) extendedProgressBar);

            // background image
            contentPane.add(imgPanel);

            splashScreen.validate();
            splashScreen.setVisible(true);
            splashScreen.toFront();

            return true;
        } catch (java.awt.HeadlessException e) {
            logger.info("Splash screen deactivated by HeadlessException: "
                        + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("Exception during splash screen setup: " + e, e);
            return false;
        }
    }

    /**
     * Get information about all known graphic devices and<br>
     * calculate the display location based on the first screen device.
     *
     * @throws NullPointerException
     *             if display location could not be determined
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    private void setDisplayLocation() throws NullPointerException {
        int screenWidth = 0;
        int screenHeight = 0;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment
                                         .getLocalGraphicsEnvironment();
            GraphicsDevice[] gs = ge.getScreenDevices();
            DisplayMode dm = gs[0].getDisplayMode();
            screenWidth = dm.getWidth();
            screenHeight = dm.getHeight();
        } catch (Exception e) {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            screenWidth = dim.getSize().width;
            screenHeight = dim.getSize().height;
        }
        if (screenHeight > 0 && screenWidth > 0) {
            int width = size.width / 2;
            int height = size.height / 2;
            splashScreen.setLocation((screenWidth / 2) - width,
                                     (screenHeight / 2) - height);
        } else {
            throw new NullPointerException("Could not determine display location.");
        }
    }

    /**
     * Close the splashscreen.
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    public void closeSplashScreen() {
        if (splashScreen != null) {
            // IExtendedProgressBar progressBar = ExtendedProgressBar
            // .getInstance();
            // if (progressBar.getValue() != 100) {
            // logger.debug("Forcing splash screen progress to 100%.");
            // progressBar.propertyChange(new PropertyChangeEvent(
            // new Object(), "progress", progressBar.getValue(), 100));
            // }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.debug("Splash screen progress update delay interrupted.  Skipping...");
            }
            logger.debug("Closing splash screen.");
            // splashScreen.setVisible(false);
            splashScreen.dispose();
            // splashScreen = null;
        }
    }

    /**
     * Create background image panel.
     *
     * @param loaderUrl
     *            [{@link URL}] The location of the loader.jar
     *
     * @throws IOException
     * @throws URISyntaxException
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    private void createBackgroundImage() throws IOException, URISyntaxException {
        InputStream inputStream = getInputStreamFromJarForFile(SplashscreenPlugin.getInstance()
                                                                                 .getProperties()
                                                                                 .getURL(),
                                                               splashBackground);
        if (inputStream != null) {
            img = ImageIO.read(inputStream);
            size.setSize(img.getWidth() + 20, img.getHeight() + 20);
        }
        imgPanel = new ImagePanel(img, size);
    }

    /**
     * Extract {@link InputStream} for given <b>file</b> in the jar file.<br>
     *
     * @param file
     *            [String] absolute path file to extract
     * @return {@link InputStream}
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    private InputStream getInputStreamFromJarForFile(URL url, String file)
            throws IOException, URISyntaxException {
        logger.debug("Loading " + file + " from " + url);
        JarFile jarRes = new JarFile(new File(url.toURI()));
        ZipEntry entry = jarRes.getEntry(file);
        return entry != null ? jarRes.getInputStream(entry) : null;
    }
}