package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;
import CH.ifa.draw.application.NoGuiAvailableException;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.ExportHolder;
import CH.ifa.draw.io.ImportHolder;
import CH.ifa.draw.io.StatusDisplayer;
import CH.ifa.draw.io.StorableInputDrawingLoader;

import CH.ifa.draw.standard.FigureException;
import CH.ifa.draw.standard.InfoDialog;
import CH.ifa.draw.standard.NullDrawingEditor;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenuItem;
import CH.ifa.draw.util.StorableInput;

import de.renew.application.SimulationEnvironment;
import de.renew.application.SimulatorExtensionAdapter;
import de.renew.application.SimulatorPlugin;

import de.renew.gui.menu.AlignmentMenuExtender;
import de.renew.gui.menu.AttributesMenuExtender;
import de.renew.gui.menu.EditMenuExtender;
import de.renew.gui.menu.HelpMenuCreator;

import de.renew.io.exportFormats.SNSExportFormat;
import de.renew.io.exportFormats.WoflanExportFormat;
import de.renew.io.exportFormats.XMLExportFormat;
import de.renew.io.importFormats.SNSImportFormat;
import de.renew.io.importFormats.XMLImportFormat;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import de.renew.remote.NetInstanceAccessor;

import de.renew.shadow.SyntaxException;

import java.awt.EventQueue;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.net.URL;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;


/**
 * Provides a graphical user interface (gui) for Renew.
 * Features editing nets and other drawings based on JHotDraw (CH plugin).
 * Net drawings can be prepared for simulation, and the
 * simulation process (Simulator plugin) can be controlled.
 * <p>
 * This class provides methods control the gui.
 * It comprises the basic features to open and close the gui (the
 * gui does not start automatically when the plugin is loaded
 * unless the property <code>de.renew.gui.autostart</code> is set).
 * While the gui is running, drawings can be opened within the gui.
 * </p>
 * <p>
 * Additional palettes, figure creators and import/export
 * filters can be registered. The registration can be done
 * while the gui is running or not. See the interface
 * descriptions of {@link PaletteHolder},
 * {@link FigureCreatorHolder}, {@link ImportHolder} and
 * {@link ExportHolder} for details.
 * <b>Note:</b> Menus are managed by {@link DrawPlugin}
 * </p>
 * <p>
 * Unless stated otherwise, all methods in this class will synchronise
 * their execution with the AWT event queue to avoid concurrency
 * problems. The synchronisation step is left out when the calling thread
 * in fact is the AWT event thread itself.
 * </p>
 *
 * @author Joern Schumacher
 * @author Michael Duvigneau
 **/
public class GuiPlugin extends PluginAdapter implements StatusDisplayer {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(GuiPlugin.class);
    public static final String MENU_PREFIX = "de.renew.gui";
    public static final String SIMULATION_MENU = "Simulation";
    public static final String NET_MENU = "Net";
    private SyntaxExceptionFrame syntaxFrame = null;

    /**
     * All menu registration work is delegated to this object.
     **/
    private Collection<String> _menus = new Vector<String>();


    /**
     * The delegator Object for PaletteHolder calls.
     *
     * @author J&ouml;rn Schumacher
     */
    private GuiPalettes paletteManager = new GuiPalettes(this);

    /**
     * References the started gui so we can control it.
     * A <code>null</code> reference indicates that no gui is running.
     * Access to this variable has to be protected by synchronisation with
     * the GuiPlugin instance.
     **/
    private CPNApplication _gui;

    /**
     * The set of registered figure creators.
     **/
    private FigureCreatorComposition _figureCreator = new FigureCreatorComposition();

    /**
     * A listener for simulation initialisation and termination
     * events. Informs the gui about the state change.
     **/
    private SimulationStateListener _simulationListener = null;

    /**
     * Set with TabControllers used to configure a simulation.
    * Contains all {@link ConfigureSimulationTabController}
    * objects belonging to the simulation configuration dialog.
    * The order in this list matches the order of tabs in the dialog.
    **/
    private List<ConfigureSimulationTabController> _simConfigTabControllers = new Vector<ConfigureSimulationTabController>();

    /**
     * The property name {@value}. Controls whether the gui should shut
     * down the whole plugin system when it closes.
     **/
    public static final String SHUTDOWN_PROP_NAME = "de.renew.gui.shutdownOnClose";

