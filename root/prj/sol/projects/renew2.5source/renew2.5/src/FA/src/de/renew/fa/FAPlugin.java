package de.renew.fa;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager;
import CH.ifa.draw.application.MenuManager.SeparatorFactory;
import CH.ifa.draw.application.VersionInfoCommand;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingTypeManager;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;

import de.renew.fa.commands.ChangeDecorationCommand;
import de.renew.fa.commands.ChangeFADrawModeCommand;
import de.renew.fa.commands.ShowPaletteCommand;
import de.renew.fa.figures.EndDecoration;
import de.renew.fa.figures.FAArcConnection;
import de.renew.fa.figures.FADrawMode;
import de.renew.fa.figures.FAStateFigure;
import de.renew.fa.figures.FATextFigure;
import de.renew.fa.figures.NullDecoration;
import de.renew.fa.figures.StartDecoration;
import de.renew.fa.figures.StartEndDecoration;
import de.renew.fa.service.JflapExportFormat;
import de.renew.fa.service.XFAExportFormat;
import de.renew.fa.service.XFAImportFormat;

import de.renew.gui.CPNInstanceDrawing;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.GuiPlugin;
import de.renew.gui.InscribableFigure;
import de.renew.gui.TextFigureCreator;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.PropertyHelper;

import java.awt.event.KeyEvent;

import java.net.URL;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JMenuItem;


/**
 * The wrapper for the FA plugin.
 *
 * <pre>
 * 0.3.x - Added Figure creation. Fixed several issues with drawing representation.
 * 0.4.0 - Added export to JFLAP (v4) [jff].
 *         Included prompt of formal fa description on console as LaTeX code.
 * 0.5.0 - Added simulation support. Only simple states can be simulated
 *                    for now.
 * </pre>
 *
 * @author Lawrence Cabac
 * @author Möller
 * @version 0.5.0
 *
 */
