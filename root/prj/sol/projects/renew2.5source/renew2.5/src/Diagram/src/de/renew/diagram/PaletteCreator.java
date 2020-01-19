package de.renew.diagram;

import CH.ifa.draw.figures.ConnectedTextTool;
import CH.ifa.draw.figures.RoundRectangleFigure;
import CH.ifa.draw.figures.TextFigure;
import CH.ifa.draw.figures.TextTool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.standard.CreationTool;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.Palette;

import de.renew.dcdiagram.DCAnswerMessageConnection;
import de.renew.dcdiagram.DCExchangeMessageConnection;
import de.renew.dcdiagram.DCTaskFigure;
import de.renew.dcdiagram.DCTaskFigureCreationTool;

import de.renew.gui.GuiPlugin;
import de.renew.gui.PaletteHolder;
import de.renew.gui.fs.UMLNoteFigure;

import java.awt.Color;


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

        //NOTICEredundant
        if (starter == null) {
            logger.error("PaletteCreator: no GuiPlugin available");
            return;
        }
        PaletteHolder paletteHolder = starter.getPaletteHolder();

        //        toolPanel = cpnapp.getToolsPanel();
        //        menuFrame = cpnapp.menuFrame();
        String IMAGES = "/de/renew/diagram/images/";


        //this.getClass()
        //                            .getResource("").toString();
        //                            "/de/renew/diagram/images/"
        //                            .toString().substring(5);
        // create tools for Net-Components
        // logger.debug(IMAGES);
        Tool tool;

        palette = new Palette(paletteName);
        DrawingEditor editor = starter.getDrawingEditor();


        tool = new RoleDescriptorFigureCreationTool(editor);
        palette.add(paletteHolder.createToolButton(IMAGES + "role",
                                                   "Role Descriptor Figure Tool",
                                                   tool));

        tool = new TaskFigureCreationTool(editor);
        palette.add(paletteHolder.createToolButton(IMAGES + "task",
                                                   "Task Figure Tool", tool));
        ActionTextFigure prototype2 = new ActionTextFigure();
        prototype2.setFillColor(Color.LIGHT_GRAY);
        prototype2.setFrameColor(Color.BLACK);
        prototype2.setAlignment(TextFigure.CENTER);
        tool = new ConnectedTextTool(editor, prototype2);
        palette.add(paletteHolder.createToolButton(IMAGES + "action",
                                                   "Action Tool", tool));
        DCServiceTextFigure prototype3 = new DCServiceTextFigure();
        prototype3.setFillColor(new Color(150, 250, 150));
        prototype3.setFrameColor(Color.BLACK);
        prototype3.setAlignment(TextFigure.CENTER);
        tool = new ConnectedTextTool(editor, prototype3);
        palette.add(paletteHolder.createToolButton(IMAGES + "exchange",
                                                   "DC exchange tool", tool));


        // logger.debug("the prototype of the connection: "+ MessageConnection.NormalArc);                         
        tool = new MessageConnectionTool(editor, new MessageConnection());
        palette.add(paletteHolder.createToolButton(IMAGES + "mess",
                                                   "Message Tool", tool));

        tool = new ConnectionTool(editor, new LifeLineConnection(1));
        palette.add(paletteHolder.createToolButton(IMAGES + "life",
                                                   "Life Line Tool", tool));

        //      no semantics for or available
        //        tool = new SplitFigureCreationTool(editor, true, new ORDecoration());
        //        palette.add(paletteHolder.createToolButton(IMAGES + "osplit",
        //                                             "OR Split Tool", tool));
        tool = new SplitFigureCreationTool(editor, true, new XORDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "xsplit",
                                                   "XOR Lifeline Split Tool",
                                                   tool));

        tool = new SplitFigureCreationTool(editor, true, new ANDDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "asplit",
                                                   "AND Lifeline Split Tool",
                                                   tool));
        //        tool = new SplitFigureCreationTool(editor, false, new ORDecoration());
        //        palette.add(paletteHolder.createToolButton(IMAGES + "ojoin",
        //                                             "OR Message Join Tool", tool));
        tool = new SplitFigureCreationTool(editor, false, new XORDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "xjoin",
                                                   "XOR Message Join Tool", tool));

        tool = new SplitFigureCreationTool(editor, false, new ANDDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "ajoin",
                                                   "AND Message Join Tool", tool));

        tool = new JoinFigureCreationTool(editor, true, new XORDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "xmerge",
                                                   "XOR Lifeline Merge Tool",
                                                   tool));

        tool = new JoinFigureCreationTool(editor, true, new ANDDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "amerge",
                                                   "AND Lifeline Merge Tool",
                                                   tool));

        UMLNoteFigure umlfigure = new UMLNoteFigure(new java.awt.Color(64, 64,
                                                                       64));
        umlfigure.setTextColor(Color.BLUE);
        tool = new TextTool(editor, umlfigure);
        palette.add(paletteHolder.createToolButton(IMAGES + "UMLNOTE",
                                                   "UML Note Tool", tool));

        RoundRectangleFigure rrf = new DiagramFrameFigure();
        rrf.setFillColor(ColorMap.color("None"));
        rrf.setArc(72, 72);

        tool = new CreationTool(editor, rrf);
        palette.add(paletteHolder.createToolButton(IMAGES + "frame",
                                                   "Diagram Frame Tool", tool));


        DiagramTextFigure prototype = new DiagramTextFigure(false);
        prototype.setAlignment(TextFigure.LEFT);
        tool = new TextTool(editor, prototype);

        palette.add(paletteHolder.createToolButton("/CH/ifa/draw/images/"
                                                   + "TEXT",
                                                   "Diagram Text Tool", tool));

        prototype = new DiagramTextFigure();
        prototype.setAlignment(TextFigure.CENTER);
        tool = new ConnectedTextTool(editor, prototype);
        palette.add(paletteHolder.createToolButton("/CH/ifa/draw/images/"
                                                   + "ATEXT",
                                                   "Diagram Connected Text Tool",
                                                   tool));


        tool = new DCTaskFigureCreationTool(editor);
        palette.add(paletteHolder.createToolButton(IMAGES + "dctask",
                                                   "DCTask Figure Tool", tool));

        tool = new MessageConnectionTool(editor,
                                         new DCExchangeMessageConnection());
        palette.add(paletteHolder.createToolButton(IMAGES + "dcmess",
                                                   "DCExchange Message Tool",
                                                   tool));

        tool = new MessageConnectionTool(editor, new DCAnswerMessageConnection());
        palette.add(paletteHolder.createToolButton(IMAGES + "dcansw",
                                                   "DCAnswer Message Tool", tool));
        //         MiniDiamondFigure mdf = new MiniDiamondFigure();
        //         mdf.setFillColor(ColorMap.color("White"));
        //         tool = new CreationTool(cpnapp, mdf);
        //         palette.add(cpnapp.createToolButton( IMAGES  +  "decoor", 
        //                                              "Split Decoration OR",
        //                                              tool));
        //         mdf = new MiniDiamondFigure();
        //         mdf.setFillColor(ColorMap.color("Black"));
        //         tool = new CreationTool(cpnapp, mdf);
        //         palette.add(cpnapp.createToolButton( IMAGES  +  "decoand", 
        //                                              "Split Decoration AND",
        //                                              tool));
        //        Debug.print ("toolpanel is " + toolPanel + ", palette is " + palette);
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