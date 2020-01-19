package de.renew.refactoring.renamevariable;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.DeclarationFigure;

import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.parse.DeclarationFinder;
import de.renew.refactoring.parse.VariableParser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A RenameVariableRefactoring object holds the state of the refactoring and
 * delegates searching and editing operations to other objects.
 *
 * @author 2mfriedr
 */
class RenameVariableRefactoring {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameVariableRefactoring.class);
    private final VariableParser _parser;
    private final Drawing _drawing;
    private final CPNTextFigure _selectedTextFigure;
    private Variable _selectedVariable;
    private List<VariableNameMatch> _references;
    private String _newName;
    private RenameVariableEditor _editor;
    private Set<Variable> _variablesInSelection;

    RenameVariableRefactoring(VariableParser parser, Drawing drawing,
                              List<Figure> selection)
            throws NoVariableSelectedException {
        _parser = parser;
        _drawing = drawing;
        _selectedTextFigure = findSelectedTextFigure(parser, drawing, selection);
        _variablesInSelection = findVariablesInSelection();
        if (_variablesInSelection.size() == 0) {
            throw new NoVariableSelectedException();
        }
    }

    /**
     * Finds variables in the selection.
     *
     * @return a set of variables
     */
    Set<Variable> getVariablesInSelection() {
        return _variablesInSelection;
    }

    private Set<Variable> findVariablesInSelection() {
        // TODO should this be moved to its own class?
        List<StringMatch> variableMatches = (_selectedTextFigure instanceof DeclarationFigure)
                                            ? _parser
                                                .findVariablesInDeclarationNode()
                                            : _parser.findVariables(_selectedTextFigure
                                                                    .getText());

        Set<Variable> variables = new HashSet<Variable>();
        for (StringMatch match : variableMatches) {
            String name = match.match();
            variables.add(new Variable(name, _parser.findVariableType(name)));
        }
        return variables;
    }

    /**
     * Finds references and and saves them to a member variable.
     *
     * @see RenameVariableReferenceFinder#findReferences()
     * @return a list of variable name matches
     */
    void findReferences() {
        assert _selectedVariable != null;
        _references = new RenameVariableReferenceFinder(_parser, _drawing,
                                                        _selectedVariable
                          .getName()).searchNextItem();
    }

    CPNTextFigure getSelectedTextFigure() {
        return _selectedTextFigure;
    }

    Variable getSelectedVariable() {
        return _selectedVariable;
    }

    String getSelectedVariableName() {
        return _selectedVariable.getName();
    }

    void setSelectedVariable(Variable selectedVariable) {
        _selectedVariable = selectedVariable;
    }

    List<VariableNameMatch> getReferences() {
        return Collections.unmodifiableList(_references);
    }

    /**
     * Finds a reference to the selected variable in the selected text figure.
     * If there is more than one reference, the first is returned.
     *
     * @return a string match object
     */
    StringMatch getSelectedVariableStringMatch() {
        String text = _selectedTextFigure.getText();
        List<StringMatch> variables = (_selectedTextFigure instanceof DeclarationFigure)
                                      ? _parser.findVariablesInDeclarationNode()
                                      : _parser.findVariables(text);

        for (StringMatch variable : variables) {
            if (variable.match().equals(getSelectedVariableName())) {
                return variable;
            }
        }
        return null;
    }

    /**
     * @see {@link RenameVariableEditor#replaceVariables()}
     */
    void replaceVariables() {
        if (_references == null) {
            findReferences();
        }
        _editor = new RenameVariableEditor(_references, _newName);
        _editor.performAllEdits();
    }

    String getNewName() {
        return _newName;
    }

    /**
     * Note: This method allows setting the new variable name even if the
     * parameter is not a valid name. Check with {@link
     * #enteredValidVariableName()}.
     *
     * @param newName the new variable name
     */
    void setNewName(String newName) {
        _newName = newName;
        logger.debug("new name: " + newName);
    }

    boolean enteredValidVariableName() {
        // The old variable name has to be handled seperately because the
        // parser's validity check returns false if there already is a variable
        // with the same name (to avoid duplicate declaration)
        if (_newName.equals(getSelectedVariableName())) {
            return true;
        }
        return _parser.isValidVariableName(_newName);
    }

    /**
     * Resets all text figures with references to the selected variable to the
     * texts they held before renaming.
     *
     * This way, we don't need to look for references again and the search
     * doesn't break if the variable name is completely deleted or changed to a
     * name that is already declared.
     */
    void restorePreviousTexts() {
        if (_editor == null) {
            return;
        }
        _editor.restorePreviousTexts();
    }

    /**
     * Finds the selected text figure based on the specified selection and
     * drawing. If only one text figure is selected, it is returned by this
     * method. Otherwise, the drawing's declaration figure is returned.
     * @param drawing the drawing
     * @param selection the selection
     *
     * @return the selected text figure
     */
    private static CPNTextFigure findSelectedTextFigure(final VariableParser parser,
                                                        final Drawing drawing,
                                                        final List<Figure> selection) {
        RenameVariableSelectionChecker selChecker = new RenameVariableSelectionChecker(selection,
                                                                                       parser);
        if (selChecker.isTextFigureWithVariableSelected()) {
            return (CPNTextFigure) selection.get(0);
        }
        return new DeclarationFinder(drawing).declarationFigure();
    }
}