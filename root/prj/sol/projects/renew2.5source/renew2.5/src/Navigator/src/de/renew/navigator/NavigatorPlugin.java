package de.renew.navigator;

import CH.ifa.draw.DrawPlugin;
import CH.ifa.draw.IOHelper;

import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.io.ImportHolder;

import CH.ifa.draw.util.CommandMenuItem;

import de.renew.navigator.gui.NavigatorGuiProxy;
import de.renew.navigator.io.FileFilterBuilder;
import de.renew.navigator.io.FileFilterBuilderImpl;
import de.renew.navigator.models.NavigatorFileTree;

import de.renew.plugin.PluginManager;
import de.renew.plugin.annotations.Inject;
import de.renew.plugin.annotations.Provides;
import de.renew.plugin.di.DIPlugin;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JTree;


/**
 * This is the main class of the Navigator plug-in.
 * It is initializing and registering two CommandMenuItems to the Renew-GUI.
 * The initialization of the NavigatorGUI will be done when one of these commands gets executed.
 *
 * @author Konstantin Moellers (1kmoelle), Hannes Ahrens (4ahrens)
 * @version September 2015
 */
final public class NavigatorPlugin extends DIPlugin implements NavigatorExtender {
    private final MenuManager menuManager;
    private JMenuItem openNavigatorItem;
    private static final String OPEN_NAVIGATOR_COMMAND_MENU_ID = "de.renew.navigator.show";
    private final NavigatorFileTree model;
    private final AutosaveController autosave;
    private final FilesystemController filesystem;
    private final NavigatorGui gui;

    @Inject
    public NavigatorPlugin(MenuManager menuManager, ImportHolder importHolder,
                           IOHelper ioHelper) {
        // Create the navigator model.
        model = new NavigatorFileTree();

        // Init Menu.
        this.menuManager = menuManager;
        initMenu();

        // Create a FileFilterBuilder for filesystem ops.
        FileFilterBuilder builder;
        builder = new FileFilterBuilderImpl(ioHelper, importHolder);

        // Create controllers.
        autosave = new AutosaveController(this);
        filesystem = new FilesystemController(this, builder);

        // Initialize the GUI proxy.
        gui = new NavigatorGuiProxy(filesystem, this);
    }

    @Provides
    public NavigatorFileTree getModel() {
        return model;
    }

    @Provides
    public NavigatorGui getGui() {
        return gui;
    }

    /**
     * This method overwrites the virtual PluginAdapter.initGui() method.
     * Two new commands get initialized and registered to the Renew-GUI:
     * - OpenNavigatorCommand: opening the Navigator
     * - OpenInNavigatorCommand: opening the Navigator and a JFileChooser to open files
     * The OpenNavigatorCommand gets registered to the plug-in menu, similar to the MulanViewer with
     * the shortcut ctrl+shift+n.
     * The OpenInNavigatorCommand gets registered to the file menu, with the shortcut ctrl+shift+o
     * to open files.
     */
    @Override
    public void init() {
        PluginManager pluginManager = PluginManager.getInstance();
        if (pluginManager != null) {
            pluginManager.addCLCommand(OpenNavigatorCommand.CMD,
                                       new OpenNavigatorCommand(this));
        }
        // Create controllers
        initControllers();
    }

    private void initMenu() {
        openNavigatorItem = new CommandMenuItem(new OpenNavigatorCommand(this),
                                                KeyEvent.VK_N,
                                                KeyEvent.SHIFT_DOWN_MASK
                                                | Toolkit.getDefaultToolkit()
                                                         .getMenuShortcutKeyMask());
        openNavigatorItem.putClientProperty(MenuManager.ID_PROPERTY,
                                            OPEN_NAVIGATOR_COMMAND_MENU_ID);
        menuManager.registerMenu(DrawPlugin.FILE_MENU, openNavigatorItem, 2, 0);
    }

    /**
     * Deregisters the navigator plugin
     *
     * @see de.renew.plugin.IPlugin#cleanup
     **/
    @Override
    public boolean cleanup() {
        gui.closeWindow();

        if (openNavigatorItem != null) {
            menuManager.unregisterMenu(openNavigatorItem);
            openNavigatorItem = null;
        }
        PluginManager pluginManager = PluginManager.getInstance();
        if (pluginManager != null) {
            pluginManager.removeCLCommand(OpenNavigatorCommand.CMD);
        }

        return true;
    }

    @Override
    public void registerExtension(NavigatorExtension extension) {
        gui.addExtension(extension);
    }

    @Override
    public boolean deregisterExtension(NavigatorExtension extension) {
        return gui.removeExtension(extension);
    }

    @Override
    public JTree getTree() {
        return gui.getTree();
    }

    /**
     * The openNavigator() method assures the NavigatorGUI is created, initialized, visible and in
     * the front.
     */
    public void openNavigator() {
        // Load model from autosave.
        if (autosave.isAutosaveFileExisting()) {
            autosave.loadModel();
        }

        // Open the GUI window.
        gui.openWindow();
    }

    /**
     * Initializes controllers and let them observe the model.
     */
    protected void initControllers() {
        model.addObserver(autosave);
        model.addObserver(filesystem);
    }

    /**
     * This method first calls openNavigator() to assure the navigator is open and initialized.
     * Afterwards it calls the navigator-GUI to open a JFileChooser for adding files and folders.
     *
     * @deprecated Use {@link #openNavigator} directly
     */
    public void openInNavigator() {
        openNavigator();
    }

    /**
     * @return the current plugin instance
     * @deprecated Please let the Plugin be injected.
     */
    @Deprecated
    public static NavigatorPlugin getCurrent() {
        return (NavigatorPlugin) PluginManager.getInstance()
                                              .getPluginByName("Navigator");
    }
}