    /**
     * The property name {@value}. Controls whether the gui should be
     * opened automatically when the GuiPlugin is loaded.
     **/
    public static final String AUTOSTART_PROP_NAME = "de.renew.gui.autostart";
    private BreakpointManager bpManager = null;

    // ---------------------------------------------------- Initialisation


    /**
     * Instantiates the plugin (and nothing more).
     * @see PluginAdapter#PluginAdapter(URL)
     **/
    public GuiPlugin(URL location) throws PluginException {
        super(location);
    }

    /**
     * Instantiates the plugin (and nothing more).
     * @see PluginAdapter#PluginAdapter(PluginProperties)
     **/


    //NOTICEthrows
    public GuiPlugin(PluginProperties props) throws PluginException {
        super(props);
    }

    /**
     * Initializes the plugin.
     * Registers commands and connections to underlying plugins.
     * Initialises all extension registration lists.
     * If the autostart property is set, the gui is started.
     **/
    public void init() {
        logger.debug("initializing GUI plugin.");
        patchStorableInput();
        _simulationListener = new SimulationStateListener();


        // create the objects that extend the existing menus
        Collection<JMenuItem> alignmentMenus = (new AlignmentMenuExtender())
                                                   .createMenus();
        Collection<JMenuItem> attributeMenus = (new AttributesMenuExtender())
                                                   .createMenus();
        Collection<JMenuItem> editMenus = (new EditMenuExtender()).createMenus();


        // create the objects that add new menus
        Collection<JMenuItem> simMenus = (new SimulationMenuCreator())
                                             .createMenus(getBreakpointManager());
        Collection<JMenuItem> netMenus = (new NetMenuCreator()).createMenus(getBreakpointManager());
        Collection<JMenuItem> helpMenus = (new HelpMenuCreator()).createMenus();

        registerMenuItems(NET_MENU, netMenus);
        registerMenuItems(SIMULATION_MENU, simMenus);
        // this is really strange: the help menu is only added in the gui plugin;
        // however, the DrawPlugin knows the help menus title so it can treat it separately
        // (it puts it at the end of the menu bar). 
        registerMenuItems(DrawPlugin.HELP_MENU, helpMenus);
        registerMenuItems(DrawPlugin.LAYOUT_MENU, alignmentMenus);
        registerMenuItems(DrawPlugin.ATTRIBUTES_MENU, attributeMenus);
        registerMenuItems(DrawPlugin.EDIT_MENU, editMenus);

        registerCreators();

        SimulatorPlugin.getCurrent().addExtension(_simulationListener);

        // init Export / Import
        initDefaultImportFormats();
        initDefaultExportFormats();

        PluginManager manager = PluginManager.getInstance();
        manager.addCLCommand("gui", new StartGuiCommand(this));
        manager.addCLCommand("demonstrator", new Demonstrator());

        if (getProperties().getBoolProperty(AUTOSTART_PROP_NAME)) {
            // TODO: Instead of System.out we should use a print stream
            //       that maps to the logger.
            ((StartGuiCommand) manager.getCLCommands().get("gui")).execute(new String[] {  },
                                                                           System.out);
        }

        // add standard TabControllers for simulation configuration to set of
        // configuration tab controllers.
        _simConfigTabControllers.add(new ConfigureEngineController());
        _simConfigTabControllers.add(new ConfigureRemoteAccessController());
        _simConfigTabControllers.add(new ConfigureNetpathController());
    }

    /**
     *
     */
    static public void patchStorableInput() {
        // moved this block from CPNApplication to assure 
        // patching input files without gui
        // this is needed to ensure backwards compatibility for old rnw files
        // (or other drawing files)
        DrawingFileHelper.setStorableInputDrawingLoader(new StorableInputDrawingLoader() {
                protected StorableInput makeStorableInput(URL location,
                                                          boolean useUFT)
                        throws IOException {
                    return new PatchingStorableInput(location, useUFT);
                }
            });
    }

