/**
 *
 */
package de.renew.pd.generating;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.io.SimpleFileFilter;

import de.renew.pd.PluginDevelopmentPlugin;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


/**
 * Generates a (Renew) plugin folder structure for a Renew Plugin.
 * Includes source folder structure, build.xml, plugin.cfg,
 *
 * This version is written for AOSE projects (2008ff):
 *
 *Excerpt from the Readme.pd
 * <pre>
 *  PluginDevelopment for Renew (Beta)
 *
 *  Menu Command extends Plugin menu by entry
 *  Plugin Development >> Create Renew Plugin Folder
 *
 *  Use as follows.
 *
 *  - Create a new text file "name.plg" (properties-like file)
 *  - add the entry (key value) "appName = <YourPluginName>"
 *  - Start Renew with pd and hit command.
 *  - pd will promt to create a new Plugin Directory
 *    default given in in <pd plugin load path>/../../../Renew
 *
 *    Will create:
 *    - src folder
 *    - etc/README.<name>
 *    - etc/plugin.cfg
 *    - build.xml
 *    </pre>
 *
 * @author cabac
 *
 */
public class StandardPluginGenerator implements PluginGenerator {
    private static String STANDARD_TEMPLATE_FOLDER = "de/renew/pd/templates/standard/";
    private static final String SOURCE_PATH_PREFIX = "src/de/renew/";
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StandardPluginGenerator.class);
    private String dirRenewCheckout = "";
    protected VelocityEngine ve;
    private URL location;
    private JFrame editorFrame;
    protected String templateFolder;

    /**
     */
    public StandardPluginGenerator() {
        this.templateFolder = STANDARD_TEMPLATE_FOLDER;
        ve = new VelocityEngine();

        ve.setProperty("resource.loader", "file, class");
        ve.setProperty("class.resource.loader.description",
                       "Velocity Classpath Resource Loader");
        ve.setProperty("class.resource.loader.class",
                       "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        try {
            ve.init();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        editorFrame = DrawPlugin.getGui().getFrame();
        location = PluginDevelopmentPlugin.getLocation();
        dirRenewCheckout = new File(location.getPath()).getParentFile()
                                                       .getParentFile()
                                                       .getParentFile()
                                                       .getParent();
    }

    /**
    * Tries to find a config file (*.plg) with the name 'test'.
    * Prompts for an alternative location if it does not exist. Does
    * some simple checks on the file system and triggers the creation of the
    * folder structure and the generation of the files from the templates.
    *
    */
    @Override
    public void generate() {
        logger.info(StandardPluginGenerator.class.getName() + "Root dir is: "
                    + dirRenewCheckout);
        String name = "test"; //hot-hack
        File plgfile = new File(dirRenewCheckout + File.separator + "develop"
                                + File.separator + name + ".plg");
        File propsfile = null;
        Properties props = new Properties();
        InputStream inStream;
        String appName = null;

        // if test exists ask if this should be used
        boolean plgFileChosen = false;
        if (plgfile.exists()) {
            int answer = JOptionPane.showConfirmDialog(editorFrame,
                                                       "Found 'test' in Renew/develop.\n"
                                                       + "do you want to use this file as your configuration file?");
            logger.info("PLG file found. Using default config file: " + name
                        + ".plg");
            if (answer == JOptionPane.OK_OPTION) {
                propsfile = plgfile;
                plgFileChosen = true;
            }
        }

        boolean proceedWithoutPropertyfile = false;

        // if no test file exists or test file is not chosen.
        if (!plgfile.exists() || !plgFileChosen) {
            // choose a prop file for the setup of a Renew-Plugin
            JFileChooser jfc = new JFileChooser(dirRenewCheckout);
            FileFilter ff = new PLGFileFilter();
            jfc.setFileFilter(ff);
            jfc.setDialogTitle("Choose a Plugin Setup Property File");
            int returnValue = jfc.showOpenDialog(DrawPlugin.getGui().getFrame());
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                propsfile = jfc.getSelectedFile();
            } else {
                String answer = JOptionPane.showInputDialog("You have not chosen a *.plg file.\n "
                                                            + "If you wish to create a Plugin without a plg file you need to provide a Name.");
                if (answer == null || answer.equals("")) {
                    logger.info(StandardPluginGenerator.class.getName()
                                + ": Canceled");
                    return;
                } else {
                    appName = answer;
                    proceedWithoutPropertyfile = true;
                    props.put("appName", appName);
                }
            } // else {
              //                logger.info("Cancel pressed.");
              //                return;
              //            }
        }

        //  not canceled  -- propsfile was set
        // try to read properties from it
        if (!proceedWithoutPropertyfile) {
            try {
                inStream = new FileInputStream(propsfile);
                props.load(inStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // check if property appName is set
        //NOTICEnull
        if (propsfile != null && !props.containsKey("appName")
                    && appName != null && !appName.equals("")) {
            logger.info("You must specify the porperty \"appName\".");
            JOptionPane.showMessageDialog(editorFrame,
                                          "Please set the proptery 'appName' in "
                                          + propsfile.getName());
            return;
        }
        appName = props.getProperty("appName");
        PluginManager pluginManager = PluginManager.getInstance();
        IPlugin pdPlugin = pluginManager.getPluginByName("Renew Plugin Development");
        PluginProperties pdProperties = pdPlugin.getProperties();
        String pdProperty = pdProperties.getVersion();
        props.setProperty("pdversion", pdProperty);
        logger.info("PluginDevelopment Plugin version: "
                    + props.getProperty("pdversion"));

        JFileChooser chooser = new JFileChooser(dirRenewCheckout
                                                + File.separator + "..");
        chooser.setSelectedFile(new File("Renew"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        chooser.setDialogTitle("Select Renew Plugin Source Folder.");
        chooser.setToolTipText("If you do not know better choose the Renew folder.");
        int returnValue = chooser.showDialog(editorFrame, "Choose Directory");

        File selectedDir;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedDir = chooser.getSelectedFile();
        } else {
            logger.info("User abort.");
            return;
        }
        if (!new File(selectedDir, "ant").exists()) {
            logger.info("Not a valid Root for Plugins.");
            return;
        }

        // assuming that now we have found the right location 
        // which should be a directory, where the NamedPlugin should be created in 
        String pluginParentDirName = selectedDir.getAbsolutePath();


        // check if plugin directory does not already exist
        if (new File(pluginParentDirName + File.separator + appName).exists()) {
            logger.info("Plugin as plugin named " + appName
                        + " already exists. Please erase and try again.");
            return;
        }
        createFolders(pluginParentDirName, appName);
        createFilesFromTemplates(pluginParentDirName, props);
    }

    /**
     * Prepares the context for velocity and creates the files from the
     * templates.
     *
     * @param props
     */
    protected void createFilesFromTemplates(String rootDir, Properties props) {
        try {
            // copy the build file
            Context context = getContextFromProperties(props);
            context.put("generic-description",
                        "This is the generated desctiption for this plugin.");
            context.put("generic-name",
                        props.getProperty("appName").toLowerCase());
            context.put("smallAppName",
                        props.getProperty("appName").toLowerCase());

            // copy the build.xml file
            createFile(ve, context, rootDir, "", "build.xml", null);

            // copy the plugin.cfg file
            createFile(ve, context, rootDir, "etc", "plugin.cfg", null);

            // copy the plugin class facade file
            createFile(ve, context, rootDir,
                       SOURCE_PATH_PREFIX + File.separator
                       + context.get("smallAppName"), "PluginClass.java",
                       props.getProperty("appName") + "Plugin.java");


        } catch (ResourceNotFoundException e) {
            logger.error(e.getMessage());
            logger.debug(e.getStackTrace());
        } catch (ParseErrorException e) {
            logger.error(e.getMessage());
            logger.debug(e.getStackTrace());
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.debug(e.getStackTrace());
        }
    }


    /**
    * Pipes the properties into a velocity context.
    *
    * @param properties -
    *            given Properties from configuration file (*plg)
    * @return a newly created context containing the values of the properties
    */
    protected Context getContextFromProperties(Properties properties) {
        Context context;
        context = new VelocityContext();
        Enumeration<Object> en = properties.keys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            context.put(key, properties.getProperty(key));

        }
        return context;
    }

    /**
     * Creates a given file from a template in a given folder using the velocity
     * engine.
     *
     * @param ve
     *            The velocity engine.
     * @param context
     *            The context for the created file.
     * @param foldername
     *            The name of the subfolder where the file should be created.
     * @param filename
     *            The name of the file to be created without extension.
     * @param extension
     *            The extension of the file to be created.
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws Exception
     * @throws IOException
     * @throws MethodInvocationException
     */
    protected void createFile(VelocityEngine ve, Context context,
                              String rootDir, String foldername,
                              String filename, String newfilename)
            throws ResourceNotFoundException, ParseErrorException, Exception,
                           IOException, MethodInvocationException {
        Template buildTemp;
        String destPath;
        File file;
        FileWriter fw;
        String sourceFilepath = this.templateFolder + filename + ".vm";
        logger.debug("PluginGenerator: Tamplates taken from: " + sourceFilepath);
        buildTemp = ve.getTemplate(sourceFilepath);
        String destFoldername = rootDir + File.separator
                                + context.get("appName");
        logger.debug("PluginGenerator: Destfoldername " + destFoldername);
        if (foldername != null && !foldername.equals("")) {
            destFoldername += File.separator + foldername;
            new File(destFoldername).mkdirs();
        }
        if (newfilename == null || "".equals(newfilename)) {
            destPath = destFoldername + File.separator + filename;
        } else {
            destPath = destFoldername + File.separator + newfilename;
        }
        file = new File(destPath);
        logger.debug("PluginGenerator: ----> writing: " + destPath);
        fw = new FileWriter(file);
        buildTemp.merge(context, fw);
        fw.close();
    }

    /**
     * Creates a folder structure in the Mulan root directory.
     *
     * @param appName
     *            The name of the application/plugin.
     * @param sourcePath
     */
    protected void createFolders(String rootDir, String appName) {
        String sourcePath = SOURCE_PATH_PREFIX + File.separator
                            + appName.toLowerCase();
        File directory = new File(rootDir + File.separator + appName
                                  + File.separator + sourcePath);
        directory.mkdirs();


    }

    class PLGFileFilter extends SimpleFileFilter {
        public PLGFileFilter() {
            super();
            setExtension("plg");
            setDescription("Plugin Configuration (*.plg)");
        }
    }
}