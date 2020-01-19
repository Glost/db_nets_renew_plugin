package CH.ifa.draw;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.FileMenuCreator;
import CH.ifa.draw.application.MenuManager;
import CH.ifa.draw.application.NoGuiAvailableException;
import CH.ifa.draw.application.OpenDrawingCommand;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.ExportHolder;
import CH.ifa.draw.io.ExportHolderImpl;
import CH.ifa.draw.io.ImportHolder;
import CH.ifa.draw.io.ImportHolderImpl;

import CH.ifa.draw.standard.AlignmentMenuCreator;
import CH.ifa.draw.standard.AttributesMenuCreator;
import CH.ifa.draw.standard.EditMenuCreator;
import CH.ifa.draw.standard.NullDrawingEditor;
import CH.ifa.draw.standard.StandardDrawing;

import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.GUIProperties;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.annotations.Provides;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.UIManager;


/**
 * This class represents the Plugin for the JHotDraw Component.
 * It creates the standard menu items provided by that framework.
 * Note that the drawing window itself is not created here yet but by the
 * GuiPlugin. This should be changed, but requires quite massive
 * restructuring.
 *
 * @author Joern Schumacher
 */
public class DrawPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DrawPlugin.class);
    private static DrawApplication _gui;

    /**
     * All import/export work is delegated to these objects.
     **/
    private ImportHolderImpl importHolder;

    /**
     * All import/export work is delegated to these objects.
     **/
    private ExportHolderImpl exportHolder;

    /**
     * recent file list file name
     */
    public static final String RECENTLY_SAVED = "recently-saved.prop";
    private static final String NUM_RECENT_FILES_KEY = "ch.ifa.draw.num-of-recent-files";
    private ArrayList<String> recentFiles = null;
    private CommandMenu recentlySavedMenu = null;
    private long recentFilesModificationDate = 0;
    public static final String WINDOW_FOCUS_WORKAROUND_KEY = "ch.ifa.draw.window-focus-workaround";
    private static final String SHOW_RECENTLY_SAVED_HOTKEYS_KEY = "ch.ifa.draw.show-recentlysaved-hotkeys";
    public static final String CH_IFA_DRAW_GRID_SIZE = "ch.ifa.draw.grid.size";
    public static final String CH_IFA_DRAW_GRID_DEFAULT = "ch.ifa.draw.grid.default";

    // TODO: this is a dirty hack and should be redone
    // so that the instance is controlled by this plugin only 
    /**
     * sets the GUI for this drawPlugin
     * @param gui new GUI
     */
    public static void setGui(DrawApplication gui) {
        logger.debug("IfaPlugin setting gui to " + gui);
        _gui = gui;
    }

    /**
     * Gets the current GUI
     * @return current GUI
     */
    @Provides
    public static DrawApplication getGui() {
        return _gui;
    }

    /**
     * The name of the plugins menu.
     * Extensions of small plugins with functionality not
     * matching any of the other menus can be registered here.
     **/
    public static String PLUGINS_MENU = "Plugins";

    /**
     * The name of the platforms menu
     */
    public static String PLATFORMS_MENU = null;

    /**
     * The name of the file menu.
     * Commands to load, save and print drawings be registered here.
     **/
    public static final String FILE_MENU = "File";

    /**
     * The name of the edit menu.
     * Basic editing commands (selection, cut and paste, etc.) be registered here.
     **/
    public static final String EDIT_MENU = "Edit";

    /**
     * The name of the attributes menu.
     * Commands that modify figure attributes be registered here.
     **/
    public static final String ATTRIBUTES_MENU = "Attributes";

    /**
     * The name of the layout menu.
     * Commands that align or order figures be registered here.
     **/
    public static final String LAYOUT_MENU = "Layout";

    /**
     * Reserved name for the windows menu in the menu bar. This menu is
     * always placed at the end of the menu bar, but before the help menu.
     * <p>
     * This menu is implemented by {@link CH.ifa.draw.application.WindowsMenu}
     * and displays a list of drawing windows (grouped by categories)
     * instead of editing commands. This menu can also be extended
     * by plugins, as long as they do not meddle with the drawing list.
     * </p>
     **/
    public static final String WINDOWS_MENU = "Windows";

    /**
     * The name of the tools menu.
     * Commands that align or order figures be registered here.
     **/
    public static final String TOOLS_MENU = "Tools";

    /**
     * The name of the Paose menu.
     * Commands that are part of the Paose software engineering lifecycle belong here.
     **/
    public static final String PAOSE_MENU = "Paose";

    /**
     * The category to use in the windows menu for tools and dialogs.
     **/
    public static final String WINDOWS_CATEGORY_TOOLS = "Tools";

    /**
     * Reserved name for the help menu in the menu bar. This
     * menu is always placed at the end of the menu bar.
     **/
    public static final String HELP_MENU = "Help";

    /**
     * This prefix is used to identify menu entries belonging to
     * this plugin. It must not be used by other plugins.
     **/
    public static final String MENU_PREFIX = "CH.ifa.draw";
    public static final int[] keys = { KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0 };

    /**
     * @see PluginAdapter
     *
     * @param location  an <code>URL</code> pointing to the <code>jar</code>
     *                  file or directory containing the plugin code and
     *                  configuration files.
     *
     * @throws PluginException
     *   if the instantiation fails. This especially covers the case that
     *   AWT/Swing is not available.
     **/
    public DrawPlugin(URL location) throws PluginException {
        super(location);
        checkSwing();
    }

    /**
     * @see PluginAdapter
     *
     * @param props the plugin configuration.
     * @throws PluginException
     *   if the instantiation fails. This especially covers the case that
     *   AWT/Swing is not available.
     **/
    public DrawPlugin(PluginProperties props) throws PluginException {
        super(props);
        try {
            String tmpPlugins = PluginProperties.getUserProperties()
                                                .getProperty("CH.ifa.draw.menutext.plugin");
            if (tmpPlugins != null) {
                PLUGINS_MENU = tmpPlugins;
            }
            String tmpPlatforms = PluginProperties.getUserProperties()
                                                  .getProperty("CH.ifa.draw.menutext.platforms");
            if (tmpPlatforms != null) {
                PLATFORMS_MENU = tmpPlatforms;
            }
        } catch (Exception e) {
            // nothing to handle
        }
        checkSwing();
    }

    /**
     * Checks whether a graphical user interface can be set up.
     * This is done by calling some static Swing method, thereby
     * triggering the initialisation of the look-and-feel-specific
     * peers. If the initialisation throws any kind of exception
     * or error, this method throws a <code>PluginException</code>.
     *
     * @throws PluginException
     *   if the initialisation of the graphical user interface
     *   failed due to any kind of problem or error.
     **/
    private static void checkSwing() throws PluginException {
        try {
            Toolkit.getDefaultToolkit();
            logger.debug("DrawPlugin: Swing initialization successful.");
        } catch (Throwable e) {
            logger.debug("DrawPlugin: Swing initialization failed: " + e);
            throw new PluginException("Could not initialize Swing, disabling DrawPlugin.",
                                      e);
        }
    }

    public void init() {
        GUIProperties.setProperties(_properties);
        if (GUIProperties.fixMenus()) {
            UIManager.getDefaults()
                     .put("MenuItemUI", "CH.ifa.draw.WorkaroundMenuItemUI");
        }

        // generate Import / Export Holder
        importHolder = new ImportHolderImpl();
        exportHolder = new ExportHolderImpl();

        setRecentlySavedMenu(new CommandMenu("Recently saved"));
        File file = readRecentlySavedFileList();
        recentFilesModificationDate = file.lastModified();

        registerMenuItems(FILE_MENU,
                          (new FileMenuCreator()).createMenus(importHolder,
                                                              exportHolder,
                                                              getRecentlySavedMenu()));
        registerMenuItems(EDIT_MENU, (new EditMenuCreator()).createMenus());
        registerMenuItems(LAYOUT_MENU,
                          (new AlignmentMenuCreator()).createMenus());
        registerMenuItems(ATTRIBUTES_MENU,
                          (new AttributesMenuCreator()).createMenus());
        // the WINDOWS_MENU is automatically included by the menu manager.
    }


    /** Reads the list of files recently saved.
     * The list is stored in ~/.renew/recently-saved.prop. If the
     * the file (or the folder) does not exist, it will be created.
     * The file list is stored as TextFigures in a StandardDrawing.
     * Recently saved files are represented as absolute paths in the
     * TextFigures (as text).
     *
     * @return the files that contains the file list as a renew drawing.
     */
    private File readRecentlySavedFileList() {
        File dir = PluginManager.getPreferencesLocation();
        Drawing drawing = null;
        File file = new File(dir, RECENTLY_SAVED);
        recentFiles = new ArrayList<String>();
        if (file.exists()) {
            drawing = DrawingFileHelper.loadDrawing(file, null);
            putRecentFileNamesInList(drawing);
            updateRecentFileMenu();
        } else {
            drawing = new StandardDrawing();
            DrawingFileHelper.saveDrawing(drawing, file, null);
        }
        return file;
    }

    /** Updates the list of recently saved files. If the file containing
     * the list has been modified (i.e. by another renew instance) the list will
     * be updated first. However, there is no lock on the file, thus concurrently
     * saved files may cause losses in the list. The occurrence of an error is not
     * very probable, since saving involves user interaction.
     *
     * @param filename - the file name of the most recently (just) saved file,
     *                   which is to be added to the list
     */
    public void updateRecentlySavedList(String filename) {
        assert recentFiles != null : "Error: recent file list should not be null.";
        File recentlySavedFiles = new File(PluginManager.getPreferencesLocation(),
                                           RECENTLY_SAVED);

        if (recentlySavedFiles.lastModified() > recentFilesModificationDate) {
            readRecentlySavedFileList();
        }
        putOneFileNameInListOfRecentFiles(filename);
        Drawing recent = new StandardDrawing();
        int offset = 0;
        for (Iterator<String> it = recentFiles.iterator(); it.hasNext();) {
            String name = it.next();
            TextFigure tf = new TextFigure(name);
            recent.add(tf);
            tf.moveBy(0, offset);
            offset += 20;
        }
        DrawingFileHelper.saveDrawing(recent, recentlySavedFiles, null);
        recentFilesModificationDate = recentlySavedFiles.lastModified();
        updateRecentFileMenu();
    }

    /** Puts one file name of a saved file into the list of
     * recently saved files and truncates to the size specified in
     * the property. Property key name is specified in NUM_RECENT_FILES_KEY.
     * @param filename
     */
    private void putOneFileNameInListOfRecentFiles(String filename) {
        if (recentFiles.contains(filename)) {
            recentFiles.remove(filename);
        }
        recentFiles.add(0, filename);
        // get property (truncate length) and truncate list 
        int num = PluginProperties.getUserProperties()
                                  .getIntProperty(NUM_RECENT_FILES_KEY, 10);
        if (recentFiles.size() > num) {
            recentFiles.retainAll(recentFiles.subList(0, num));
        }
    }

    private void putRecentFileNamesInList(Drawing recent) {
        FigureEnumeration figures = recent.figuresReverse();
        while (figures.hasMoreElements()) {
            TextFigure tf = (TextFigure) figures.nextElement();
            recentFiles.add(0, tf.getText());
        }
    }


    /* Updating the menu for the recently saved files on the basis
     * of the recentFiles list.
     */
    private void updateRecentFileMenu() {
        Iterator<String> it = recentFiles.iterator();
        getRecentlySavedMenu().removeAll();
        int i = 0;
        while (it.hasNext()) {
            String recentFileName = it.next();
            if (logger.isDebugEnabled()) {
                logger.debug(DrawPlugin.class.getName() + ": Putting "
                             + recentFileName + " into 'Recently saved' menu.");
            }
            if (!getProperties().getBoolProperty(SHOW_RECENTLY_SAVED_HOTKEYS_KEY)
                        || i > 9) {
                getRecentlySavedMenu()
                    .add(new OpenDrawingCommand(recentFileName,
                                                de.renew.util.StringUtil
                    .getFilename(recentFileName)));
            } else {
                getRecentlySavedMenu()
                    .add(new OpenDrawingCommand(recentFileName,
                                                de.renew.util.StringUtil
                    .getFilename(recentFileName)), keys[i++],
                         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                         + KeyEvent.ALT_DOWN_MASK);
            }
        }
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
            JMenuItem item = it.next();
            if (item.getClientProperty(MenuManager.ID_PROPERTY) == null) {
                item.putClientProperty(MenuManager.ID_PROPERTY,
                                       MENU_PREFIX + "." + menu + "."
                                       + item.getText());
            }
            menuManager.registerMenu(menu, item);
        }
    }

    /**
     * returns the IOHelper
     * @return the current IOHelper
     */
    @Provides
    public IOHelper getIOHelper() {
        return IOHelper.getInstance();
    }

    /**
     * @return the import holder
     */
    @Provides
    public ImportHolder getImportHolder() {
        return importHolder;
    }

    /**
     * @return the export holder
     */
    @Provides
    public ExportHolder getExportHolder() {
        return exportHolder;
    }

    /**
     * creates a new view container for the drawing
     * @param drawing drawing which should be viewed in the new container
     */
    public void newDrawingViewContainer(Drawing drawing) {
        getGui().newDrawingViewContainer(drawing);
    }

    /**
     * @return current DrawPlugin
     */
    public static DrawPlugin getCurrent() {
        Iterator<IPlugin> it = PluginManager.getInstance()
                                            .getPluginsProviding("ch.ifa.draw")
                                            .iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof DrawPlugin) {
                return (DrawPlugin) o;
            }
        }
        return null;
    }

    /**
     * @return the current menu manager
     */
    @Provides
    public MenuManager getMenuManager() {
        return MenuManager.getInstance();
    }

    /**
     * @return the current drawing editor
     */
    @Provides
    public DrawingEditor getDrawingEditor() {
        DrawingEditor result = null;
        DrawApplication gui = getGui();
        if (gui == null) {
            result = NullDrawingEditor.INSTANCE;
        } else {
            result = gui;
        }
        return result;
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
                        DrawApplication gui = getGui();
                        if (gui != null) {
                            gui.showStatus(status);
                        }
                    }
                });
        } else {
            throw new NoGuiAvailableException("DrawPlugin: cannot show status: no gui window.");
        }
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
     * sets the recently saved menu command to the given menu command.
     *
     * @param recentlySavedMenu new CommandMenu
     */
    public void setRecentlySavedMenu(CommandMenu recentlySavedMenu) {
        this.recentlySavedMenu = recentlySavedMenu;
    }

    /**
     * @return the current recently saved menu command
     */
    public CommandMenu getRecentlySavedMenu() {
        return recentlySavedMenu;
    }
}