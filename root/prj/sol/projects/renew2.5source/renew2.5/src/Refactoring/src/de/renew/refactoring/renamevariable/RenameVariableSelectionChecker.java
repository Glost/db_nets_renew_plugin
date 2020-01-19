package de.renew.refactoring.renamevariable;

import CH.ifa.draw.framework.Figure;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.DeclarationFigure;

import de.renew.refactoring.parse.VariableParser;

import java.util.List;


/**
 * Checks the selection to find text figures with variables.
 *
 * @author 2mfriedr
 */
class RenameVariableSelectionChecker {
    private final List<Figure> _selection;
    private final VariableParser _parser;

    RenameVariableSelectionChecker(final List<Figure> selection,
                                   final VariableParser parser) {
        _selection = selection;
        _parser = parser;
    }

    /**
     * Checks if a text figure is selected and tries to find a variable
     * in the text figure's text using the parser.
     *
     * @return {@code true} if a text figure with a variable is selected,
     * otherwise {@code false}
     */
    boolean isTextFigureWithVariableSelected() {
        if (isTextFigureOtherThanDeclarationNodeSelected()) {
            String text = ((CPNTextFigure) _selection.get(0)).getText();
            return _parser.containsVariable(text);
        }
        return false;
    }

    /**
     * Checks if a text figure is selected.
     *
     * @return {@code true} if a text figure is selected, otherwise {@code
     * false}
     */
    boolean isTextFigureSelected() {
        return (_selection.size() == 1
               && (_selection.get(0) instanceof CPNTextFigure));
    }

    private boolean isTextFigureOtherThanDeclarationNodeSelected() {
        return (_selection.size() == 1
               && (_selection.get(0) instanceof CPNTextFigure)
               && !(_selection.get(0) instanceof DeclarationFigure));
    }
}