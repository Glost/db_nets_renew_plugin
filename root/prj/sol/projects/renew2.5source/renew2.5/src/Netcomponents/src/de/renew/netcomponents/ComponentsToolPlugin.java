package de.renew.netcomponents;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;
import CH.ifa.draw.application.VersionInfoCommand;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;

import de.renew.gui.CPNApplication;
import de.renew.gui.GuiPlugin;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.PropertyHelper;

import java.awt.Font;
import java.awt.event.KeyEvent;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;


/**
 * The wrapper for the ComponentsTool.
 *
 * <pre>
 *
 *
 *  0.2 user can choose toolsdir directory via GUI
 *      palettes can be removed.
 *
 *  0.3 remove problems solved
 *      the default palette is part of the list
 *
 *  0.3.2 status message for nothing to do while removing nothing.
 *
 *  0.3.3 adapted to log4j logging
 *
 *  0.4   separating general functionality from the mulan components
 *        latter reside in plugin MulanComponents, now
 *
 *  0.4.2 Fixed read/write problems.
 *
 *  0.4.6 Adapted changed signature of constructor of ComponentsTool.
 *
 *  0.5.0 Fixed deregistering of plugins.
 *        Plugin-plugins can provide other <code>ToolButton</code> now.
 *               Changed interface of <code>ComponentsPluginExtender</code> and
 *        some method signatures.
 *
 *  0.6.0 Added possible visual representation of the net component figure.
 *        Made initialization of repository more robust.
 *        Removed necessity to include generic image: 1.gif.
 *
 * </pre>
 *
 * @author Lawrence Cabac
 * @version 0.6.0
 *
 */
