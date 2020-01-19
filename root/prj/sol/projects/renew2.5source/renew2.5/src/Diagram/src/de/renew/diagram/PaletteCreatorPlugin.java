package de.renew.diagram;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;
import CH.ifa.draw.application.MenuManager.SeparatorFactory;
import CH.ifa.draw.application.VersionInfoCommand;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingTypeManager;

import CH.ifa.draw.util.CommandMenu;

import de.renew.diagram.commands.HideTextCommand;
import de.renew.diagram.commands.ToggleTextCommand;
import de.renew.diagram.commands.UnhideTextCommand;
import de.renew.diagram.drawing.DiagramDrawing;
import de.renew.diagram.peer.NCLoader;

import de.renew.gui.GuiPlugin;
import de.renew.gui.InscribableFigure;
import de.renew.gui.TextFigureCreator;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.PropertyHelper;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JMenuItem;


/**
 * The wrapper for the PaletteCreator.
 *
 * <pre>
 *
 * Added features:
 *
 *  - generation of Petri net structures for net components in, out, start, cond,
 *    ajoin, psplit and pjoin.
 *
 *  - (0.3.2)
 *    Added FigureCreator for Tasks and Messages.
 *  - (0.5.0)
 *  - (0.5.1)
 *    Fixed some problems with the generation of text.
 *  - (0.5.2)
 *    Fixed grouping of components.
 *    Added DCServiceTextFigure and support for generation of NC *exchange
 *  - (0.5.3)
 *    Fixed canvas of generated nets (to show correct scroll bars).
 *    Allowing stop as action inscription.
 *  - (0.6.0) Added hiding and unhiding of text figures
 * </pre>
 *     known bugs: when the palette is detached from the gui the palette will not be removed. -> fix later!
 *
 * @author Lawrence Cabac
 * @version 0.6.0
 *
 */
public class PaletteCreatorPlugin extends PluginAdapter {
    private static final int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit()
                                                             .getMenuShortcutKeyMask();
    static private URL location;

    /**
    * The images directory.
    */
    final static public String IMAGES = "images/ ";
    private static final String PLUGIN_NAME = "Renew Diagram Tool";
    private PaletteCreator pc;
    /*
     * Not used yet(?)
     */
    private CommandMenu _menu;
    private JMenuItem _separator;
    private boolean loaded;
    private DiagramFigureCreator diagramFigureCreator = null;

    public PaletteCreatorPlugin(URL location) throws PluginException {
        super(location);
        setLocation(location);
    }

    public PaletteCreatorPlugin(PluginProperties props) {
        super(props);
        setLocation(props.getURL());
    }

    /**
     * @see de.renew.plugin.IPlugin#init()
     */
    public void init() {
        DrawPlugin gs = DrawPlugin.getCurrent();
        _menu = createMenu();
        if (gs != null) {
            registerMenu(gs);

            diagramFigureCreator = new DiagramFigureCreator();
            GuiPlugin.getCurrent().getFigureCreatorHolder()
                     .registerCreator(diagramFigureCreator);


            //            gs.addMenuExtender(menuExtender);
            //logger.debug("************************Plugin wurde initialisiert.");
            //        if (System.getProperty("PLUGIN_INIT") != null) {
            //logger.debug("************************Plugin wurde initialisiert.");
            //            String s = System.getProperty("PLUGIN_INIT");
            //            if (s.indexOf("de.renew.diagram") > -1) {
            //                this.create();
            //            }
            //        }
            // register the Drawing at the DrawingTypeManager
            //NOTICEredundant
            DrawingTypeManager.getInstance()
                              .register("de.renew.diagram.drawing.DiagramDrawing",
                                        new AIPFileFilter());
            //logger.debug("de.renew.diagram.drawing.DiagramDrawing " + new AIPFileFilter());
            try {
                boolean init = PropertyHelper.getBoolProperty(getProperties(),
                                                              "de.renew.diagram.init");
                if (init) {
                    create();
                }
            } catch (RuntimeException e) {
            }
        }
    }

    public boolean cleanup() {
        DrawPlugin gui = DrawPlugin.getCurrent();
        if (gui != null) {
            if (_separator != null) {
                gui.getMenuManager().unregisterMenu(_separator);
            }

            if (_menu != null) {
                gui.getMenuManager().unregisterMenu(_menu);
            }
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
            pc = new PaletteCreator("diagramPalette");
            loaded = true;
        }
    }

