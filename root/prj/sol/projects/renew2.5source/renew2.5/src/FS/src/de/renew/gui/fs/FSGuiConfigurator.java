package de.renew.gui.fs;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.PolyLineFigure;
import CH.ifa.draw.figures.TextTool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.standard.ChangeAttributeCommand;
import CH.ifa.draw.standard.ConnectionTool;

import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.Palette;

import de.renew.gui.CPNTextTool;
import de.renew.gui.GuiPlugin;
import de.renew.gui.JavaGuiCreator;
import de.renew.gui.PaletteHolder;

import javax.swing.JMenuItem;


/**
 * This class configures the Renew user interface when a FS Net Compiler is
 * chosen.
 * <p>
 * </p>
 * Created: Fri Jul  8  2005
 *
 * @author Michael Duvigneau
 * @since Renew 2.1
 **/
public class FSGuiConfigurator extends JavaGuiCreator {
    protected final static String IMAGES = "/de/renew/gui/fs/images/";
    protected final static String MENU_ID_ISATYPE = "de.renew.gui.fs.isatype";

    /**
     * {@inheritDoc}
     *
     * @return a <code>Palette</code> that comprises tools for concepts and
     *         feature structures
     **/
    public Palette createPalette() {
        Palette palette = super.createPalette();
        GuiPlugin starter = GuiPlugin.getCurrent();
        DrawingEditor editor = starter.getDrawingEditor();
        PaletteHolder paletteHolder = starter.getPaletteHolder();
        if (palette == null) {
            palette = new Palette("Information Modeling Tools");
        }

        Tool tool;

        tool = new CPNTextTool(editor, new FSFigure(), false);
        palette.add(paletteHolder.createToolButton(IMAGES + "FS",
                                                   "Feature Structure Tool",
                                                   tool));

        tool = new TextTool(editor, new FSNodeFigure());
        palette.add(paletteHolder.createToolButton(IMAGES + "FSNODE",
                                                   "Feature Structure Node Tool",
                                                   tool));

        tool = new ConnectionTool(editor, new FeatureConnection());
        palette.add(paletteHolder.createToolButton(IMAGES + "FEATURE",
                                                   "Feature Tool", tool));

        tool = new TextTool(editor, new ConceptFigure());
        palette.add(paletteHolder.createToolButton(IMAGES + "CONCEPT",
                                                   "Concept Tool", tool));

        tool = new ConnectionTool(editor, new IsaConnection());
        palette.add(paletteHolder.createToolButton(IMAGES + "DISISA",
                                                   "Disjunctive Is-A Tool", tool));

        tool = new ConnectionTool(editor, new IsaConnection(false));
        palette.add(paletteHolder.createToolButton(IMAGES + "ISA", "Is-A Tool",
                                                   tool));

        tool = new ConnectionTool(editor, new AssocConnection());
        palette.add(paletteHolder.createToolButton(IMAGES + "ASSOC",
                                                   "Association Tool", tool));

        tool = new TextTool(editor, new UMLNoteFigure());
        palette.add(paletteHolder.createToolButton(IMAGES + "UMLNOTE",
                                                   "UML Note Tool", tool));

        tool = new ConnectionTool(editor,
                                  new LineConnection(null, null,
                                                     PolyLineFigure.LINE_STYLE_DASHED));
        palette.add(paletteHolder.createToolButton(IMAGES + "ANCHOR",
                                                   "UML Note Anchor Tool", tool));
        return palette;
    }

    /**
     * {@inheritDoc}
     *
     * @return a menu with FS-specific commands
     **/
    public JMenuItem createMenu() {
        CommandMenu menu = new CommandMenu("FS options");

        JMenuItem javaMenu = super.createMenu();
        menu.add(javaMenu);

        addFSOptions(menu);
        return menu;
    }

    /**
     * Adds formalism-specific options to the simulation menu under the
     * heading "FS Options". This method is called by {@link #createMenu}.
     * Subclasses overriding this method should be call first
     * <code>super.addFSOptions(optionsMenu)</code>.
     * <p>
     * In the <code>FSGuiConfigurator</code>, the global object rendering
     * switch is added to the menu.
     * </p>
     *
     * @param optionsMenu  a <code>CommandMenu</code> where entries may be
     *                     added.
     **/
    protected void addFSOptions(CommandMenu optionsMenu) {
        CommandMenu renderModeMenu = new CommandMenu("Object Rendering");
        renderModeMenu.add(new SetFSRenderModeCommand("Feature Structures",
                                                      false));
        renderModeMenu.add(new SetFSRenderModeCommand("UML Notation", true));
        optionsMenu.add(renderModeMenu);
    }

    /**
     * Registers formalism-specific entries in the attributes menu.
     **/
    public void formalismActivated() {
        super.formalismActivated();
        MenuManager mgr = DrawPlugin.getCurrent().getMenuManager();
        CommandMenu isaTypeMenu = new CommandMenu("Is-A Type");
        isaTypeMenu.add(new ChangeAttributeCommand("disjunctive", "IsaType",
                                                   Boolean.TRUE));
        isaTypeMenu.add(new ChangeAttributeCommand("multiple", "IsaType",
                                                   Boolean.FALSE));
        mgr.registerMenu(DrawPlugin.ATTRIBUTES_MENU, isaTypeMenu,
                         MENU_ID_ISATYPE);
    }

    /**
     * Deregisters formalism-specific entries in the attributes menu.
     **/
    public void formalismDeactivated() {
        MenuManager mgr = DrawPlugin.getCurrent().getMenuManager();
        mgr.unregisterMenu(MENU_ID_ISATYPE);
        super.formalismDeactivated();
    }
}