    /*
     * Convenience method that registers the JMenuItems contained
     * in the given collection under the given parent menu.
     * Its primary function is to properly calculate the id value of the items.
     */
    private void registerMenuItems(String menu, Collection<JMenuItem> items) {
        Iterator<JMenuItem> it = items.iterator();
        MenuManager menuManager = MenuManager.getInstance();
        while (it.hasNext()) {
            Object o = it.next(); //FIXME it.next is alsways JMenuItem...
            JMenuItem item = null;
            if (o instanceof JMenuItem) {
                item = (JMenuItem) o;
            } else if (o instanceof Command) {
                item = new CommandMenuItem((Command) o);
            }

            //NOTICEnull
            if (item != null) {
                String id = MENU_PREFIX + "." + item.getText();
                _menus.add(id);
                menuManager.registerMenu(menu, item, id);
            } else {
                logger.warn("GuiPlugin.registerMenuItems: Could not register "
                            + menu + ". No parent menu found.");
            }
        }
    }

    public synchronized boolean cleanup() {
        Iterator<String> toUnregister = _menus.iterator();
        while (toUnregister.hasNext()) {
            DrawPlugin.getCurrent().getMenuManager()
                      .unregisterMenu(toUnregister.next());
        }

        if (isGuiPresent()) {
            doSynchronized(new Runnable() {
                    public void run() {
                        _gui.exit();
                    }
                });
        }
        Demonstrator.cleanup();
        if (_simulationListener != null) {
            SimulatorPlugin.getCurrent().removeExtension(_simulationListener);
            _simulationListener = null;
        }

        PluginManager.getInstance().removeCLCommand("gui");
        PluginManager.getInstance().removeCLCommand("demonstrator");
        return true;
    }

    public synchronized boolean canShutDown() {
        if (isGuiPresent()) {
            class GuiQuery implements Runnable {
                public boolean exitOK = false;

                public void run() {
                    exitOK = _gui.canClose();
                }
            }

            GuiQuery guiQuery = new GuiQuery();
            logger.debug("asking gui to allow close operation");
            doSynchronized(guiQuery);
            return guiQuery.exitOK;
        }
        return true;
    }

    // ---------------------------------------------------- Helper methods