    public static NCLoader getNCLoader() {
        return NCLoader.getInstance(getLocation());
    }

    public static NCLoader getDCNCLoader() {
        String path = getLocation().getPath();
        path.replaceFirst("MulanComponents", "DCNetComponents");
        NCLoader loader = null;

        try {
            loader = NCLoader.getInstance(new URL(path));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return loader;
    }

    /**
     * Generate the Peer Nets for all RoleDescriptorFigures, i.e. the Mulan protocols.
     */
    public void generatePeers() {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter != null) {
            Drawing drawing = starter.getDrawingEditor().drawing();
            if (drawing != null && drawing instanceof DiagramDrawing) {
                ((DiagramDrawing) drawing).generatePeers();
            }
        }
    }

    static public URL getLocation() {
        return location;
    }

    static public void setLocation(URL newLocation) {
        location = newLocation;
    }

    /**
     * Registers a menu in the menu.
     *
     * @param drawPlugin the current DrawPlugin.
     */
    protected void registerMenu(DrawPlugin drawPlugin) {
        SeparatorFactory sepFac = new SeparatorFactory("ch.ifa.draw");
        _separator = sepFac.createSeparator();
        drawPlugin.getMenuManager()
                  .registerMenu(DrawPlugin.TOOLS_MENU, _separator);
        drawPlugin.getMenuManager().registerMenu(DrawPlugin.TOOLS_MENU, _menu);
    }

    private CommandMenu createMenu() {
        _menu = new CommandMenu("Interactions (AIP Diagrams)");
        _menu.add(new ShowPaletteCommand(this), KeyEvent.VK_X,
                  MENU_SHORTCUT_KEY_MASK + KeyEvent.SHIFT_DOWN_MASK);
        _menu.add(new GeneratePeersCommand(this), KeyEvent.VK_G,
                  MENU_SHORTCUT_KEY_MASK + KeyEvent.SHIFT_DOWN_MASK);
        _menu.add(new HideTextCommand("Hide Text"), KeyEvent.VK_MINUS,
                  MENU_SHORTCUT_KEY_MASK + KeyEvent.SHIFT_DOWN_MASK);
        _menu.add(new UnhideTextCommand("Unhide Text"), KeyEvent.VK_EQUALS,
                  MENU_SHORTCUT_KEY_MASK + KeyEvent.SHIFT_DOWN_MASK);
        _menu.add(new ToggleTextCommand("Toggle Text"), KeyEvent.VK_SLASH,
                  MENU_SHORTCUT_KEY_MASK + KeyEvent.SHIFT_DOWN_MASK);
        _menu.addSeparator();
        _menu.add(new VersionInfoCommand(this));
        _menu.putClientProperty(MenuManager.ID_PROPERTY, "de.renew.gui.diagram");

        return _menu;
    }

    /*
     * This class provides the Figures in Diagram figure with its standard inscription.
     */
    private class DiagramFigureCreator implements TextFigureCreator {
        public boolean canCreateDefaultInscription(InscribableFigure figure) {
            if (figure instanceof TaskFigure || figure instanceof VSplitFigure
                        || figure instanceof MessageConnection) {
                return true;
            }
            return false;
        }

        public boolean canCreateFigure(InscribableFigure figure) {
            if (figure instanceof TaskFigure || figure instanceof VSplitFigure
                        || figure instanceof MessageConnection) {
                return true;
            }
            return false;
        }

        public TextFigure createTextFigure(InscribableFigure figure) {
            if (figure instanceof TaskFigure) {
                ActionTextFigure text = new ActionTextFigure();
                text.setFillColor(Color.LIGHT_GRAY);
                text.setFrameColor(Color.BLACK);
                text.setAlignment(TextFigure.CENTER);
                return text;
            }
            return new DiagramTextFigure();
        }

        public String getDefaultInscription(InscribableFigure figure) {
            if (figure instanceof TaskFigure) {
                return "action";
            }
            if (figure instanceof MessageConnection) {
                return "request";
            }
            if (figure instanceof VSplitFigure) {
                return "condition";
            }
            return "_";
        }
    }

    public static IPlugin getCurrent() {
        return PluginManager.getInstance().getPluginByName(PLUGIN_NAME);
    }
}