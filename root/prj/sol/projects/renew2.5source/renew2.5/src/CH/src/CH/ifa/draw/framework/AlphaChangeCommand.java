package CH.ifa.draw.framework;

import CH.ifa.draw.standard.NullDrawingEditor;

import java.awt.Color;


public class AlphaChangeCommand extends UndoableCommand {
    private String fAttribute;
    private int fValue;

    /**
     * Constructs a change attribute command.
     * @param name the command name
     * @param attributeName the name of the attribute to be changed
     * @param value the new attribute value
     */
    public AlphaChangeCommand(String name, String attributeName, int value) {
        super(name);
        fAttribute = attributeName;
        fValue = value;
    }

    @Override
    protected boolean executeUndoable() {
        DrawingView view = getEditor().view();
        FigureEnumeration k = view.selectionElements();
        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();
            Object val = f.getAttribute(fAttribute);
            if (val instanceof Color) {
                Color c = (Color) val;
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), fValue);
                f.setAttribute(fAttribute, c);
            }
        }
        view.checkDamage();
        return true;
    }

    public boolean isExecutable() {
        if (getEditor() == NullDrawingEditor.INSTANCE) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }
}