    /**
     * Executes the given runnable in sync with the AWT event queue and
     * waits until the operation has completed. If the thread is
     * interrupted or the operation throws an exception (both situations
     * are rather unexpected), a <code>RuntimeException</code> is thrown.
     *
     * @param runnable   the <code>Runnable</code> to execute.
     **/
    private static void doSynchronized(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                throw new RuntimeException("GuiPlugin: synchronised operation has been interrupted.",
                                           e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("GuiPlugin: synchronised operation has failed.",
                                           e.getTargetException());
            }
        }
    }


    /**
     * Convenience Method for getting the gui starter object
     * presently registered in the PluginManager
     */
    public static GuiPlugin getCurrent() {
        // Iterator it = PluginManager.getInstance().getPlugins().iterator();
        Iterator<IPlugin> it = PluginManager.getInstance()
                                            .getPluginsProviding("de.renew.gui")
                                            .iterator();
        while (it.hasNext()) {
            IPlugin o = it.next();
            if (o instanceof GuiPlugin) {
                return (GuiPlugin) o;
            }
        }
        return null;
    }

    /**
     * Returns all registered {@link ConfigureSimulationTabController} to configure
     * a simulation.
     *
     * @return an Array with the registered ConfigureSimulationTabController
     */
    public ConfigureSimulationTabController[] getConfigTabController() {
        return this._simConfigTabControllers.toArray(new ConfigureSimulationTabController[] {  });
    }

    /**
     * Adds a new {@link ConfigureSimulationTabController} to the list of registered
     * ConfigurationTabControllers.
     *
     * @param tabController the ConfigureSimulationTabController to add
     */
    public void addConfigTabController(ConfigureSimulationTabController tabController) {
        this._simConfigTabControllers.add(tabController);
    }

    /**
     * Removes a registered {@link ConfigureSimulationTabController} from the list of registered
     * ConfigurationTabControllers.
     *
     * @param tabController the ConfigureSimulationTabController to remove
     */
    public void removeConfigTabController(ConfigureSimulationTabController tabController) {
        this._simConfigTabControllers.remove(tabController);
    }

    // --------------------------------------- Opening and closing the gui


    /**
     * Creates the application window.
     * If the gui is already running, nothing happens.
     **/
    public synchronized void openGui() {
        if (!isGuiPresent()) {
            final GuiPlugin pluginInstance = this;
            doSynchronized(new Runnable() {
                    public void run() {
                        logger.debug("GuiPlugin: Starting gui. Classloader is "
                                     + getClass().getClassLoader());
                        new CPNApplication(pluginInstance);
                    }
                });
        }
        paletteManager.notifyGuiOpen();
        assert isGuiPresent() : "Gui should run after opening.";
    }

    /**
     * Called by CPNApplication after it has been created.
     * No one else may call this method and at no other time.
     *
     * @param c the application instance.
     **/
    void notifyGuiStart(CPNApplication c) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called in the AWT event thread.";
        assert !isGuiPresent() : "Must be called exactly once.";
        assert c != null : "Must be called by the gui instance.";
        registerExitBlock();
        _gui = c;
        // menuHolder.addRegisteredMenus();
    }

    /**
     * Called by CPNApplication to query wether it should shut
     * down the whole plugin system or just terminate itself.
     *
     * @return <code>true</code> if a shutdown of the whole
     *         plugin system is requested.
     **/
    boolean isShutdownOnClose() {
        return getProperties().getBoolProperty(GuiPlugin.SHUTDOWN_PROP_NAME);
    }

    /**
     * Called by CPNApplication to shut down the plugin system.
     **/
    void doShutdownOnClose() {
        PluginManager.getInstance().stop();
    }

    /**
     * Called by CPNApplication after it has terminated.
     * No one else may call this method and at no other time.
     *
     * @param c the application instance.
     **/
    void notifyGuiClosed(CPNApplication c) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called in the AWT event thread.";
        assert _gui == c : "Must be called by the current gui instance";
        _gui = null;
        registerExitOk();
        ModeReplacement.killInstance();
        // menuHolder.guiClosed();
    }

    /**
     * Attempts to close the gui. This in effect has the same behaviour as
     * if the user would click on the window close button. The operation
     * may be cancelled by the user under certain circumstances.
     **/
    public synchronized void closeGui() {
        if (isGuiPresent()) {
            doSynchronized(new Runnable() {
                    public void run() {
                        _gui.requestClose();
                    }
                });
        }
        bpManager = null;
    }

    // ------------------------------------------------------- Gui queries


    /**
     * Gives a reference to the running gui.
     * <p>
     * <strong>Caution:</strong>
     * This reference may be <code>null</code> when no gui is running.
     * Furthermore, all calls to methods of the gui object have to be
     * synchronised with the AWT event queue.
     * </p>
     *
     * @return the current gui instance.
     **/
    public CPNApplication getGui() {
        return _gui;
    }

    /**
     * Returns a drawing editor so clients can create ch.ifa.draw elements
     * that need it.
     *
     * @return The currently open main window if one is open,
     * a NullDrawingEditor otherwise.
     */
    public DrawingEditor getDrawingEditor() {
        DrawingEditor result = null;
        CPNApplication gui = getCurrentGui();
        if (gui == null) {
            result = NullDrawingEditor.INSTANCE;
        } else {
            result = gui;
        }
        return result;
    }

    /**
     * Checks whether the gui window is open.
     * Use this test to avoid NoGuiAvailableExceptions in methods
     * that depend on the Frame to be open.
     *
     * @return True if the window is active, false otherwise.
     */
    public boolean isGuiPresent() {
        return _gui != null;
    }

    /**
     * Returns a Frame object of a currently running GUI.
     * This is for cases in which a plugin wants to show
     * a Dialog; in this case he needs a Frame in the constructor.
     *
     * It is NOT supposed to be used for closing the frame,
     * finding any contained components or whatever.
     * Just for showing Dialogs.S
     */
    public JFrame getGuiFrame() {
        CPNApplication site = getGui();
        if (site != null) {
            return site.getFrame();
        } else {
            logger.error("GuiPlugin: no gui to get frame!");
        }
        return null;
    }

    private static CPNApplication getCurrentGui() {
        GuiPlugin current = getCurrent();
        if (current == null) {
            logger.error("GuiPlugin: no GuiPlugin available.");
            return null;
        }
        return current._gui;
    }


    // ------------------------------------------------- Menu registration
    //                            (returns MenuHolder delegation object)
    //    public MenuHolder getMenuHolder() {
    //        return menuHolder;
    //    }
    // ------------------------------------------------- Menu registration
    //                            (returns PaletteHolder delegation object)
    public PaletteHolder getPaletteHolder() {
        return paletteManager;
    }


    // -------------------------------------------- Gui operations and I/O
    // has been removed: most methods have not been called.


    /**
     * Opens the given drawing in the editor.
     *
     * @throws NoGuiAvailableException if no Gui window is open.
     * @deprecated This method should never have existed.
     */
    public Drawing openDrawing(Drawing drawing) {
        if (isGuiPresent()) {
            return _gui.openDrawing(drawing);
        }
        throw new NoGuiAvailableException("Cannot create new drawing: no gui window.");
    }

    /**
     * Opens a window displaying the given net instance.
     *
     * @param net  the net instance to show in the window.
     * @throws NoGuiAvailableException  if the Gui window is not open.
     * @throws NullPointerException  if <code>net</code> is null.
     **/
    public synchronized void openInstanceDrawing(final NetInstanceAccessor net)
            throws NoGuiAvailableException {
        if (net == null) {
            throw new NullPointerException("Net instance may not be null.");
        }
        if (isGuiPresent()) {
            doSynchronized(new Runnable() {
                    public void run() {
                        getGui().openInstanceDrawing(net);
                    }
                });
        } else {
            throw new NoGuiAvailableException("GuiPlugin: no gui to open net instance!");
        }
    }

    /**
     * Displays a message in the gui status line.
     *
     * @param status  the message to show.
     * @throws NoGuiAvailableException  if the Gui window is not open.
     * @throws NullPointerException  if <code>net</code> is null.
     **/
    public void showStatus(final String status) throws NoGuiAvailableException {
        if (isGuiPresent()) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        CPNApplication gui = getCurrentGui();
                        if (gui != null) {
                            gui.showStatus(status);
                        }
                    }
                });
        } else {
            throw new NoGuiAvailableException("GuiPlugin: cannot show status: no gui window.");
        }
    }


    /**
     * Creates an about box dialog.
     * If the editor window is open, this window is used as parent of the dialog.
     *
     * @return an <code>InfoDialog</code> value
     **/
    public InfoDialog createAboutBox() {
        JFrame owner = null;
        if (isGuiPresent()) {
            owner = getGui().getFrame();
        }
        PluginProperties guiProperties = getCurrent().getProperties();

        String title = getAboutProperty(guiProperties, "title");
        String version = getAboutProperty(guiProperties, "version");
        String content = getAboutProperty(guiProperties, "content");
        String revisionTmp = getAboutProperty(guiProperties, "revision");
        String addRenewLinkTmp = getAboutProperty(guiProperties, "enableLinks");

        int revision = 0;
        if (revisionTmp != null) {
            try {
                revision = Integer.parseInt(revisionTmp);
            } catch (NumberFormatException e) {
                logger.warn("Revision has wrong format: " + revisionTmp, e);
            }
        }
        String message = version + content
                         + (revision > 0 ? "\nRevision" + revision : "");

        boolean addRenewLink = false;
        if (addRenewLinkTmp != null) {
            addRenewLink = Boolean.valueOf(addRenewLinkTmp).booleanValue();
        }
        return new InfoDialog(owner, title, message, addRenewLink);
    }

    /**
     * Get help contents by key from file renew.properties (if specified),<br>
     * else use the value as defined in plugin.cfg of the gui plugin.
     *
     * @param properties [{@link PluginProperties}]
     * @param key [String]
     *
     * @author Eva Mueller
     * @since 2013-10-21
     *
     * @return
     */
    private String getAboutProperty(PluginProperties properties, String key) {
        return properties.getProperty("de.renew.help." + key,
                                      properties.getProperty("de.renew.help.gui."
                                                             + key));
    }

    /**
     * Brings the menu frame to front (if the frame is open).
     **/
    public void bringMenuFrameToFront() {
        if (isGuiPresent()) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        final JFrame owner = getGui().getFrame();
                        if (owner != null) {
                            owner.setVisible(true);
                            owner.toFront();
                        }
                    }
                });
        }
    }

    /**
     * Returns the window containing the currently open drawing.
     *
     * @param drawing whose view should be returned.
     * @return the window containing the drawing.
     * @throws NoGuiAvailableException if the Gui Window is not open.
     */
    public DrawingView getView(Drawing drawing) {
        if (!isGuiPresent()) {
            throw new NoGuiAvailableException("GuiPlugin: no view for "
                                              + drawing
                                              + ", no gui window open");
        }
        return _gui.getView(drawing);
    }

    /**
     * Handle the given SyntaxException.
     * If the gui window is not open, a stack trace of the exception is printed.
     */
    public void handleSyntaxException(SyntaxException e) {
        if (isGuiPresent()) {
            logger.debug(e.getMessage(), e);
            processSyntaxException(FigureExceptionFactory.createFigureException(e),
                                   true);
        } else {
            logger.error(e.getMessage(), e);
        }
    }

    void processFigureException(FigureException e, boolean displayImmediately) {
        if (syntaxFrame == null) {
            CPNApplication app = getGui();
            if (app != null) {
                syntaxFrame = new SyntaxExceptionFrame(app);
            }
        }
        if (syntaxFrame != null) {
            syntaxFrame.displayException(e, displayImmediately);
        } else {
            logger.error(e.getMessage());
        }
    }

    public void processSyntaxException(FigureException e,
                                       boolean displayImmediately) {
        processFigureException(e, displayImmediately);


        // Print the error message.
        showStatus("A syntax error occurred.");
    }

    public void closeSyntaxErrorFrame() {
        if (syntaxFrame != null) {
            syntaxFrame.dispose();
            syntaxFrame = null;
        }
    }


    // --------------------------------------------------- Figure creators


    /**
     * Registers all preconfigured figure creators.
     **/
    private void registerCreators() {
        safeRegisterFigureCreator("de.renew.gui.Token", new TokenFigureCreator());
        safeRegisterFigureCreator("de.renew.unify.Aggregate",
                                  new AggregateFigureCreator());
        safeRegisterFigureCreator("de.renew.remote.NetInstanceAccessor",
                                  new NetInstanceFigureCreator());
        safeRegisterFigureCreator("CH.ifa.draw.framework.Figure",
                                  new LocalFigureFigureCreator());
    }

    private void safeRegisterFigureCreator(String className,
                                           FigureCreator register) {
        try {
            Class.forName(className, true,
                          PluginManager.getInstance().getBottomClassLoader());
            _figureCreator.registerCreator(register);
        } catch (ClassNotFoundException e) {
            logger.error("could not register FigureCreator: " + e);
        }
    }

    public FigureCreatorHolder getFigureCreatorHolder() {
        return _figureCreator;
    }

    public TextFigureCreator getTextFigureCreator() {
        return _figureCreator;
    }

    public FigureCreator getFigureCreator() {
        return _figureCreator;
    }


    // Import Export --------------------------------------------------
    private void initDefaultImportFormats() {
        ImportHolder importHolder = DrawPlugin.getCurrent().getImportHolder();
        importHolder.addImportFormat(new SNSImportFormat());
        importHolder.addImportFormat(new XMLImportFormat());

    }

    private void initDefaultExportFormats() {
        ExportHolder exportHolder = DrawPlugin.getCurrent().getExportHolder();
        exportHolder.addExportFormat(new XMLExportFormat());
        exportHolder.addExportFormat(new SNSExportFormat());
        exportHolder.addExportFormat(new WoflanExportFormat());
    }

    public BreakpointManager getBreakpointManager() {
        if (bpManager == null) {
            // what happens if there is no gui?
            // the bpm will not be requested without gui, will it?
            // 
            // just for the record: yes it IS!!! ARGHHGHH! Ah, whatever...
            bpManager = new BreakpointManager(ModeReplacement.getInstance()
                                                             .getSimulation());
            ModeReplacement.getInstance().getSimulation()
                           .setBreakpointManager(bpManager);
        }
        return bpManager;
    }

    /**
    * Listens as simulator extension for simulation start and
    * termination events. Updates the menu whenever such an
    * event happens.
    * @author Michael Duvigneau
    **/
    private class SimulationStateListener extends SimulatorExtensionAdapter {
        public void simulationSetup(SimulationEnvironment env) {
            enforceMenuUpdate(false);
        }

        public void simulationTerminated() {
            enforceMenuUpdate(true);
        }

        private void enforceMenuUpdate(final boolean closeSimulationWindows) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        if (isGuiPresent()) {
                            _gui.menuStateChanged();
                            if (closeSimulationWindows) {
                                _gui.cleanupSimulationWindows();
                            }
                        }
                    }
                });
        }
    }


    /** Provides a Renew logo icon
     * @return a renew logo icon
     */
    public static ImageIcon getRenewIcon() {
        return new ImageIcon(getCurrent().getClass()
                                 .getResource(CPNApplication.CPNIMAGES
                                              + "renew.png"));
    }
}