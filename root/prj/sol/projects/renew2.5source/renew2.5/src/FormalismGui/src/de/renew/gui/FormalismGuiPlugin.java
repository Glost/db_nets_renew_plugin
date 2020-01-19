/*
 * Created on 05.05.2003
 *
 */
package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;
import CH.ifa.draw.application.MenuManager.SeparatorFactory;

import CH.ifa.draw.util.Palette;

import de.renew.formalism.FormalismChangeListener;
import de.renew.formalism.FormalismPlugin;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * Provides a gui interface presenting all choices that are
 * available with the formalism plugin.
 * @author J&ouml;rn Schumacher
 **/
public class FormalismGuiPlugin extends PluginAdapter
        implements FormalismChangeListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FormalismGuiPlugin.class);

    /**
     * The palette associated with the formalism currently chosen.
     **/
    private Palette currentPalette;

    /**
     * The menu entry associated with the formalism currently chosen.
     **/
    private JMenuItem currentMenu;

    /**
     * The gui creator associated with the formalism currently chosen.
     **/
    private FormalismGuiCreator currentCreator;

    /**
     * The menu where all formalism entries are presented.
     **/
    private JMenu _formalismMenu;

    /**
     * Maps from formalism names to the respective entries in the
     * formalisms menu. Contains ({@link String}, {@link JMenuItem})
     * pairs.
     **/
    private Map<String, JMenuItem> _menuEntries = new HashMap<String, JMenuItem>();

    /**
     * The menu manager where we register our menu entries.
     **/
    private MenuManager _menuManager;

    /**
     * Maps from formalism names to the associated gui configuration
     * objects. Contains ({@link String}, {@link FormalismGuiCreator})
     * pairs.
     **/
    private Map<String, FormalismGuiCreator> _guicreators = new HashMap<String, FormalismGuiCreator>();

    public FormalismGuiPlugin(URL location) throws PluginException {
        super(location);
    }

    public FormalismGuiPlugin(PluginProperties props) {
        super(props);
    }

    public static FormalismGuiPlugin getCurrent() {
        Iterator<IPlugin> it = PluginManager.getInstance().getPlugins()
                                            .iterator();
        while (it.hasNext()) {
            IPlugin o = it.next();
            if (o instanceof FormalismGuiPlugin) {
                return (FormalismGuiPlugin) o;
            }
        }
        return null;
    }

    public void init() {
        logger.debug("initializing FormalismGui");

        JavaGuiCreator javaGuiCreator = new JavaGuiCreator();
        _guicreators.put(FormalismPlugin.JAVA_COMPILER, javaGuiCreator);
        _guicreators.put(FormalismPlugin.TIMED_COMPILER,
                         javaGuiCreator.getSequentialJavaGuiCreator());

        FormalismPlugin store = FormalismPlugin.getCurrent();

        // register myself as listener so I can keep the menu up-to-date
        store.addFormalismChangeListener(this);

        // TODO: OKU: Once the new multi-formalism compilation
        // is in place, think of a new lint system. Until then,
        // this listener does more harm than good.
        // store.addFormalismChangeListener(new JavaCompilerListener());
        _formalismMenu = createMenu();

        GuiPlugin starter = GuiPlugin.getCurrent();

        logger.debug("adding menu");
        _menuManager = DrawPlugin.getCurrent().getMenuManager();
        SeparatorFactory sepFac = new SeparatorFactory("de.renew.formalism.gui");
        _menuManager.registerMenu(GuiPlugin.SIMULATION_MENU,
                                  sepFac.createSeparator());
        _menuManager.registerMenu(GuiPlugin.SIMULATION_MENU, _formalismMenu,
                                  "de.renew.formalism.gui.formalisms");

        if (starter == null) {
            logger.debug("FormalismGuiPlugin: no GuiPlugin, could not add menu.");
        }
        formalismChosen(store.getCompiler());
    }

    public void addGuiConfigurator(String formalismName,
                                   FormalismGuiCreator creator) {
        logger.debug("Adding GUI configurator for " + formalismName);
        FormalismPlugin store = FormalismPlugin.getCurrent();
        String currentFormalism = store.getCompiler();
        boolean affectsCurrentFormalism = (currentFormalism != null
                                          && currentFormalism.equals(formalismName));
        if (affectsCurrentFormalism) {
            cleanGui();
        }
        _guicreators.put(formalismName, creator);
        if (affectsCurrentFormalism) {
            createGuiItems(currentFormalism);
        }
    }

    public void removeGuiConfigurator(String formalismName) {
        logger.debug("Removing GUI configurator for " + formalismName);
        FormalismPlugin store = FormalismPlugin.getCurrent();
        String currentFormalism = store.getCompiler();
        boolean affectsCurrentFormalism = (currentFormalism != null
                                          && currentFormalism.equals(formalismName));
        if (affectsCurrentFormalism) {
            cleanGui();
        }
        _guicreators.remove(formalismName);
    }

    private JMenu createMenu() {
        JMenu result = new JMenu("Formalisms");
        FormalismPlugin store = FormalismPlugin.getCurrent();
        Iterator<String> formalisms = store.getKnownFormalisms();
        while (formalisms.hasNext()) {
            String current = formalisms.next();
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem(current);
            if (current.equals(store.getCompiler())) {
                item.setSelected(true);
            }
            item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String chosenCompiler = item.getText();
                        logger.debug("compiler " + chosenCompiler
                                     + " has been chosen.");
                        formalismChosen(chosenCompiler);
                    }
                });
            result.add(item);
        }
        return result;
    }

    private void formalismChosen(String chosenCompiler) {
        FormalismPlugin store = FormalismPlugin.getCurrent();
        store.setCompiler(chosenCompiler);
        selectFormalismItem(chosenCompiler);

        cleanGui();

        createGuiItems(chosenCompiler);
    }

    private void createGuiItems(String chosenCompiler) {
        GuiPlugin starter = GuiPlugin.getCurrent();

        // do we need more gui entries (toolbar and/or menus) for the chosen compiler?
        currentCreator = _guicreators.get(chosenCompiler);
        if (currentCreator != null) {
            currentPalette = currentCreator.createPalette();
            if (currentPalette != null) {
                starter.getPaletteHolder().addPalette(currentPalette);
            }

            currentMenu = currentCreator.createMenu();
            if (currentMenu != null) {
                _menuManager.registerMenu(GuiPlugin.SIMULATION_MENU,
                                          currentMenu,
                                          "de.renew.formalism.gui.current");
            }
            currentCreator.formalismActivated();
        }
    }

    private void cleanGui() {
        // if the previously chosen compiler added gui entries,
        // remove them
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (currentCreator != null) {
            currentCreator.formalismDeactivated();
            currentCreator = null;
        }
        if (currentPalette != null) {
            starter.getPaletteHolder().removePalette(currentPalette);
            currentPalette = null;
        }
        if (currentMenu != null) {
            _menuManager.unregisterMenu(currentMenu);
            currentMenu = null;
        }
    }

    private void selectFormalismItem(String chosenCompiler) {
        // check the chosen compiler in the menu
        Component[] comps = _formalismMenu.getMenuComponents();
        for (int i = 0; i < comps.length; i++) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) comps[i];
            boolean selectItem = item.getText().equals(chosenCompiler);
            item.setSelected(selectItem);
        }
    }

    public boolean cleanup() {
        FormalismPlugin store = FormalismPlugin.getCurrent();
        store.removeFormalismChangeListener(this);
        cleanGui();
        DrawPlugin.getCurrent().getMenuManager().unregisterMenu(_formalismMenu);
        _formalismMenu = null;
        return true;
    }

    // this will be triggered if a formalism is added to the compilerstore
    public void formalismChanged(String formalismName, Object name, int action) {
        if (action == FormalismChangeListener.ADD) {
            logger.debug("FormalismGuiPlugin: new formalism added: "
                         + formalismName);

            // we need to add it to the menu
            final JMenuItem item = new JCheckBoxMenuItem(formalismName);
            item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String chosenCompiler = item.getText();
                        logger.debug("compiler " + chosenCompiler
                                     + " has been chosen.");
                        formalismChosen(chosenCompiler);
                    }
                });
            _formalismMenu.add(item);
            _menuEntries.put(formalismName, item);
        } else if (action == FormalismChangeListener.CHOOSE) {
            logger.debug("FormalismGuiPlugin: formalism chosen: "
                         + formalismName);
            formalismChosen(formalismName);
        } else if (action == FormalismChangeListener.REMOVE) {
            logger.debug("FormalismGuiPlugin: formalism removed: "
                         + formalismName);
            JMenuItem item = _menuEntries.get(formalismName);
            if (item != null) {
                _formalismMenu.remove(item);
                _menuEntries.remove(formalismName);
            }
        }
    }
}