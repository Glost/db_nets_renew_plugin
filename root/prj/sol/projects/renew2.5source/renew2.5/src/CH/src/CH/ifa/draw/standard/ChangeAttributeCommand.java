/*
 * @(#)ChangeAttributeCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import java.awt.Color;


/**
 * Command to change a named figure attribute.
 */
public class ChangeAttributeCommand extends UndoableCommand {
    private String fAttribute;
    private Object fValue;

    /**
     * Constructs a change attribute command.
     *
     * @param name
     *            the command name
     * @param attributeName
     *            the name of the attribute to be changed
     * @param value
     *            the new attribute value
     */
    public ChangeAttributeCommand(String name, String attributeName,
                                  Object value) {
        super(name);
        fAttribute = attributeName;
        fValue = value;
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        FigureEnumeration k = view.selectionElements();
        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();
            Object val = f.getAttribute(fAttribute);

            //Check if new value is of type Color to save possible transparency values
            if (val instanceof Color && fValue instanceof Color) {
                Color c = (Color) fValue;
                int alphaValue = ((Color) val).getAlpha();
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alphaValue);
                f.setAttribute(fAttribute, c);
            } else {
                f.setAttribute(fAttribute, fValue);
            }
        }
        view.checkDamage();
        return true;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }
}