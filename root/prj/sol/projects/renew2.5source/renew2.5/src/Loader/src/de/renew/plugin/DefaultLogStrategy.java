package de.renew.plugin;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.FactoryConfigurationError;


/**
 * A default log strategy which can be used on most systems.
 *
 * @author Dominic Dibbern
 * @version 1.0
 * @date 08.02.2012
 *
 */
public class DefaultLogStrategy implements LogStrategy {

    /**
     * The name of the property to set the location of the configuration file
     * for the configuration of the logging framework.
     */
    public static final String LOG_CONFIG_PROPERTY_NAME = "log4j.configuration";

    /**
     * The name of the property to set the directory for the log files. This
     * property key can be used in the log4j configuration file
     * log4j.properties. See the default log4j.properties file for an example
     * how to use the property logs.home.
     */
    public static final String LOG_HOME_PROPERTY_NAME = "logs.home";

    /**
     * The relative path to the default directory where the log files should be
     * located. The path is relative to the renew distribution directory where
     * the loader.jar is located.
     */
    public static final String LOG_HOME_DEFAULT_LOCATION = "renewlogs";

    /**
     * The relative path to the property file with the the log4j configuration.
     * The path is used relative to the directory where the loader.jar is
     * located.
     */
    public static final String LOG4J_PROPERTIES_FILE = "log4j.properties";

    /**
     * The relative path to the default xml file with the the log4j
     * configuration. The path is used relative to the directory where the
     * loader.jar is located.
     */
    public static final String LOG4J_XML_FILE = "log4j.xml";

    /**
     * Configures the log4j framework by trying to load properties from a
     * configuration file in the config directory of the renew distribution home
     * directory. To configure the log4j framework this method first look if the
     * property log4j.configuration is set. If this property is not set, an
     * existing xml configuration file with the name log4j.xml located in the
     * config directory of renew will be used. If no such file exists, a
     * property configuration file with the name log4j.properties will be used.
     * <p>
     * If there are configuration files with the upper names <em>and an
     * initial dot</em> in the user home directory, first the default files from
     * the config directory will be read and than additionally the config files
     * from the users home directory. The properties set in the users config
     * files overwrite the default properties.
     * </p>
     * <p>
     * If the system property "log4j.configuration" is set only the propery
     * settings from the given file are used.
     * </p>
     * TODO: Currenty only the XML users configuration file is used to configure
     * the logging framework if one exists, because the semantic merging of two
     * xml configuration files seems a little strange.
     */
    @Override
    public void configureLogging() {
        Vector<String> logMessages = new Vector<String>();
        String logConfigSource = null;

        logConfigSource = tryConfigurationByPropertyName(logMessages,
                                                         logConfigSource);

        if (logConfigSource == null) {
            // use default configuration files in the config directory
            // of the renew distribution for basic configuration properties.
            // Than look for an additional configuration file in the users
            // home directory. Set properties in the users configuration
            // file overwrites the properties set in the default configuration.
            try {
                // find renew distribution home directory
                URL url = PluginManager.getLoaderLocation();
                String base = url.toExternalForm();
                base = base.substring(0, base.lastIndexOf("/"));

                // try configuration using XML file in home directory
                logConfigSource = tryXMLConfigurationHome(logMessages,
                                                          logConfigSource, base);

                if (logConfigSource == null) {
                    // try configuration using Properties file in home directory
                    logConfigSource = tryPropertiesConfigurationHome(logMessages,
                                                                     logConfigSource,
                                                                     base);
                }
            } catch (Exception e) {
                logMessages.add(e.toString());
            }

            if (logConfigSource == null) {
                logConfigSource = fallbackConfiguration(logMessages);
            }
        }

        // now the log4j framework should be in any way configured,
        // so we can now output any stored log messages.
        if (!logMessages.isEmpty()) {
            Iterator<String> i = logMessages.iterator();
            while (i.hasNext()) {
                PluginManager.logger.error(i.next());
            }
        }
        if (PluginManager.logger.isDebugEnabled()) {
            PluginManager.logger.debug("Used " + logConfigSource
                                       + " to configure Log4j.");
        }
    }

    /**
     * Configures log4j with minimum fall-back values.
     * @param logMessages
     * @return
     */
    private String fallbackConfiguration(Vector<String> logMessages) {
        String logConfigSource;
        ConsoleAppender failsafeAppender = new ConsoleAppender(new SimpleLayout());
        failsafeAppender.setThreshold(Level.INFO);
        BasicConfigurator.configure(failsafeAppender);
        logMessages.add("No log4j configuration file found, falling back to failsaife settings.");
        logConfigSource = "failsafe settings";
        return logConfigSource;
    }

