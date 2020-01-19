package de.renew.gui;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * Sets the display box of the selected figure(s) to
 * a uniform size.
 * <p>
 * </p>
 * ChangeSizeCommand.java
 * Created: Tue Jun 20  2000
 * @author Michael Duvigneau
 **/
public class ChangeSizeCommand extends UndoableCommand {

    /**
     * Mode to specify the size to set:
     * The default size of each figure will be used.
     * This will work only for known figure types,
     * e.g. (currently) place and transition figures.
     **/
    public static final int DEFAULT_SIZE = 1;

    /**
     * Mode to specify the size to set:
     * The size of the first selected figure will be used.
     * This will make sense only if more then one figure
     * is selected.
     **/
    public static final int FIRST_FIGURE = 2;

    /**
     * Mode to specify the size to set:
     * The size given to the constructor will be used.
     * This mode should not be set manually, instead
     * the corresponding constructor will set it.
     **/
    private static final int SPECIFIED = 0;

    // private DrawingEditor editor;
    private int mode;

    /**
     * Contains the size to apply to all figures, if the
     * current mode needs such a size.
     * <p>
     * In <code>FIRST_FIGURE</code> mode this value is
     * recomputed at the beginning of every execution.
     * This is the reason why the <code>execute()</code>
     * method has to be synchronized.
     * </p>
     **/
    private Dimension useSize;

    /**
     * Constructs a change size command calculating
     * the size to be applied at execution time.
     *
     * @param name   the command name to be used in menus
     * @param mode   How to calculate the size to apply.
     *               Valid modes are: DEFAULT_SIZE,
     *                                FIRST_FIGURE.
     *
     * @see #DEFAULT_SIZE
     * @see #FIRST_FIGURE
     **/
    public ChangeSizeCommand(String name, int mode) {
        super(name);
        if (mode == SPECIFIED) {
            throw new RuntimeException("ChangeSizeCommand: Invalid mode.");
        }
        this.mode = mode;
        this.useSize = null;
    }

    /**
     * Constructs a change size command using the
     * specified size.
     *
     * @param name   the command name to be used in menus
     * @param size   the size to apply to all figures
     **/
    public ChangeSizeCommand(String name, Dimension size) {
        super(name);
        // this.editor = editor;
        this.mode = SPECIFIED;
        this.useSize = size;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        if (mode == FIRST_FIGURE) {
            return getEditor().view().selectionCount() > 1;
        } else {
            return getEditor().view().selectionCount() > 0;
        }
    }

    public synchronized boolean executeUndoable() {
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            FigureEnumeration figures = getEditor().view().selectionElements();
            Figure figure;
            Rectangle oldBox;
            Rectangle newBox;
            Point oldLoc;
            Point newLoc;
            Dimension oldDim;
            Dimension newDim;

            if (mode == FIRST_FIGURE) {
                // isExecutable() guarantees that there is a figure.
                // As the first figure serves as template, I don't
                // need to put it back into the enumeration - it has
                // already the correct size by definition.
                // Ergo: Just extract it, no checks, no remembering.
                Figure firstFigure = figures.nextFigure();
                useSize = firstFigure.displayBox().getSize();
            }

            while (figures.hasMoreElements()) {
                figure = figures.nextFigure();
                oldBox = figure.displayBox();
                oldLoc = oldBox.getLocation();
                oldDim = oldBox.getSize();
                newDim = getNewSize(figure);
                if (newDim != null) {
                    newLoc = new Point(oldLoc.x
                                       - (newDim.width - oldDim.width) / 2,
                                       oldLoc.y
                                       - (newDim.height - oldDim.height) / 2);
                    newBox = new Rectangle(newLoc, newDim);
                    figure.displayBox(newBox);
                }
            }

            getEditor().view().checkDamage();
            return true;
        }
        return false;
    }

    /**
     * Returns the size to be applied to the given figure
     * with respect to the current mode.
     * <p>
     * In <code>FIRST_FIGURE</code> mode, the instance variable
     * <code>useSize</code> must contain the actual pivot figure's
     * size, which is set by the <code>execute()</code>
     * implementation.
     * </p>
     **/
    protected Dimension getNewSize(Figure figure) {
        if (mode == DEFAULT_SIZE) {
            if (figure instanceof PlaceFigure) {
                return PlaceFigure.defaultDimension();
            } else if (figure instanceof TransitionFigure) {
                return TransitionFigure.defaultDimension();
            } else {
                return null;
            }
        } else {
            return useSize;
        }
    }
}