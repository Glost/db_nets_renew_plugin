/**
 * @author Joern Schumacher, Eva Mueller
 * @created Feb 23, 2010
 * @modified Nov 14, 2010
 * @version 0.1
 */
package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.CommandMenuItem;

import java.awt.Component;
import java.awt.Font;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * This class manages the menus of the JHotDraw draw window.<br>
 * If a plugin wants to add a menu item, it uses the registerMenu() method.
 * A unique id needs to be calculated by the registering plugin to identify the registered item.
 * From there, two possibilities exist:
 * <ul>
 * <li>the plugin registers the item with the registerMenuItem(String, JMenuItem, String id)
 * method.</li>
 * <li>the plugin first calls setClientProperty(MenuManager.ID_PROPERTY, id) on the item
 * and then uses the registerMenuItem(String, JMenuItem) method to register it.</li>
 * <p>
 * Top level menus are identified by their label; if a menu item is added to
 * a top level menu that does not exist yet, it is created.
 * Separator items need not be unregistered: they are removed automatically if they appear
 * on the top or bottom of a menu if a item is removed.
 *
 * @author Joern Schumacher
 * @author Michael Duvigneau
 */
public class MenuManager {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(MenuManager.class);
    public static final String ID_PROPERTY = "ch.ifa.draw.menu.id";
    private static MenuManager instance;

    /**
     * Constants for menu positioning in sections.
     */
    public static final int AT_BEGIN_SECTION = 0;
    public static final int AT_END_SECTION = 1;
    public static final int SECTION_MIDDLE = 2;

    /**
     * A map from parent menu names to their SeparatorFactory.<br>
     * Consists of pairs <code>(String, SeparatorFactory)</code>.
     */
    private HashMap<String, SeparatorFactory> separatorFactories;

    /**
     * A map from top level menu names to their menu objects.
     * Consists of pairs <code>(String, JMenu)</code>.
     **/
    private HashMap<String, JMenu> toplevelMenus;

    /**
     * A map from identification strings to their associated menu items.
     * Consists of pairs <code>(String, JMenuItem)</code>.
     **/
    private HashMap<String, JMenuItem> registeredMenus;

    /**
     * A list of top level menu names, representing the order of registered
     * top level menus.
     **/
    private List<String> toplevelOrder = new Vector<String>();

    /**
     * The currently running application.  If <code>null</code>, no
     * application is running.
     **/
    private DrawApplication application = null;

    /**
     * Indicates that the top level menu with the name
     * {@link DrawPlugin#WINDOWS_MENU} has been registered.
     * This menu has a special position at the end of the menu bar,
     * therefore all other menus (except the <code>Help</code> menu)
     * must be inserted in front of it.
     **/
    private boolean windowsMenuPresent = false;

    /**
     * Indicates that the top level menu with the name
     * {@link DrawPlugin#HELP_MENU} has been registered.
     * This menu has a special position at the end of the menu bar,
     * therefore all other menus must be inserted in front of it.
     **/
    private boolean helpMenuPresent = false;

    /**
     * A temporary map from menu ids to their menu items.<br>
     * Consists of pairs <code>(String, JMenuItem)</code>.
     */
    private HashMap<String, JMenuItem> tmpRegisteredMenuItems;

    private MenuManager() {
        toplevelMenus = new HashMap<String, JMenu>();
        registeredMenus = new HashMap<String, JMenuItem>();

        // The windows menu is always present.
        // Because the application is not known yet, we just add the menu
        // to our preconfiguration tables.
        WindowsMenu winMenu = new WindowsMenu(DrawPlugin.WINDOWS_MENU);
        toplevelMenus.put(DrawPlugin.WINDOWS_MENU, winMenu);
        toplevelOrder.add(DrawPlugin.WINDOWS_MENU);
        windowsMenuPresent = true;
    }

