package de.renew.fa;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.ConnectedTextTool;
import CH.ifa.draw.figures.PolyLineFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.standard.ConnectionTool;

import CH.ifa.draw.util.Palette;

import de.renew.fa.figures.EndDecoration;
import de.renew.fa.figures.FAArcConnection;
import de.renew.fa.figures.FAFigureCreationTool;
import de.renew.fa.figures.FALoopArcConnectionCreationTool;
import de.renew.fa.figures.FATextFigure;
import de.renew.fa.figures.StartDecoration;
import de.renew.fa.figures.StartEndDecoration;

import de.renew.gui.CPNApplication;
import de.renew.gui.GuiPlugin;
import de.renew.gui.PaletteHolder;


/**
 * PaletteCreator.java
 *
 *
 * @author Lawrence Cabac
 * @version 0.2b, October 2003
 *
 */
public class AltPaletteCreator extends PaletteCreator {

    /**
    * This is a creator for <i>one</i> tool {@link Palette} which uses
    * the international style for state representation.
    *
    * <p> For creating the tool with Matthias Jantzen style for
    * tate representation, make use of the {@link PaletteCreator}. </p>
    *
    * <p> The palette uses images taken from <tt>/de/renew/fa/images/</tt>
    * </p>
    *
    * @see  Palette
    * @see  PaletteCreator
    */
    public AltPaletteCreator(String name) {
        super(name);
    }

    @Override
    protected void createPalette(String paletteName) {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter == null) {
            logger.error("No GuiPlugin available");
            return;
        }

        // setup palette for adding tools
        String IMAGES = "/de/renew/fa/images/";
        Tool tool;
        PaletteHolder paletteHolder = starter.getPaletteHolder();
        palette = new Palette(paletteName);
        DrawingEditor editor = starter.getDrawingEditor();


        // add state tools
        tool = new FAFigureCreationTool(editor, new StartDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "fa_state_s_alt",
                                                   "FA Start State Tool", tool));

        tool = new FAFigureCreationTool(editor);
        palette.add(paletteHolder.createToolButton(IMAGES + "fa_state",
                                                   "FA State Tool", tool));

        tool = new FAFigureCreationTool(editor, new EndDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "fa_state_e_alt",
                                                   "FA End State Tool", tool));

        tool = new FAFigureCreationTool(editor, new StartEndDecoration());
        palette.add(paletteHolder.createToolButton(IMAGES + "fa_state_se_alt",
                                                   "FA Start End State Tool",
                                                   tool));


        // add text tools
        FATextFigure prototype = FATextFigure.Name;
        prototype.setAlignment(TextFigure.CENTER);
        tool = new ConnectedTextTool(editor, prototype);
        palette.add(paletteHolder.createToolButton(CPNApplication.CPNIMAGES
                                                   + "NAME", "FA Name Tool",
                                                   tool));

        prototype = FATextFigure.Inscription;
        prototype.setAlignment(TextFigure.CENTER);
        tool = new ConnectedTextTool(editor, prototype);
        palette.add(paletteHolder.createToolButton(CPNApplication.CPNIMAGES
                                                   + "INSCR",
                                                   "FA Inscription Tool", tool));

        // add arc tools
        FAArcConnection arc = new FAArcConnection(null, new ArrowTip(),
                                                  AttributeFigure.LINE_STYLE_NORMAL);
        Object spline = new Integer(PolyLineFigure.BSPLINE_SHAPE);
        arc.setAttribute("LineShape", spline);
        tool = new ConnectionTool(editor, arc);
        palette.add(paletteHolder.createToolButton("/de/renew/gui/images/"
                                                   + "ARC",
                                                   "FA ArcConnection Tool", tool));

        tool = new FALoopArcConnectionCreationTool(editor);
        palette.add(paletteHolder.createToolButton(IMAGES + "fa_loop",
                                                   "FA Loop ArcConnection Tool",
                                                   tool));


        paletteHolder.addPalette(palette);

    }
}