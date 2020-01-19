package de.renew.tablet;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.util.Palette;

import de.renew.gui.GuiPlugin;
import de.renew.gui.PaletteHolder;

import de.renew.tablet.tools.PTToggleCreationTool;
import de.renew.tablet.tools.ScribbleTool;


/**
 * PaletteCreator.java
 *

 * @author Lawrence Cabac
 * @version 0.2b,  October 2003
 *
 */
public class PaletteCreator {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PaletteCreator.class);

    /**
    * The panel that contains the palettes / ComponentsTools.
    */


    //private static final String IMAGES = "images/";


    /**
     * The palette that holds the tool buttons of this PaletteCreator.
     */
    private Palette palette;

    /**
     * The signifier of this PaletteCreator.
     */
    private String label;

    /**
     * Constructor for PaletteCreator. It has to know the  CPN application.
     * The tools directory is determined by the "user.toolsdir" java  commandlind argument.
     *
     * @param  name - the CPNApplication to which the palette should be added
     */
    public PaletteCreator(String name) {
        create(name);
    }

    /**
     * Method createTools.
     *
     * @param paletteName - the tools directory name.
     */
    void create(String paletteName) {
        GuiPlugin starter = GuiPlugin.getCurrent();
        PaletteHolder paletteHolder = starter.getPaletteHolder();

        //        toolPanel = cpnapp.getToolsPanel();
        //        menuFrame = cpnapp.menuFrame();
        String IMAGES = "/de/renew/tablet/images/";


        //this.getClass()
        //                            .getResource("").toString();
        //                            "/de/renew/diagram/images/"
        //                            .toString().substring(5);
        // create tools for Net-Components
        // logger.debug(IMAGES);
        Tool tool;

        palette = new Palette(paletteName);
        DrawingEditor editor = starter.getDrawingEditor();


        tool = new ScribbleTool(editor);
        palette.add(paletteHolder.createToolButton(IMAGES + "scribble",
                                                   "tablet scribble", tool));
        tool = new PTToggleCreationTool(editor);
        palette.add(paletteHolder.createToolButton(IMAGES + "toggle",
                                                   "toggle p / t", tool));

        paletteHolder.addPalette(palette);


        // cpnapp.menuFrame().pack();
        //  end of elements for diagram drawing figures                              
        // start of decoration firgures
        // using the 
    }

    // end of create()


    /**
     * Method remove.
    * Removes the PaletteCreator form the toosPanel.
     */
    public void remove() {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter == null) {
            return;
        }
        starter.getPaletteHolder().removePalette(palette);
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return getLabel();
    }
}