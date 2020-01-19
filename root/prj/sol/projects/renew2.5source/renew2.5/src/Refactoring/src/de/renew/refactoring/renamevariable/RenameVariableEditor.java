package de.renew.refactoring.renamevariable;

import CH.ifa.draw.figures.TextFigure;

import de.renew.refactoring.edit.TextFigureMatchEditor;
import de.renew.refactoring.util.StringHelper;

import java.util.List;


/**
 * RenameVariableEditor edits {@link VariableNameMatch} objects and returns Void.
 *
 * @author 2mfriedr
 */
class RenameVariableEditor extends TextFigureMatchEditor<VariableNameMatch, Void> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameVariableEditor.class);
    final String _newName;

    RenameVariableEditor(final List<VariableNameMatch> references,
                         final String newName) {
        super(references);
        _newName = newName;
    }

    @Override
    protected Void performEdit(VariableNameMatch match) {
        TextFigure textFigure = match.getTextFigure();
        String text = textFigure.getText();

        int nameStart = match.getStart();
        int nameEnd = match.getEnd();

        // replace variable name
        String newText = StringHelper.replaceRange(text, nameStart, nameEnd,
                                                   _newName);
        logger.debug("Replaced variable: " + newText);
        changeText(match, newText);
        return null;
    }
}