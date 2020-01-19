package de.renew.refactoring.wizard;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.CPNApplication;
import de.renew.gui.GuiPlugin;

import java.util.Enumeration;


/**
 * Provides static methods to show a drawing in the gui.
 *
 * @author 2mfriedr
 */
class DrawingOpener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(DrawingOpener.class);

    /**
     * Opens the specified drawing.
     *
     * @param drawing the drawing
     */
    static void open(final Drawing drawing) {
        open(drawing, null);
    }

    /**
     * Opens the specified drawing and selects the specified figure.
     *
     * @param drawing the drawing
     * @param figure the figure to be selected
     */
    static void open(final Drawing drawing, final FigureWithID figure) {
        // FIXME when opening a drawing for the second time, all text figures
        // disappear.
        // DrawPlugin.getGui().openOrLoadDrawing(file.getPath());
        CPNApplication gui = GuiPlugin.getCurrent().getGui();
        int id = (figure != null) ? figure.getID() : FigureWithID.NOID;

        // assuming unique drawing names
        boolean isDrawingLoaded = false;
        Enumeration<Drawing> guiDrawings = gui.drawings();
        while (guiDrawings.hasMoreElements()) {
            String guiDrawingName = guiDrawings.nextElement().getName();
            if (drawing.getName().equals(guiDrawingName)) {
                isDrawingLoaded = true;
                break;
            }
        }

        // make sure the drawing is not opened twice
        if (!isDrawingLoaded) {
            gui.openDrawing(drawing);
        }
        gui.openNetPatternDrawing(drawing.getName(), id);
    }
}