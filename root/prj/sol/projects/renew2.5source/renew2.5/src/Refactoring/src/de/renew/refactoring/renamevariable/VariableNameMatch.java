package de.renew.refactoring.renamevariable;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNTextFigure;

import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.match.TextFigureMatch;


/**
 * Represents a variable name match within a text figure, i.e. a reference to
 * the variable with the respective name.
 *
 * @author 2mfriedr
 */
public class VariableNameMatch extends TextFigureMatch {
    VariableNameMatch(Drawing drawing, CPNTextFigure textFigure,
                      StringMatch stringMatch) {
        super(drawing, textFigure, stringMatch);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        return (obj instanceof VariableNameMatch);
    }
}