public class ComponentsToolPlugin extends PluginAdapter {
    org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ComponentsToolPlugin.class);
    /*
     * Not used yet(?)
     */
    private CommandMenu _menu;

    /**
     * The default ComponentsTool.
     */
    private ComponentsTool defCT;

    /**
     * The list of additional loaded ComponentsTools.
     */
    private Vector<ComponentsTool> componentsToolList;
    static private URL location;
    private HashMap<String, ComponentsPluginExtender> _pluginList;
    private Hashtable<Command, ComponentsPluginExtender> _commandList;

    public ComponentsToolPlugin(URL location) throws PluginException {
        super(location);

        componentsToolList = new Vector<ComponentsTool>();
        setDefaultCT(null);
        setLocation(location);
        _pluginList = new HashMap<String, ComponentsPluginExtender>();

    }

    public ComponentsToolPlugin(PluginProperties props) {
        super(props);

        componentsToolList = new Vector<ComponentsTool>();
        setDefaultCT(null);
        setLocation(props.getURL());
        _pluginList = new HashMap<String, ComponentsPluginExtender>();
        _commandList = new Hashtable<Command, ComponentsPluginExtender>();
    }

    /**
     * @see de.renew.plugin.IPlugin#init()
     */
    public void init() {
        DrawPlugin dp = DrawPlugin.getCurrent();

        _menu = createMenu();
        MenuManager mm = dp.getMenuManager();

        //SeparatorFactory sepFac = new SeparatorFactory("de.renew.nc");
        //mm.registerMenu(DrawPlugin.TOOLS_MENU, sepFac.createSeparator());
        mm.registerMenu(DrawPlugin.TOOLS_MENU, _menu);

        String toolDirProp = ComponentsTool.TOOLDIRPROPERTY;
        String toolDir = getProperties().getProperty(toolDirProp);
        if (toolDir != null) {
            System.setProperty(toolDirProp, toolDir);
            logger.debug("NetComponents: " + toolDirProp + " set to " + toolDir);
            try {
                if (PropertyHelper.getBoolProperty(getProperties(), "nc.init")) {
                    logger.info("Your init option: de.renew.nc.init is set to true.");
                    logger.info("This option is not supported anymore, whatsoever.");
                }

                boolean init = PropertyHelper.getBoolProperty(getProperties(),
                                                              "de.renew.nc.init");
                if (init) {
                    //since there is no default palette provided by this plugin
                    //the init option is ignored, here. Add a init option to 
                    //(sub)plugin providing the palette.
                    //createPalette();
                    logger.info("Your init option: de.renew.nc.init is set to true.");
                    logger.info("However, this is not supported anymore.");
                    logger.info("Please set the init option of the plugin providing your default palette.");
                }
            } catch (RuntimeException e) {
            }
        } else {
            logger.error("NetComponents: " + toolDirProp
                         + " not set in plugin.cfg.");
        }
    }

    /**
     *
     */
    public void registerPlugin(ComponentsPluginExtender plugin) {
        _pluginList.put(plugin.getToolDirPath(), plugin);
        for (Command command : plugin.getMenuCommands()) {
            _menu.add(command);
            _commandList.put(command, plugin);
            logger.debug("Added command " + command.toString() + " of Plugin "
                         + plugin);
        }
    }

    /**
     *
     */
    public void deregisterPlugin(IPlugin plugin) {
        Enumeration<Command> it = _commandList.keys();

        // Removing all menu entries for the deregistering plugin.
        while (it.hasMoreElements()) {
            Command command = it.nextElement();
            if (_commandList.get(command) == plugin) {
                _menu.remove(command);
                _commandList.remove(command);
            }
        }
        _pluginList.remove(plugin);

    }

    public boolean addEntryToMenu(Command command) {
        _menu.add(command);
        return false;
    }

    public boolean removeEntryFromMenu(Command command) {
        _menu.remove(command);
        return false;
    }

    public boolean cleanup() {
        unloadAllPalettes();
        DrawPlugin.getCurrent().getMenuManager().unregisterMenu(_menu);
        return true;
    }

    private void unloadAllPalettes() {
        Iterator<ComponentsTool> it = componentsToolList.iterator();
        while (it.hasNext()) {
            ComponentsTool ct = it.next();
            ct.remove();
        }
        componentsToolList = new Vector<ComponentsTool>();
    }

    /**
     * Creates a new default palette and refreshes the menuFrame, if not already
     * loaded. If loaded the palette gets removed.
     */
    public void createPalette() {
        // toggel on/off
        try {
            GuiPlugin starter = GuiPlugin.getCurrent();
            if (starter == null) {
                logger.error("no gui starter object! cannot create palette.");
                return; //NOTICEnull add return statement
            }
            if (getDefaultCT() == null) {
                ComponentsTool ct = new ComponentsTool();

                //check whether a valid tools directory has been loaded
                if (ct.toolsDirIsValid()) {
                    //                    _site.menuFrame().pack();
                    componentsToolList.add(ct);
                    setDefaultCT(ct);

                } else {
                    JOptionPane.showMessageDialog(starter.getGuiFrame(),
                                                  "The '"
                                                  + ComponentsTool.TOOLDIRPROPERTY
                                                  + "' propery is not set to a valid net component directory."
                                                  + "\n Set this property in \"renew.properties\" "
                                                  + "in the \"config\" directory of your renew installation and restart the program"
                                                  + "\nor use the menu entry \"Edit > Netcomponents > select from directory\"!",
                                                  "Abort",
                                                  JOptionPane.WARNING_MESSAGE,
                                                  new ImageIcon(starter.getClass()
                                                                       .getResource(CPNApplication.CPNIMAGES
                                                                                    + "RENEW.gif")));
                }
            } else {
                //get rid of the default palette and remove from List
                getDefaultCT().remove();
                componentsToolList.remove(getDefaultCT());
                setDefaultCT(null);
            }
        } catch (Exception ee) {
            logger.error("Something went wrong while toggling the default palette. Palette is: "
                         + getDefaultCT() + " Message: " + ee);

            if (getDefaultCT() != null) {
                logger.error("Directory is: " + getDefaultCT().getLabel());
            }
        }
    }

    /**
     * Lets the user choose the tools directory. Creates a new palette and
     * refreshes the menuFrame.
     */
    public void selectDirAndCreatePalette() {
        File dir = getDirectory();
        String dirName = "";

        if (dir != null) {
            GuiPlugin starter = GuiPlugin.getCurrent();
            if (starter == null) {
                logger.error("no gui starter object! cannot create palette.");
                return; //NOTICEnull added return statement
            }
            try {
                dirName = dir.getCanonicalPath();
            } catch (Exception se) {
                logger.error("Scurity of FilePath violated. File: "
                             + dir.getName() + "\n Exception: " + se);
            }

            ComponentsTool componentsTool = new ComponentsTool(dirName,
                                                               "Components",
                                                               null);

            if (componentsTool.toolsDirIsValid()) {
                componentsToolList.add(componentsTool);
            } else {
                JOptionPane.showMessageDialog(starter.getGuiFrame(),
                                              "No valid net component directory was selected.",
                                              "Abort",
                                              JOptionPane.WARNING_MESSAGE,
                                              new ImageIcon(starter.getClass()
                                                                   .getResource(CPNApplication.CPNIMAGES
                                                                                + "RENEW.gif")));
            }
        }
    }

    /**
     * Lets the user choose the tools directory. Creates a new palette and
     * refreshes the menuFrame.
     */
    public void createPalette(String name, String paletteName,
                              ComponentsPluginExtender plugin) {
        File dir = null;
        if (logger.isDebugEnabled()) {
            logger.debug(ComponentsToolPlugin.class.getSimpleName() + ": "
                         + name);
        }
        try {
            dir = new File(new URI("file://" + name));
        } catch (URISyntaxException e) {
            logger.error(ComponentsToolPlugin.class.getSimpleName()
                         + "Could not find dierectory: " + name + "/n"
                         + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(ComponentsToolPlugin.class.getSimpleName()
                             + ": Could not find dierectory: " + name + "/n"
                             + e);
            }
        }
        String dirName = name;
        if (dir != null && dir.exists()) {
            GuiPlugin starter = GuiPlugin.getCurrent();
            if (starter == null) {
                logger.error("no gui starter object! cannot create palette.");
                return; //NOTICE null added return statement
            }
            try {
                dirName = dir.getCanonicalPath();
            } catch (Exception se) {
                logger.error("Security of FilePath violated. File: "
                             + dir.getName() + "\n Exception: " + se);
            }
            if (!componentsToolListcontains(dirName)) {
//            	logger.info("Looking for plugin in _pluginList. Name: "+ dirName);
//            	logger.info("List is "+_pluginList);
//            	ComponentsPluginExtender plugin = _pluginList.get(dirName);
//            	logger.info("Found plugin "+ plugin);
                ComponentsTool componentsTool = new ComponentsTool(dirName,
                                                                   paletteName,
                                                                   plugin);

                if (componentsTool.toolsDirIsValid()) {
                    componentsToolList.add(componentsTool);

                } else {
                    JOptionPane.showMessageDialog(starter.getGuiFrame(),
                                                  "No valid net component directory was selected.",
                                                  "Abort",
                                                  JOptionPane.WARNING_MESSAGE,
                                                  new ImageIcon(starter.getClass()
                                                                       .getResource(CPNApplication.CPNIMAGES
                                                                                    + "RENEW.gif")));
                }
            } else {
            }
        }
    }

    /**
     * @param componentsToolDirName
     * @return
     */
    private boolean componentsToolListcontains(String componentsToolDirName) {
        //boolean check = false;
        Iterator<ComponentsTool> it = componentsToolList.iterator();
        while (it.hasNext()) {
            ComponentsTool ct = it.next();
            if (ct.getLabel().equals(componentsToolDirName)) {
                return true;
            }
        }

        return false;
    }

    public void removeLastPalette() {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter == null) {
            logger.error("no gui starter object! cannot remove last palette.");
            return; //NOTICEnull added return statement
        }
        if (!componentsToolList.isEmpty()) {
            ComponentsTool componentsTool = componentsToolList.lastElement();

            if (componentsTool == getDefaultCT()) {
                setDefaultCT(null);
            }

            componentsTool.remove();
            componentsToolList.remove(componentsTool);
            componentsTool = null;
            //            _site.menuFrame().pack();
        } else {
            starter.showStatus("Nothing to do.");
        }
    }

    public void removePalette() {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter == null) {
            logger.error("no gui starter object! cannot remove palette.");
            return; //NOTICEnull added return statement
        }
        if (!componentsToolList.isEmpty()) {
            new RemoveToolsControl(this);
        } else {
            starter.showStatus("Nothing to do.");
        }
    }

    public void removePalette(String toolDirPath) {
        Iterator<ComponentsTool> it = componentsToolList.iterator();
        ComponentsTool toRemove = null;
        while (it.hasNext()) {
            ComponentsTool ct = it.next();
            if (toolDirPath.equals(ct.getLabel())) {
                toRemove = ct;
            }
        }
        if (toRemove != null) {
            componentsToolList.remove(toRemove);
            toRemove.remove();
            toRemove = null;
        }
    }

    public File getDirectory() {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter == null) {
            logger.error("no gui starter object! cannot create palette.");
            return null; //NOTICEnull added return statement
        }
        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int check = chooser.showOpenDialog(starter.getGuiFrame());

        if (check == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }

        return null;
    }

    /**
     * Get the value of CTList.
     *
     * @return value of CTList.
     */
    public Vector<ComponentsTool> getCTList() {
        return componentsToolList;
    }

    /**
     * Get the value of defCT.
     *
     * @return value of defCT.
     */
    public ComponentsTool getDefaultCT() {
        return defCT;
    }

    /**
     * Set the value of defCT.
     *
     * @param ct -
     *            Value to assign to default ComponentsTool.
     */
    public void setDefaultCT(ComponentsTool ct) {
        this.defCT = ct;
    }

    static public URL getLocation() {
        return location;
    }

    static public void setLocation(URL url) {
        location = url;
    }

    private CommandMenu createMenu() {
        CommandMenu _menu = new CommandMenu("Netcomponents");

        //_menu.add(new ShowNetComponentsToolCommand(this));
        _menu.add(new SelectNetComponentsToolCommand(this));
        _menu.add(new RemoveNetComponentsToolCommand(this));
        _menu.add(new RemoveLastNetComponentsToolCommand(this));
        _menu.add(new GroupCommand(), KeyEvent.VK_1);
        _menu.add(new UngroupCommand(), KeyEvent.VK_3);
        _menu.addSeparator();
        _menu.add(new VersionInfoCommand(this));
        _menu.addSeparator();

        JMenuItem palettesHeading = _menu.add("Available Palettes");
        palettesHeading.setEnabled(false);
        palettesHeading.setFont(palettesHeading.getFont().deriveFont(Font.BOLD));

        _menu.putClientProperty(MenuManager.ID_PROPERTY, "de.renew.gui.nc");

        return _menu;
    }

    static public ComponentsToolPlugin getCurrent() {
        return (ComponentsToolPlugin) PluginManager.getInstance()
                                                   .getPluginByName("Renew NetComponents");
    }
}