    /**
     * Singleton access method
     * @return The MenuManager instance
     */
    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }

    /**
     *
     * Register the given menu below the top level menu identified by the
     * parentMenu parameter.
     * IMPORTANT: The registered menu must have the client property
     * MenuManager.ID_PROPERTY set to a sensible ID (String).
     * (This is done via the setClientProperty() method).
     *
     * If you just want to add one menu item, we recommend using the
     * registerMenu(String parent, JMenuItem item, String id) below
     * since it set the client property itself and therefore is less error prone.
     *
     * @param parentMenu the top level menu in which to show the menu item.
     *                         If this menu does not yet exist, it will be created.
     * @param toRegister the menu item to add.
     */
    public synchronized void registerMenu(String parentMenu,
                                          JMenuItem toRegister) {
        // check if the parentMenu exists, else create it
        final JMenu parent = getOrCreateParentMenu(parentMenu);
        String id = (String) toRegister.getClientProperty(ID_PROPERTY);
        if (id == null) {
            logger.error(getClass() + ": could not register menu " + toRegister
                         + ": please set " + ID_PROPERTY
                         + " with menu.setClientProperty().");
            return;
        }


        if (toRegister instanceof SeparatorItem) {
            // no need to unregister separators: they are removed automatically
            registeredMenus.put(id, toRegister);
            parent.addSeparator();

        } else {
            registeredMenus.put(id, toRegister);
            parent.add(toRegister);
        }
        logger.debug(getClass() + ": the menu " + toRegister.getText()
                     + " has been registered under " + parentMenu + ", id "
                     + toRegister.getClientProperty(ID_PROPERTY));
        if (tmpRegisteredMenuItems == null) {
            tmpRegisteredMenuItems = new HashMap<String, JMenuItem>();
        }
        tmpRegisteredMenuItems.put(id, toRegister);
    }

    /**
     * Register given <b>separator</b> below the menu identified by the given <b>parentMenu</b> name.<br>
     * <span style="color: red;">Note</span> :  The given <b>separator</b> must have the client property
     * <b>MenuManager.ID_PROPERTY</b><br> set to a unique ID (String).<br>
     * (This is done via the setClientProperty() method).
     * @param parentMenu [String] Name of the parent menu
     * @param separator [{@link JPopupMenu.Separator}] Separator to be registered
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public synchronized void registerMenu(String parentMenu,
                                          JPopupMenu.Separator separator) {
        final JMenu parent = getOrCreateParentMenu(parentMenu);
        String id = (String) separator.getClientProperty(ID_PROPERTY);
        if (id == null) {
            logger.error(getClass() + ": could not register separator "
                         + separator + ": please set " + ID_PROPERTY
                         + " with menu.setClientProperty().");
            return;
        }

        parent.addSeparator();
    }

    /**
    *
    * Register the given menu below the top level menu identified by the
    * parentMenu parameter.
    * IMPORTANT: The registered menu must have the client property
    * MenuManager.ID_PROPERTY set to a sensible ID (String).
    * (This is done via the setClientProperty() method).
    *
    * If you just want to add one menu item, we recommend using the
    * registerMenu(String parent, JMenuItem item, String id) below
    * since it set the client property itself and therefore is less error prone.
    *
    * @param parentMenu the top level menu in which to show the menu item.
    *                         If this menu does not yet exist, it will be created.
    * @param toRegister the menu item to add.
    * @param separator [int] add separator policy [_NO_SEPARATOR, _SEPARATOR_ABOVE, _SEPARATOR_BELOW]
    */
    public synchronized void registerMenu(String parentMenu,
                                          JMenuItem toRegister, int separator) {
        SeparatorFactory factory = getFactory(parentMenu);

        if (separator == SeparatorFactory._SEPARATOR_ABOVE) {
            registerMenu(parentMenu,
                         factory.createJPopupMenuSeparator(toRegister
                .getClientProperty(ID_PROPERTY)));
        }
        registerMenu(parentMenu, toRegister);
        if (separator == SeparatorFactory._SEPARATOR_BELOW) {
            registerMenu(parentMenu,
                         factory.createJPopupMenuSeparator(toRegister
                .getClientProperty(ID_PROPERTY)));
        }
    }

    /**
     * Register the given menu item, setting the client property
     * to the given id string.
     *
     * @param parentMenu [String] Name of the parent menu
     * @param toRegister the menu item to add.
     * @param id [String] Client id of the menu item to unregister
     */
    public void registerMenu(String parentMenu, JMenuItem toRegister, String id) {
        toRegister.putClientProperty(ID_PROPERTY, id);
        registerMenu(parentMenu, toRegister);
    }

    /**
     * Registers a heading in the menu.
     *
     * @param parentMenu [String] Name of the parent menu
     * @param headingText [String] Text to be displayed as heading
     * @param id [String] Client id of the menu item to unregister
     */
    public void registerHeading(String parentMenu, String headingText, String id) {
        registerHeading(parentMenu, headingText, id, 1);
    }

    /**
     * Registers a heading in the menu.
     *
     * @param parentMenu [String] Name of the parent menu
     * @param headingText [String] Text to be displayed as heading
     * @param id [String] Client id of the menu item to unregister
     * @param section the section position of the new heading.
     */
    public void registerHeading(String parentMenu, String headingText,
                                String id, int section) {
        JMenuItem item = new JMenuItem(headingText);
        item.setEnabled(false);
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        item.putClientProperty(ID_PROPERTY, id);

        if (section == 1) {
            registerMenu(parentMenu, item);
        } else {
            registerMenu(parentMenu, item, section, 0);
        }
    }

    /**
     * Register the given menu item and add it to the given parentMenu.
     * This methods allows you to position the item in the menu.
     * Sections of a menu are determined by its separators. Everything
     * before the first separator is section 1, everything between the
     * first and the second separator is section 2, and so on.
     * The parameter <code>requestedSection</code> takes an <code>int</code>
     * value which chooses the section, the parameter <code>where</code>
     * takes also an <code>int</code> value and determines the position
     * inside the section:
     * <ul>
     *         <li>0: at beginning of section
     *         <li>1: at end of section
     *         <li>2: in the middle of section
     * </ul>
     * Other values of <code>where</code> produce an error message and
     * prevent the item to be added. If <code>requestedSection</code>
     * is larger than the number of sections in <code>parentMenu</code>
     * the item is added as last item.
     *
     * @param parentMenu the top level menu to which to add the menu item.
     * @param toRegister the menu item to add.
     * @param requestedSection [int] to which section item should be addd
     * @param where [int] determines where in the section item appears
     */
    public synchronized void registerMenu(String parentMenu,
                                          JMenuItem toRegister,
                                          int requestedSection, int where) {
        if (requestedSection == 0 || where > 2 || where < 0) { // invalid arguments
            logger.error(getClass().getSimpleName()
                         + ": could not register menu item "
                         + toRegister.getText()
                         + "\nparameter 'where' must be  0 < where < 3 and sections start with 1");
            return;
        }
        final JMenu parent = getOrCreateParentMenu(parentMenu);
        String id = (String) toRegister.getClientProperty(ID_PROPERTY);
        if (id == null || parent == null) { // error when no id set or parentMenu does not exist
            logger.error(getClass().getSimpleName()
                         + ": could not register menu item "
                         + toRegister.getText()
                         + (id == null
                            ? ": please set " + ID_PROPERTY
                            + " with menu.setClientProperty()."
                            : " toplevel menu " + parentMenu
                            + " does not exist"));
            return;
        }
        // register because adding always succeeds.
        registeredMenus.put(id, toRegister);
        Component[] menuComps = parent.getMenuComponents();
        for (int pos = 0, sec = 1, itemsInSec = 0;
                     pos < menuComps.length && sec <= requestedSection;
                     pos++, itemsInSec++) {
            boolean nextIsSep = pos + 1 >= menuComps.length
                                || menuComps[pos + 1] instanceof JPopupMenu.Separator;
            if (sec == requestedSection) { // found our section
                if (where == AT_BEGIN_SECTION) { // add to beginning of section
                                                 // because parent is a CommandMenu, we call the overwritten
                                                 // method there, which registers the ActionListener for the
                                                 // command. 
                    parent.add(toRegister, pos);
                    break;
                } else if (nextIsSep) { // last item of section or of menu
                    if (where == AT_END_SECTION) { // add to end of section
                        parent.add(toRegister, pos + 1);
                    } else if (where == SECTION_MIDDLE) { // add to middle of section
                        parent.add(toRegister,
                                   itemsInSec / 2 + (pos + 1 - itemsInSec));
                    }
                    break;
                }
            }
            if (menuComps[pos] instanceof JPopupMenu.Separator) {
                sec++;
                itemsInSec = 0;
            }
            if (pos + 1 == menuComps.length) { // reached last item
                parent.add(toRegister);
            }
        }
        logger.debug(getClass() + ": the menu " + toRegister.getText()
                     + " has been registered under " + parentMenu + ", id "
                     + toRegister.getClientProperty(ID_PROPERTY) + "\n"
                     + registeredMenus.get(id).getText());
        if (tmpRegisteredMenuItems == null) {
            tmpRegisteredMenuItems = new HashMap<String, JMenuItem>();
        }
        tmpRegisteredMenuItems.put(id, toRegister);
    }

    /**
     * Unregister a previously registered menu.
     * Also check out the unregisterMenu(SeparatorItem) method
     * to avoid code smell.
     *
     * @param item
     */
    public void unregisterMenu(JMenuItem item) {
        if (item instanceof SeparatorItem) {
            logger.debug(getClass() + ": no need to unregister separator.");
            return;
        }
        String id = (String) item.getClientProperty(ID_PROPERTY);
        if (id == null) {
            logger.debug(getClass() + " unregistering item " + item.getText()
                         + ": no client property set!");
            return;
        }
        unregisterMenu(id);
    }


    /**
     * Unregister the given <b>item</b> and its separator identified by the given <b>separatorType</b>.
     *
     * @param item [{@link JMenuItem}] The menu item to unregister
     * @param separatorType [int] The type of separator to unregister
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public void unregisterMenu(JMenuItem item, int separatorType) {
        String id = (String) item.getClientProperty(ID_PROPERTY);
        if (id == null) {
            logger.debug(getClass() + " unregistering item " + item.getText()
                         + ": no client property set!");
            return;
        }
        unregisterMenu(id, separatorType);
    }

    public synchronized void unregisterMenu(String id) {
        // this should work...
        // JMenu toplevel = (JMenu) item.getParent();
        // it didnt: a JPopupMenu appeared from nowhere.
        // i'll have to look for the parent manually
        JMenu toplevel = null;
        Iterator<JMenu> tlMenus = toplevelMenus.values().iterator();
        JMenuItem item = null;
        while (toplevel == null && tlMenus.hasNext()) {
            JMenu c = tlMenus.next();
            Component[] comp = c.getMenuComponents();
            for (int i = 0; i < comp.length; i++) {
                if (comp[i] instanceof JMenuItem) {
                    JMenuItem currentItem = (JMenuItem) comp[i];
                    String currentId = (String) currentItem.getClientProperty(ID_PROPERTY);
                    if (id.equals(currentId)) {
                        toplevel = c;
                        item = currentItem;
                        break;
                    }
                }
            }
        }
        if (item == null) {
            logger.debug(getClass() + ".unregisterMenu(): menu " + id
                         + " not found!");
            return;
        }

        // now, wasn't that fun?
        logger.debug(getClass() + ": unregistering menu " + item.getText()
                     + " from "
                     + (toplevel != null ? toplevel.getText()
                                         : "[ERROR: JMenu 'topLevel' not set!]"));
        if (toplevel != null) {
            toplevel.remove(item);
            // if the first item in the menu is now a separator,
            // remove it.
            while (toplevel.getItemCount() > 0 && toplevel.getItem(0) == null) {
                logger.debug(getClass() + ": removing top separator.");
                toplevel.remove(0);
            }

            // same for the last one.
            while (toplevel.getItemCount() > 0
                           && toplevel.getItem(toplevel.getItemCount() - 1) == null) {
                logger.debug(getClass() + ": removing top separator.");
                toplevel.remove(toplevel.getItemCount() - 1);
            }
            removeMenuIfEmpty(toplevel.getText(), toplevel);
        }

        registeredMenus.remove(item.getClientProperty(ID_PROPERTY));
    }

    /**
     * Unregister menu item with given <b>id</b> and its separator of given <b>separatorType</b> from menu.
     * @param id [String] Client id of the menu item to unregister
     * @param separatorType [int] Separator type to unregister
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public synchronized void unregisterMenu(String id, int separatorType) {
        JMenu toplevel = null;
        Iterator<JMenu> tlMenus = toplevelMenus.values().iterator();
        JMenuItem item = null;
        JPopupMenu.Separator separator = null;
        while (toplevel == null && tlMenus.hasNext()) {
            JMenu c = tlMenus.next();
            Component[] comp = c.getMenuComponents();
            for (int i = 0; i < comp.length; i++) {
                if (comp[i] instanceof JMenuItem) {
                    JMenuItem currentItem = (JMenuItem) comp[i];
                    String currentId = (String) currentItem.getClientProperty(ID_PROPERTY);
                    if (id.equals(currentId)) {
                        toplevel = c;
                        item = currentItem;
                        if (i - 1 >= 0
                                    && separatorType == SeparatorFactory._SEPARATOR_ABOVE) {
                            Component tmp = comp[i - 1];
                            if (tmp instanceof JPopupMenu.Separator) {
                                separator = (JPopupMenu.Separator) tmp;
                            }
                        }
                        if (i + 1 < comp.length
                                    && separatorType == SeparatorFactory._SEPARATOR_BELOW) {
                            Component tmp = comp[i + 1];
                            if (tmp instanceof JPopupMenu.Separator) {
                                separator = (JPopupMenu.Separator) tmp;
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (item == null) {
            logger.debug(getClass() + ".unregisterMenu(): menu " + id
                         + " not found! Check registeredMenus instead...");
            return;
        }

        // now, wasn't that fun?
        logger.debug(getClass() + ": unregistering menu " + item.getText()
                     + " from "
                     + (toplevel != null ? toplevel.getText()
                                         : "[ERROR: JMenu 'topLevel' not set!]"));
        if (toplevel != null) {
            toplevel.remove(item);
            if (separator != null) {
                toplevel.remove(separator);
            }

            // if the first item in the menu is now a separator,
            // remove it.
            while (toplevel.getItemCount() > 0 && toplevel.getItem(0) == null) {
                logger.debug(getClass() + ": removing top separator.");
                toplevel.remove(0);
            }

            // same for the last one.
            while (toplevel.getItemCount() > 0
                           && toplevel.getItem(toplevel.getItemCount() - 1) == null) {
                logger.debug(getClass() + ": removing top separator.");
                toplevel.remove(toplevel.getItemCount() - 1);
            }
            removeMenuIfEmpty(toplevel.getText(), toplevel);
        }

        registeredMenus.remove(item.getClientProperty(ID_PROPERTY));
    }

    /**
     * Describe <code>removeMenuIfEmpty</code> method here.
     *
     * @param parentMenu a <code>String</code> value
     * @param menuObject a <code>JMenu</code> value
     **/
    private void removeMenuIfEmpty(String parentMenu, JMenu menuObject) {
        // Never remove the windows menu.
        if (DrawPlugin.WINDOWS_MENU.equals(parentMenu)) {
            return;
        }

        // check if menuObject menu has become empty
        if (menuObject.getMenuComponentCount() == 0) {
            logger.debug(getClass() + ": the last MenuItem of menu "
                         + parentMenu + " has been removed, removing menu");
            if (application != null) {
                application.removeMenu(menuObject);
            }
            toplevelMenus.remove(parentMenu);
            toplevelOrder.remove(parentMenu);
            if (DrawPlugin.HELP_MENU.equals(parentMenu)) {
                helpMenuPresent = false;
            }
        }
    }

    /**
     * This method should be called if the gui is opened (with the new instance as parameter)
     * or closed (with null to allow garbage collection).
     *
     * @param app the Gui object that has just been opened or null if a previous
     * instance is closed.
     */
    void setGui(DrawApplication app) {
        application = app;

        if (application != null) {
            logger.debug(getClass() + ": adding menus to application object "
                         + app);


            // create all those registered menus
            // applying the order in which they were created
            Iterator<String> labels = toplevelOrder.iterator();
            while (labels.hasNext()) {
                String label = labels.next();
                JMenu add = toplevelMenus.get(label);
                application.addMenu(add);
            }
        }
    }

    /**
     * Convenience method to be able to create a
     * separator JMenuItem on the fly.
     * This method does NOT assure that the id is unique
     * so the client has to assure it.
     * If you want the framework to handle unique ids,
     * use a SeparatorFactory object.
     * @param id  the id for referencing that is set as client property.
     * @return a new separator menu item with the given <code>id</code>.
     */
    public static JMenuItem createSeparator(String id) {
        return new SeparatorItem(id);
    }

    JMenu getToplevelMenu(String name) {
        return toplevelMenus.get(name);
    }

    /**
     * Returns the special windows menu.
     * Although this menu is a toplevel menu, it cannot be unregistered.
     * Currently it is always created with the <code>MenuManager</code>.
     *
     * @return the registered <code>WindowsMenu</code> instance.
     **/
    public WindowsMenu getWindowsMenu() {
        return (WindowsMenu) getToplevelMenu(DrawPlugin.WINDOWS_MENU);
    }

    /**
     * Convenience method to create a MenuItem from a given Command
     * that will be executed when the item is chosen.
     *
     * @param command   the command to execute when this menu item is
     *                  activated.
     * @param shortcut  the AWT keyboard shortcut to assign to this menu entry.
     * @return  the created menu item.
     */
    public static CommandMenuItem createMenuItem(Command command, int shortcut) {
        CommandMenuItem item = new CommandMenuItem(command, shortcut);
        return item;
    }

    // an evil tag class to be able to add separators to menus...
    // can't think of any other way
    private static class SeparatorItem extends JMenuItem {
        public SeparatorItem(String label) {
            // we need a label to be able to remove the menu again
            super(label);
        }
    }

    /**
     * Use this class to create Separators in your menus.
     * <br>
     * In the method where you create your menu, instantiate a
     * SeparatorFactory with your Plugin's ID
     * (the one you use in your putClientProperty(MenuManager.ID_PROPERTY, ...)-call).
     * The factory will then create JMenuItems that will be identified by the MenuManager,
     * so it will add a separator if you call menuManager.registerMenu(String, JMenuItem).
     * <br>
     * The factory will set the proper ids, so you don't have to worry about that.
     * Actually, you shouldn't.
     *
     * @author Joern Schumacher,Eva Mueller
     * @modified Nov 14, 2010
     * @version 0.1
     */
    public static class SeparatorFactory {

        /**
             * _SEPARATOR_ABOVE = 0
             */
        public final static int _SEPARATOR_ABOVE = 0;

        /**
             * _SEPARATOR_BELOW = 1
             */
        public final static int _SEPARATOR_BELOW = 1;
        private int count;
        private String id;

        public SeparatorFactory(String id) {
            this.id = id;
        }

        public JMenuItem createSeparator() {
            JMenuItem item = new SeparatorItem(id + ".sep" + count);
            item.putClientProperty(ID_PROPERTY, id + ".sep" + count++);
            return item;
        }

        /**
         * Create {@link JPopupMenu.Separator} add the given <b>menuID</b> to<br>
                 * the separator's id (idOfSeparatorFactory + "." + menuID + ".separator").
         * @param menuID [Object] The separator identifier
         * @return [{@link JPopupMenu.Separator}]
         *
         * @author Eva Mueller
         * @date Nov 14, 2010
         * @version 0.1
         */
        public JPopupMenu.Separator createJPopupMenuSeparator(Object menuID) {
            JPopupMenu.Separator item = new JPopupMenu.Separator();
            item.putClientProperty(ID_PROPERTY, id + "." + menuID
                                   + ".separator");
            return item;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Get {@link SeparatorFactory} for the given <b>parentMenu</b>.<br>
     * If no factory is available then a new one is created and stored.
     * @param parentMenu [String] Name of the parent menu
     * @return {@link SeparatorFactory}
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    private SeparatorFactory getFactory(String parentMenu) {
        SeparatorFactory factory = null;
        if (separatorFactories == null) {
            separatorFactories = new HashMap<String, SeparatorFactory>();
        }
        if (separatorFactories.containsKey(parentMenu)) {
            factory = separatorFactories.get(parentMenu);
        }
        if (!separatorFactories.containsKey(parentMenu)) {
            factory = new SeparatorFactory(parentMenu);
            separatorFactories.put(parentMenu, factory);
        }
        return factory;
    }

    /**
     * Gets an existing parent menu or exists one,
     * if none exists for given parent menu name.
     *
     * @param parentMenu [String] Name of the parent menu.
     * @return a JMenu for this menu.
     */
    private JMenu getOrCreateParentMenu(String parentMenu) {
        JMenu parent = toplevelMenus.get(parentMenu);

        if (parent == null) {
            logger.debug(getClass() + ": creating new top level menu "
                         + parentMenu);

            int newPosition;
            if (parentMenu.equals(DrawPlugin.WINDOWS_MENU)) {
                throw new RuntimeException("Windows menu should be always present, but is not available!");
            } else if (parentMenu.equals(DrawPlugin.HELP_MENU)) {
                parent = new CommandMenu(parentMenu);
                newPosition = toplevelOrder.size();
                helpMenuPresent = true;
            } else {
                // we need a command menu so DrawApp will be able to check enabledness
                // and automatically trigger commands in CommandMenuItems
                parent = new CommandMenu(parentMenu);
                newPosition = toplevelOrder.size() - (helpMenuPresent ? 1 : 0)
                              - (windowsMenuPresent ? 1 : 0);
            }

            toplevelMenus.put(parentMenu, parent);
            toplevelOrder.add(newPosition, parentMenu);
            if (application != null) {
                application.addMenu(parent, newPosition);
            }
        }

        return parent;
    }
}