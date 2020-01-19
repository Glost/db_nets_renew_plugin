/*
 * @(#)TextTool.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.CreationTool;
import CH.ifa.draw.standard.TextHolder;

import CH.ifa.draw.util.FloatingTextField;

import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;


/**
 * Tool to create new or edit existing text figures.
 * The editing behavior is implemented by overlaying the
 * Figure providing the text with a FloatingTextField.<p>
 * A tool interaction is done once a Figure that is not
 * a TextHolder is clicked.
 *
 * @see TextHolder
 * @see FloatingTextField
 */
public class TextTool extends CreationTool {
    protected FloatingTextField fTextField;
    private TextHolder fTypingTarget;

    public TextTool(DrawingEditor editor, TextFigure prototype) {
        super(editor, prototype);
    }

    /**
     * If the pressed figure is a TextHolder it can be edited otherwise
     * a new text figure is created.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        Figure pressedFigure;
        TextHolder textHolder = null;

        pressedFigure = drawing().findFigureInside(x, y);
        if (pressedFigure instanceof TextHolder) {
            textHolder = (TextHolder) pressedFigure;
            if (!textHolder.acceptsTyping()) {
                textHolder = null;
            }
        }
        if (textHolder != null) {
            beginEdit(textHolder);
            return;
        }
        if (fTypingTarget != null) {
            endEdit();
            editor().toolDone();
        } else {
            super.mouseDown(e, x, y);
            textHolder = (TextHolder) createdFigure();
            beginEdit(textHolder);
        }
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
    }

    public void mouseUp(MouseEvent e, int x, int y) {
    }

    /**
     * Terminates the editing of a text figure.
     */
    public void deactivate() {
        super.deactivate();
        endEdit();
    }

    /**
     * Sets the text cursor.
     */
    public void activate() {
        super.activate();


        // JDK1.1 TEXT_CURSOR has an incorrect hot spot
        //view.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    protected boolean isTypingActive() {
        return fTypingTarget != null;
    }

    /** Creates a <code>FloatingTextField</code> in order to enter a text for
     *  a text figure.
     *
     * @param figure - the figure, which holds the text.
     */
    public void beginEdit(TextHolder figure) {
        if (fTextField == null) {
            fTextField = new FloatingTextField();
            // listen for a text editing finish event (Ctrl-ENTER).
            fTextField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent keyEvent) {
                        if (keyEvent.isControlDown()
                                    && keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                            deactivate();
                            editor().toolDone();
                        }
                    }
                });
        }
        if (figure != fTypingTarget && fTypingTarget != null) {
            endEdit();
        }
        fTextField.setText(figure.getText());
        fTextField.createOverlay((Container) view(), fieldBounds(figure),
                                 figure.getFont());
        fTypingTarget = figure;
    }

    public void setCaretPosition(int line, int col) {
        fTextField.setCaretPosition(line, col);
    }

    public void select(int startLine, int startColumn, int endLine,
                       int endColumn) {
        fTextField.select(startLine, startColumn, endLine, endColumn);
    }

    public void endEdit() {
        if (fTypingTarget != null) {
            String newText = fTextField.getText();
            String oldText = fTypingTarget.getText();
            fTypingTarget.setText(newText);
            if (newText.trim().length() == 0) {
                drawing().remove((Figure) fTypingTarget);
            }
            fTypingTarget = null;
            fTextField.endOverlay();
            view().checkDamage();
            if (!oldText.equals(newText)) {
                changesMade();
                intermediateUndoSnapshot();
            } else {
                noChangesMade();
            }
        }
    }

    private Rectangle fieldBounds(TextHolder figure) {
        Rectangle box = figure.textDisplayBox();
        /*
         * width and height are actually overridden by FloatingTExtField.
         * So only x,y coordinates are needed.
         */
        return box;
    }
}