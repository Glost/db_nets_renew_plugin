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

import java.net.URI;


/**
 * Tool to create new or edit existing target locations for figures.
 * The editing behavior is implemented by overlaying the
 * Figure providing the text with a FloatingTextField.<p>
 *
 * @see FloatingTextField
 */
public class TargetTool extends CreationTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TargetTool.class);
    private static final String TARGET_LOCATION = "targetLocation";
    protected FloatingTextField fTextField;
    private Figure fTypingTarget;

    public TargetTool(DrawingEditor editor) {
        super(editor, new TextFigure(false));
    }

    /**
     * A new text figure is created with the current target location.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        TextFigure textFigure = null;

        if (fTypingTarget != null) {
            endEdit();
            editor().toolDone();
        } else {
            fTypingTarget = drawing().findFigureInside(x, y);
            if (fTypingTarget != null) {
                String targetLocation = (String) fTypingTarget.getAttribute(TARGET_LOCATION);
                super.mouseDown(e, x, y);
                textFigure = (TextFigure) createdFigure();
                if (targetLocation != null) {
                    textFigure.setText(targetLocation);
                } else {
                    textFigure.setText("");
                }
//                textFigure.displayBox(new Rectangle(x,y,100,15));      
                beginEdit(textFigure);
            }
        }
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
    }

    public void mouseUp(MouseEvent e, int x, int y) {
    }

    /**
     * Terminates the editing of target location.
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
    }

    protected boolean isTypingActive() {
        return fTypingTarget != null;
    }

    /** Creates a <code>FloatingTextField</code> in order to enter a text for
     *  the target location of a figure.
     *
     * @param figure - a figure, which holds the target location text.
     */
    public void beginEdit(TextFigure figure) {
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
        fTextField.setText(figure.getText());
        fTextField.createOverlay((Container) view(), fieldBounds(figure),
                                 figure.getFont());
        view().remove(figure);
    }

    public void setCaretPosition(int line, int col) {
        fTextField.setCaretPosition(line, col);
    }

    public void endEdit() {
        if (fTypingTarget != null) {
            String newText = fTextField.getText();
            String oldText = (String) fTypingTarget.getAttribute(TARGET_LOCATION);
            if (newText.equals("")) { //remove current target location
                fTypingTarget.setAttribute(TARGET_LOCATION, null);
            } else {
                try {
                    if (!newText.startsWith("sim")) {
                        URI uri = new URI(newText); //check if it is an URI
                        if (uri.isAbsolute()) {
                            uri.toURL(); //check if it is an absolute URI
                        }
                    }
                    fTypingTarget.setAttribute(TARGET_LOCATION, newText);
                } catch (Exception e3) {
                    logger.error("Renaming link failed for : " + newText);
                    logger.debug(e3);
                }
            }

            fTypingTarget = null;
            fTextField.endOverlay();
            view().checkDamage();
            if (oldText != null) {
                if (!oldText.equals(newText)) {
                    changesMade();
                    intermediateUndoSnapshot();
                } else {
                    noChangesMade();
                }
            } else if (!newText.equals("")) {
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