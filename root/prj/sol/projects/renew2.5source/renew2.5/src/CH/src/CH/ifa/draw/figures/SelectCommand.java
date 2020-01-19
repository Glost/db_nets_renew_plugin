/*
 * @(#)SelectCommand.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureFilter;

import CH.ifa.draw.util.Command;

import java.util.HashSet;
import java.util.Vector;


/**
 * Command to select certain Figures of the current drawing.
 *
 * @see CH.ifa.draw.standard.SelectionTool
 */
public class SelectCommand extends Command {

    /** Value for the mode parameter of some constructors:
     * The select command behaves in its 'classical' way,
     * it clears any previous selection.
     * This mode is the default if constructors without
     * a mode parameter are used.
     **/
    public static final int SELECT = 0;

    /** Value for the mode parameter of some constructors:
     * The select command adds the specified figures to
     * the selection.
     **/
    public static final int ADD = 1;

    /** Value for the mode parameter of some constructors:
     * The select command removes the specified figures from
     * the selection.
     **/
    public static final int REMOVE = 2;

    /** Value for the mode parameter of some constructors:
     * The select command restricts the selection to the specified figures.
     **/
    public static final int RESTRICT = 3;

    /** Value for the mode parameter of some constructors:
     * The select command inverts the selection.
     **/
    public static final int INVERT = 4;
    protected DrawingEditor fEditor;
    protected FigureFilter fFilter;
    protected int selectMode;

    /**
     * Constructs a select all command.
     * @param name the command name
     */
    public SelectCommand(String name) {
        this(name, Figure.class, null, SELECT);
    }

    /**
     * Constructs a select command for certain figures.
     * @param name the command name
     * @param figureClass the class of figures to be selected
     */
    public SelectCommand(String name, Class<?> figureClass) {
        this(name, figureClass, SELECT);
    }

    /**
     * Constructs a select/add to selection/remove from selection
     * command for certain figures.
     * @param name the command name
     * @param figureClass the class of figures to be selected
     * @param selectMode one of the constants SELECT, ADD, REMOVE
     *           or RESTRICT defined in this class
     */
    public SelectCommand(String name, Class<?> figureClass, int selectMode) {
        this(name, figureClass, null, selectMode);
    }

    /**
     * Constructs a select command for certain figures.
     * @param name the command name
     * @param figureClass the class of figures to be selected
     * @param parentClass also check this class for the
     *           parent of the child figures to be selected.
     *           if parentClass != null, figureClass has to be
     *           a subclass of ChildFigure and parentClass should
     *           be a subclass of ParentFigure.
     */
    public SelectCommand(String name, final Class<?> figureClass,
                         final Class<?> parentClass) {
        this(name, figureClass, parentClass, SELECT);
    }

    /**
     * Constructs a select/add to selection/remove from selection
     * command for certain figures.
     * @param name the command name
     * @param figureClass the class of figures to be selected
     * @param parentClass also check this class for the
     *           parent of the child figures to be selected.
     *           if parentClass != null, figureClass has to be
     *           a subclass of ChildFigure and parentClass should
     *           be a subclass of ParentFigure.
     * @param selectMode one of the constants SELECT, ADD, REMOVE
     *           or RESTRICT defined in this class
     */
    public SelectCommand(String name, final Class<?> figureClass,
                         final Class<?> parentClass, int selectMode) {
        this(name,
             new FigureFilter() {
                public boolean isUsed(Figure fig) {
                    return figureClass.isInstance(fig)
                           && (parentClass == null
                              || parentClass.isInstance(((ChildFigure) fig)
                                     .parent()));
                }
            }, selectMode);
    }

    /**
     * Constructs a select command for certain figures.
     * @param name the command name
     * @param filter the filter object that decides whether a figure
     *           will be selected
     */
    public SelectCommand(String name, FigureFilter filter) {
        this(name, filter, SELECT);
    }

    /**
     * Constructs a select/add to selection/remove from selection
     * command for certain figures.
     * @param name the command name
     * @param filter the filter object that decides whether a figure
     *           will be selected
     * @param selectMode one of the constants SELECT, ADD, REMOVE
     *           or RESTRICT defined in this class
     */
    public SelectCommand(String name, FigureFilter filter, int selectMode) {
        super(name);
        // getEditor() = editor;
        fFilter = filter;
        this.selectMode = selectMode;
    }

    public void execute() {
        DrawingView view = DrawPlugin.getCurrent().getDrawingEditor().view();
        Drawing drawing = view.drawing();

        FigureEnumeration figenumeration = drawing.figures();
        Vector<Figure> concerned = new Vector<Figure>();

        FigureEnumeration selected = view.selectionElements();
        HashSet<Figure> selectedSet = new HashSet<Figure>();
        while (selected.hasMoreElements()) {
            selectedSet.add(selected.nextFigure());
        }

        while (figenumeration.hasMoreElements()) {
            Figure fig = figenumeration.nextFigure();
            if (selectMode != INVERT) {
                if (fFilter.isUsed(fig)) {
                    if (selectMode != RESTRICT) {
                        concerned.addElement(fig);
                    }
                } else {
                    if (selectMode == RESTRICT) {
                        concerned.addElement(fig);
                    }
                }
            } else {
                if (!(selectedSet.contains(fig))) {
                    concerned.addElement(fig);
                }
            }
        }

        switch (selectMode) {
        case REMOVE:
        case RESTRICT:
            view.removeFromSelectionAll(concerned);
            break;
        case SELECT:
        case INVERT:
            view.clearSelection();
        default:
            view.addToSelectionAll(concerned);
        }

        view.checkDamage();
    }
}