    /**
     * Tries the configuration of log4j using the LOG4J_PROPERTIES_FILE
     * in the .renew sub-folder of the users' home directory (~/.renew/LOG4J_PROPERTIES_FILE).
     * @param logMessages
     * @param logConfigSource
     * @param base
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private String tryPropertiesConfigurationHome(Vector<String> logMessages,
                                                  String logConfigSource,
                                                  String base)
            throws MalformedURLException, URISyntaxException, IOException,
                           FileNotFoundException {
        URL url;

        // try to use log4j.properties file for configuration
        File defaultConfigFile;
        File userConfigFile;

        url = new URL(base + "/config/" + LOG4J_PROPERTIES_FILE);
        defaultConfigFile = new File(new URI(url.toExternalForm()));
        userConfigFile = new File(PluginManager.getPreferencesLocation()
                                  + File.separator + "."
                                  + LOG4J_PROPERTIES_FILE);
        StringBuffer propsSource = new StringBuffer();
        Properties defaultProps = new Properties();
        boolean foundProps = false;
        if (defaultConfigFile.exists() && defaultConfigFile.isFile()) {
            defaultProps.load(new FileInputStream(defaultConfigFile));
            propsSource.append(defaultConfigFile.getAbsolutePath());
            foundProps = true;
        }
        if (userConfigFile.exists() && userConfigFile.isFile()) {
            defaultProps.load(new FileInputStream(userConfigFile));
            if (foundProps) {
                propsSource.append(" and ");
            }
            propsSource.append(userConfigFile.getAbsolutePath());
            foundProps = true;
        } else {
            File oldLocationOfConfigFile = new File(System.getProperty("user.home")
                                                    + File.separator + "."
                                                    + LOG4J_PROPERTIES_FILE);
            if (oldLocationOfConfigFile.exists()
                        && oldLocationOfConfigFile.isFile()) {
                logMessages.add("The default location for the configuration file of "
                                + "log4j logging was moved from the users' home folder to the \""
                                + PluginManager.getPreferencesLocation()
                                + "\" sub-directory.");
            }
        }
        if (foundProps) {
            try {
                PropertyConfigurator.configure(defaultProps);


                logConfigSource = propsSource.toString();
            } catch (Exception e) {
                logMessages.add("An error occured during the configuration of "
                                + "the logging framework using the properties configuration "
                                + "file(s) " + propsSource.toString()
                                + ". Error was: " + e.toString());
            }
        }
        return logConfigSource;
    }

    /**
     * Tries the configuration of log4j using the LOG4J_XML_FILE
     * in the .renew sub-folder of the users' home directory (~/.renew/LOG4J_XML_FILE).
     * @param logMessages
     * @param logConfigSource
     * @param base
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     * @throws FactoryConfigurationError
     */
    private String tryXMLConfigurationHome(Vector<String> logMessages,
                                           String logConfigSource, String base)
            throws MalformedURLException, URISyntaxException,
                           FactoryConfigurationError {
        URL url;

        // check if log.home is set. If not set to default location.
        // default location is a "renewlogs" directory in the users home
        // directory
        if (System.getProperty(LOG_HOME_PROPERTY_NAME) == null) {
            File logDir = new File(PluginManager.getPreferencesLocation()
                                   + File.separator + LOG_HOME_DEFAULT_LOCATION);

            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            if (!logDir.isDirectory()) {
                // use the parent directory as log files location
                logDir = logDir.getParentFile();
            } else {
                System.setProperty(LOG_HOME_PROPERTY_NAME,
                                   logDir.getAbsolutePath());
            }
        }

        // use default configuration files to configure the log4j
        // logging framework. First look for log4j.xml.
        url = new URL(base + "/config/" + LOG4J_XML_FILE);
        File defaultConfigFile = new File(new URI(url.toExternalForm()));

        // look for additional configuration file in the user home
        // directory
        // TODO: How should we handle the merging of two xml
        // configuration files.
        // Currenty only the users configuration file is used to
        // configure
        // the logging framework if one exists.
        File userConfigFile = new File(PluginManager.getPreferencesLocation()
                                       + File.separator + "." + LOG4J_XML_FILE);

        if (userConfigFile.exists() && userConfigFile.isFile()) {
            // the user config file overrides the default config file.
            defaultConfigFile = userConfigFile;
        }
        if (defaultConfigFile.exists() && defaultConfigFile.isFile()) {
            try {
                DOMConfigurator.configure(defaultConfigFile.getAbsolutePath());


                logConfigSource = defaultConfigFile.getAbsolutePath();
            } catch (Exception e) {
                logMessages.add("An error occured during the configuration of "
                                + "the logging framework using the xml configuration "
                                + "file in the default config directory of the renew distribution."
                                + "Error was: " + e.toString());
            }
        }
        return logConfigSource;
    }

    /**
     * Tries the configuration of log4j using the Properties file
     * found in the folder specified by the LOG_CONFIG_PROPERTY_NAME.
     * @param logMessages
     * @param logConfigSource
     * @return
     * @throws FactoryConfigurationError
     */
    private String tryConfigurationByPropertyName(Vector<String> logMessages,
                                                  String logConfigSource)
            throws FactoryConfigurationError {
        try {
            String configLocation = System.getProperty(LOG_CONFIG_PROPERTY_NAME);

            if (configLocation != null) {
                if (configLocation.endsWith(".xml")) {
                    DOMConfigurator.configure(configLocation);
                } else {
                    PropertyConfigurator.configure(configLocation);
                }


                logConfigSource = configLocation;
            }
        } catch (Exception e) {
            logMessages.add("A error occured while configuring the logging framework "
                            + "using the configuration file given by the property "
                            + LOG_CONFIG_PROPERTY_NAME
                            + ". The following exception was thrown: "
                            + e.toString());
        }
        return logConfigSource;
    }
}