public class FAPlugin extends PluginAdapter {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FAPlugin.class);
    private static final String KEY_USEINDICES = "de.renew.fa.useindices";
    private static final String KEY_STATE_PREFIX = "de.renew.fa.state-prefix";

    /*
     * The images directory.
     */
    final static public String IMAGES = "images/";

    /*
     * A list of commands added by FAPluginExtenders.
     */
    private Hashtable<Command, IPlugin> _commandList;
    /*
     * A list of all registered plugins to this.
     */
    private Vector<IPlugin> _pluginList;
    /*
     * The FA Drawing Tool menu entry
     */
    private CommandMenu _menu;
    /*
     * A SaparatorFactory for separating FA Drawing Tool menu
     */
    private JMenuItem _separator;
    /*
     * A FigureCreator responsible for creating FAStateFigures
     */
    private FAFigureCreator faFigureCreator = null;
    private boolean loaded;
    /*
     *
     */
    private PaletteCreator pc;

    /**
     * The Icon for the Alert Messages.
     */
    public FAPlugin(PluginProperties props) {
        super(props);
        _pluginList = new Vector<IPlugin>();
        _commandList = new Hashtable<Command, IPlugin>();
    }

    public FAPlugin(URL location) throws PluginException {
        super(location);
        _pluginList = new Vector<IPlugin>();
        _commandList = new Hashtable<Command, IPlugin>();
    }

    /**
     * Initializes the FAPlugin by
     * <ul>
     *   <li> registering the instance drawing for simulation visualization
     *   <li> adding the menu entry
     *   <li> adding import and export formats
     * </ul>
     * @see de.renew.plugin.IPlugin#init()
     */
    @Override
    public void init() {
        DrawPlugin gs = DrawPlugin.getCurrent();
        _menu = createMenu();

        // register own InstanceDrawing for simulation support
        CPNInstanceDrawing.registerInstanceDrawingFactory(FADrawing.class,
                                                          new FAInstanceDrawingFactory());

        if (gs != null) {
            SeparatorFactory sepFac = new SeparatorFactory("de.renew.fa");
            _separator = sepFac.createSeparator();
            DrawPlugin current = DrawPlugin.getCurrent();

            // add FA Drawing Tool menu to Tools menu and separate the the upper part
            current.getMenuManager()
                   .registerMenu(DrawPlugin.TOOLS_MENU, _separator);
            current.getMenuManager().registerMenu(DrawPlugin.TOOLS_MENU, _menu);

            faFigureCreator = new FAFigureCreator();
            GuiPlugin.getCurrent().getFigureCreatorHolder()
                     .registerCreator(faFigureCreator);

            // register the Drawing at the DrawingTypeManager
            //NOTICEredundant
            if (gs != null) {
                DrawingTypeManager.getInstance()
                                  .register("de.renew.fa.FADrawing",
                                            new FAFileFilter());
            }

            // Activating palette
            try {
                boolean init = PropertyHelper.getBoolProperty(getProperties(),
                                                              "de.renew.fa.init");
                logger.debug("init protperty is : " + init);
                if (init) {
                    // add stuff for execution when init is set
                }
            } catch (RuntimeException e) {
            }

            // Adding import/export support
            current.getImportHolder().addImportFormat(new XFAImportFormat());
            current.getExportHolder().addExportFormat(new XFAExportFormat());
            current.getExportHolder().addExportFormat(new JflapExportFormat());
        }
    }

    /**
     * @see de.renew.plugin.IPlugin#cleanup()
     */
    @Override
    public boolean cleanup() {
        DrawPlugin gui = DrawPlugin.getCurrent();
        if (gui != null) {
            gui.getMenuManager().unregisterMenu(_separator);
            gui.getMenuManager().unregisterMenu(_menu);
        }
        if (loaded) {
            pc.remove();
            loaded = false;
        }
        if (faFigureCreator != null) {
            GuiPlugin.getCurrent().getFigureCreatorHolder()
                     .unregisterCreator(faFigureCreator);
            faFigureCreator = null;
        }

        return true;
    }

    /**
    * Creates a new default palette and refreshes the menuFrame, if not already loaded.
    * If loaded the palette gets removed.
    */
    public void create() {
        // toggel on/off
        if (loaded) {
            pc.remove();
            loaded = false;
        } else {
            loadPalette();
        }
    }

    /**
     * Loads either the standard or the alternative palette.
     */
    private void loadPalette() {
        if (FADrawMode.getInstance().getMode() == FADrawMode.STANDARD) {
            pc = new PaletteCreator("FAPalette");
            logger.debug("created standard palette");
        } else {
            pc = new AltPaletteCreator("FAPalette");
            logger.debug("created alt palette");
        }

        loaded = true;
    }

    /**
     * When FADrawMode changed, this methode switches palettes.
     */
    public void switchPalette() {
        if (loaded) {
            pc.remove();
            loadPalette();
        }
    }

    /**
     * Adds FA Drawing Tool menu entry with
     * <ul>
     *         <li>command for showing palette including shortcut</li>
     *         <li>submenu for changing decoration of selected element</li>
     *         <li>command for changing draw mode</li>
     *         <li>command for displaying version info</li>
     * </ul>
     * @return
     */
    private CommandMenu createMenu() {
        _menu = new CommandMenu("FA Drawing Tool");

        // Shortcut part
        Command command = new ShowPaletteCommand(this);
        _menu.add(command, KeyEvent.VK_6);
        _commandList.put(command, this);

        // Submenu part
        CommandMenu submenu = DrawApplication.createCommandMenu("Decoration");
        command = new ChangeDecorationCommand("Start", new StartDecoration());
        submenu.add(command);
        _commandList.put(command, this);
        command = new ChangeDecorationCommand("End", new EndDecoration());
        submenu.add(command);
        _commandList.put(command, this);
        command = new ChangeDecorationCommand("Start/End",
                                              new StartEndDecoration());
        submenu.add(command);
        _commandList.put(command, this);
        command = new ChangeDecorationCommand("none", new NullDecoration());
        submenu.add(command);
        _commandList.put(command, this);
        _menu.add(submenu);

        // Change drawmode part
        command = new ChangeFADrawModeCommand(this);
        _menu.add(command);
        _commandList.put(command, this);

        _menu.addSeparator();

        // Version info part
        command = new VersionInfoCommand(this);
        _menu.add(command);
        _menu.putClientProperty(MenuManager.ID_PROPERTY, "de.renew.fa");
        _commandList.put(command, this);

        //_menu.add(new ExportJflapCommand("jflap export"));
        //        _menu.addSeparator();
        //        submenu = DrawApplication.createCommandMenu("Export");
        //        submenu.add(new ExportAsXFACommand("as XFA"));
        //        _menu.add(submenu);
        //
        //        _menu.addSeparator();
        //        submenu = DrawApplication.createCommandMenu("Import");
        //        submenu.add(new ImportFromXFACommand("from XFA"), KeyEvent.VK_7);
        //        _menu.add(submenu);
        //
        //        _menu.addSeparator();
        //        submenu = DrawApplication.createCommandMenu("Analysis");
        //        submenu.add(new ConnectedPropertyTestCommand("FA is connected"),
        //                    KeyEvent.VK_5);
        //        _menu.add(submenu);
        return _menu;
    }

    /**
     *
     */
    public void deregisterPlugin(IPlugin plugin) {
        Enumeration<Command> it = _commandList.keys();

        // Removing all menu entries for the deregistering plugin.
        while (it.hasMoreElements()) {
            Command command = it.nextElement();
            if (_commandList.get(command) instanceof FAPluginExtender) {
                _menu.remove(command);
                _commandList.remove(command);
            }
        }
        _pluginList.remove(plugin);

    }


    /**
    * Adds plugins, but is only functional for FAPluginExtenders by adopting
    * their commands.
    */
    public void registerPlugin(IPlugin plugin) {
        _pluginList.add(plugin);
        if (plugin instanceof FAPluginExtender) {
            Vector<Command> v = ((FAPluginExtender) plugin).getMenuCommands();
            Iterator<Command> it = v.iterator();
            while (it.hasNext()) {
                Command command = it.next();
                _menu.add(command);
                _commandList.put(command, plugin);

                logger.debug("Added command " + command.toString() + "\n"
                             + " of Plugin " + plugin);
            }
        }
    }

    public static FAPlugin getCurrent() {
        PluginManager pm = PluginManager.getInstance();
        if (pm == null) {
            return null;
        }
        return (FAPlugin) pm.getPluginByName("Renew Finite Automata Base");
    }

    public boolean getUseIndices() {
        return getProperties().getBoolProperty(KEY_USEINDICES);
    }

    /**
     * Provides the FAStateFigure figure with its standard inscription.
     */
    private class FAFigureCreator implements TextFigureCreator {
        @Override
        public boolean canCreateDefaultInscription(InscribableFigure figure) {
            if (figure instanceof FAStateFigure) {
                return true;
            }
            if (figure instanceof FAArcConnection) {
                return true;
            }
            return false;
        }

//        @Override
//        public boolean canCreateFigure(InscribableFigure figure) {
//            if (figure instanceof FAStateFigure) {
//                return true;
//            }
//            return false;
//        }

//        @Override
//        public TextFigure createTextFigure(InscribableFigure figure) {
//            if (figure instanceof FAStateFigure) {
//                logger.debug("LABEL created!");
//                return new FATextFigure(CPNTextFigure.NAME);
//            }
//            if (figure instanceof FAArcConnection) {
//                return new FATextFigure(CPNTextFigure.INSCRIPTION);
//            }
//            return null;
//        }

//        /**
//         * Gets standard inscription for FAStateFigures and FAArcConnections.
//         *
//         * @return inscription of form <code>Z[i]</code> for FAStateFigures
//         *         of form <code>[lowercase letter]</code> (starting from a)
//         *                                                                                         for FAArcConnections
//         */
//        @Override
//        public String getDefaultInscription(InscribableFigure figure) {
//            logger.debug("getDefaultInscription called");
//            if (figure instanceof FAStateFigure) {
//                DrawApplication da = GuiPlugin.getCurrent().getGui();
//                int i = 0;
//                if (da != null) {
//                    HashSet<String> labels = new HashSet<String>();
//                    Drawing d = da.drawing();
//                    FigureEnumeration figEnum = d.figures();
//                    while (figEnum.hasMoreElements()) {
//                        Figure fig = figEnum.nextFigure();
//                        if (fig instanceof CPNTextFigure) {
//                            String text = ((CPNTextFigure) fig).getText();
//                            if (text.startsWith("Z")) {
//                                labels.add(text);
//                            }
//                        }
//                    }
//                    while (labels.contains("Z" + i)) {
//                        i++;
//                    }
//                }
//                return "Z" + i;
//            }
//            if (figure instanceof FAArcConnection) {
//                return "a";
//            }
//            return null;
//        }
        @Override
        public boolean canCreateFigure(InscribableFigure figure) {
            return canCreateDefaultInscription(figure);
        }

        @Override
        public TextFigure createTextFigure(InscribableFigure figure) {
            if (figure instanceof FAStateFigure) {
                return new FATextFigure(CPNTextFigure.NAME);
            } else if (figure instanceof FAArcConnection) {
                return new FATextFigure(CPNTextFigure.INSCRIPTION);
            }
            return null;
        }

        @Override
        public String getDefaultInscription(InscribableFigure figure) {
            boolean useindices = getProperties().getBoolProperty(KEY_USEINDICES);
            String sp = getProperties().getProperty(KEY_STATE_PREFIX);
            sp = (sp == null ? "z" : sp); // default if not set 
            if (figure instanceof FAStateFigure) {
                DrawApplication da = GuiPlugin.getCurrent().getGui();
                int i = 0;
                if (da != null) {
                    HashSet<String> labels = new HashSet<String>();
                    Drawing d = da.drawing();
                    FigureEnumeration figEnum = d.figures();
                    while (figEnum.hasMoreElements()) {
                        Figure fig = figEnum.nextFigure();
                        if (fig instanceof FAStateFigure) {
                            FigureEnumeration stateChildren = ((FAStateFigure) fig)
                                                              .children();
                            if (stateChildren.hasMoreElements()) {
                                String label = ((TextFigure) stateChildren
                                                   .nextFigure()).getText();
                                labels.add(label);
                            }
                        }
                    }
                    while (labels.contains(sp + i)
                                   || labels.contains(sp + "_" + i)
                                   || labels.contains(sp + "_{" + i + "}")) {
                        i++;
                    }
                }
                if (useindices) {
                    return sp + "_{" + i + "}";
                }
                return sp + i;
            }
            if (figure instanceof FAArcConnection) {
                return "a";
            }
            return null;
        }
    }
}