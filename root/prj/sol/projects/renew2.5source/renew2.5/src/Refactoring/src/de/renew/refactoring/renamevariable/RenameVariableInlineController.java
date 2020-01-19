package de.renew.refactoring.renamevariable;

import CH.ifa.draw.framework.Figure;

import de.renew.gui.CPNDrawingView;
import de.renew.gui.CPNTextFigure;

import de.renew.refactoring.inline.InlineControllerWithListener;
import de.renew.refactoring.inline.InlineStep;
import de.renew.refactoring.inline.InlineStepListener;
import de.renew.refactoring.inline.PopupMenuStep;
import de.renew.refactoring.inline.RestrictedEditingTextFieldStep;
import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.parse.VariableParser;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;


/**
 * Inline controller for Rename Variable.
 *
 * @author 2mfriedr
 */
public class RenameVariableInlineController extends InlineControllerWithListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameVariableInlineController.class);
    private static enum State {STARTED,
        SELECTED_VARIABLE,
        RENAMING,
        FINISHED;
    }
    private State _state = State.STARTED;
    private final RenameVariableRefactoring _refactoring;
    private final CPNDrawingView _container;
    private RestrictedEditingTextFieldStep _textFieldStep;
    private Vector<Figure> _referencingFigures;

    public RenameVariableInlineController(final VariableParser parser,
                                          final CPNDrawingView container)
            throws NoVariableSelectedException {
        _container = container;
        _refactoring = new RenameVariableRefactoring(parser,
                                                     container.drawing(),
                                                     container.selection());
        nextStep();
    }

    @Override
    public InlineStep nextStep() {
        if (_state == State.STARTED) {
            List<Variable> variables = new ArrayList<Variable>(_refactoring
                                           .getVariablesInSelection());
            if (variables.size() == 1) {
                // If there is only one variable, it is automatically selected
                // to be renamed. Advance the state to SELECTED_VARIABLE.
                _refactoring.setSelectedVariable(variables.get(0));
                _state = State.SELECTED_VARIABLE;
            } else {
                // Otherwise, a step to select the variable is needed.
                Point origin = _refactoring.getSelectedTextFigure().center();
                Collections.sort(variables); // sort by type
                return makeSelectVariableStep(origin, variables);
            }
        }
        if (_state == State.SELECTED_VARIABLE) {
            _state = State.RENAMING;
            _refactoring.findReferences();
            _textFieldStep = makeEditVariableNameStep();
            return _textFieldStep;
        }
        if (_state == State.RENAMING) {
            _state = State.FINISHED;

            _textFieldStep.endEdit();
            _textFieldStep = null;

            if (!_refactoring.enteredValidVariableName()) {
                _refactoring.restorePreviousTexts();
            }

            _container.selection().removeAll(_referencingFigures);
            _container.checkDamage();

            informListenersFinished();
        }
        return null;
    }

    private PopupMenuStep<Variable> makeSelectVariableStep(final Point origin,
                                                           final List<Variable> variables) {
        PopupMenuStep<Variable> step = new PopupMenuStep<Variable>(_container,
                                                                   origin,
                                                                   variables) {
            @Override
            public String titleForEntry(Variable entry) {
                return "Rename " + entry;
            }

            @Override
            public ActionListener actionListenerForEntry(Variable entry) {
                return new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _state = State.SELECTED_VARIABLE;
                            JMenuItem item = (JMenuItem) e
                                           .getSource();
                            Variable selected = getEntryForMenuItem(item);
                            _refactoring.setSelectedVariable(selected);
                            nextStep();
                        }
                    };
            }
        };
        step.addListener(new InlineStepListener() {
                @Override
                public void inlineStepFinished() {
                }

                @Override
                public void inlineStepCancelled() {
                    informListenersFinished();
                }
            });
        return step;
    }

    private RestrictedEditingTextFieldStep makeEditVariableNameStep() {
        StringMatch variableNameMatch = _refactoring
                                            .getSelectedVariableStringMatch();
        CPNTextFigure selectedTextFigure = _refactoring.getSelectedTextFigure();

        // Add the referencing text figures to the selection
        _referencingFigures = new Vector<Figure>();
        for (VariableNameMatch match : _refactoring.getReferences()) {
            _referencingFigures.add(match.getTextFigure());
        }
        _container.addToSelectionAll(_referencingFigures);
        _container.checkDamage();

        return new RestrictedEditingTextFieldStep(_container,
                                                  selectedTextFigure,
                                                  variableNameMatch) {
                @Override
                public void editedTextChanged(String text) {
                    _refactoring.setNewName(text);

                    updateTextFieldBorderColor();

                    _refactoring.restorePreviousTexts();
                    _refactoring.replaceVariables();
                }

                @Override
                protected KeyListener textFieldKeyListener() {
                    // Pressing CTRL+Enter to end editing is only allowed if the entered
                    // variable name is valid.
                    return new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent keyEvent) {
                                if (keyEvent.isControlDown()
                                            && keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                                    if (_refactoring.enteredValidVariableName()) {
                                        nextStep();
                                    }
                                }
                            }
                        };
                }

                @Override
                protected MouseListener containerMouseListener() {
                    // Clicking outside the text field ends editing even if the entered
                    // variable name was invalid to avoid display artifacts. In this
                    // case, all text figures are reset to their previous texts.
                    return new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (_state == State.RENAMING) {
                                    nextStep();
                                }
                            }
                        };
                }
            };
    }

    /**
     * Sets the text field's border color to black if the entered variable name
     * is valid and red if the variable name is invalid.
     */
    private void updateTextFieldBorderColor() {
        Color borderColor = (_refactoring.enteredValidVariableName())
                            ? Color.BLACK : Color.RED;
        if (_textFieldStep != null) {
            _textFieldStep.setBorderColor(borderColor);
        }
    }
}