package de.renew.refactoring.inline;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.figures.TextTool;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.standard.NullDrawingEditor;
import CH.ifa.draw.standard.TextHolder;

import de.renew.gui.CPNTextFigure;

import de.renew.refactoring.match.StringMatch;

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * Inline step that provides a text field in which only the specified string
 * match is editable. The text field appears upon instantiation and disappears
 * when {@link #endEdit()} is called.
 *
 * @see FloatingRestrictedEditingTextField
 * @author 2mfriedr
 */
public class RestrictedEditingTextFieldStep extends InlineStepWithListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RestrictedEditingTextFieldStep.class);
    private CPNTextFigure _textFigure;
    private Container _container;
    private FloatingRestrictedEditingTextField _textField;
    private String _originalText;
    private String _lastKnownText;
    private String _lastKnownEditedText;
    private int _editCounter;

    /**
     * Creates a RestrictedEditingTextFieldStep.
     *
     * @param container the container in which the text field will appear
     * @param textFigure the text figure that will be edited
     * @param stringMatch the string match for the editable part of the text
     */
    public RestrictedEditingTextFieldStep(final Container container,
                                          final CPNTextFigure textFigure,
                                          final StringMatch stringMatch) {
        _container = container;
        _textFigure = textFigure;
        _originalText = textFigure.getText();
        beginEdit(stringMatch);
    }

    /**
     * Override point for subclasses.
     * This method is called when the editable part of the text field's text
     * is changed.
     *
     * @param text the editable part of the text
     */
    public void editedTextChanged(String text) {
    }

    /**
     * Returns the editable part of the text field's text.
     *
     * @return the editable part of the text
     */
    public String getEditedText() {
        return _textField.getEditedText();
    }

    /**
     * Returns the complete text field's text.
     *
     * @return the text
     */
    public String getText() {
        return _textField.getText();
    }

    /**
     * Override point for subclasses.
     * This method is called to determine a key listener for the text field.
     *
     * @return {@code null}
     */
    protected KeyListener textFieldKeyListener() {
        return null;
    }

    /**
     * Override point for subclasses.
     * This method is called to determine a mouse listener for the container.
     *
     * @return a listener that calls {@link #endEdit()}
     */
    protected MouseListener containerMouseListener() {
        return new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    endEdit();
                }
            };
    }

    /**
     * Sets the text field's border color.
     *
     * @param borderColor the border color
     */
    public void setBorderColor(Color borderColor) {
        _textField.setBorderColor(borderColor);
    }

    /**
     * Based on {@link TextTool#beginEdit(TextHolder)}.
     */
    private void beginEdit(final StringMatch stringMatch) {
        _textField = new FloatingRestrictedEditingTextField(stringMatch.start(),
                                                            stringMatch.end());

        _textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {
                    edited();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    edited();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    edited();
                }
            });

        _textField.addKeyListener(textFieldKeyListener());
        _container.addMouseListener(containerMouseListener());

        setText(_textFigure.getText());

        // select the editable part of the text
        _textField.select(stringMatch.start(), stringMatch.end());

        _textField.createOverlay(_container, fieldBounds(_textFigure),
                                 _textFigure.getFont());
    }

    /**
     * Called by the document listener whenever the text field's text changed.
     */
    private void edited() {
        if (getText().isEmpty()
                    && !(_lastKnownText.equals(_lastKnownEditedText))) {
            // This happens if the text was replaced by
            // #updateTextFieldFromTextFigure(), do nothing in this case.
            return;
        }

        _lastKnownText = getText();
        String newEditedText = (_lastKnownText.isEmpty()) ? "" : getEditedText();

        // If this notification was triggered by #setText(), the variable name
        // didn't change and the notification can be discarded.
        if (newEditedText.equals(_lastKnownEditedText)) {
            return;
        }

        _lastKnownEditedText = newEditedText;
        editedTextChanged(newEditedText);
        updateTextFieldFromTextFigure();

        // Reset the text figure's text so it won't be visible below the 
        // floating text field. The text will be changed again in #endEdit().
        _textFigure.setText(_originalText);
        _textFigure.changed();
    }

    /**
     * Updates the text field from the content of the underlying text figure.
     * This is useful if editing the editable part of the text field triggers a
     * change in the text figure that should be reflected by the text field.
     * {@link SwingUtilities#invokeLater(Runnable)} is needed because
     * the text can't be updated from a notification.
     */
    private void updateTextFieldFromTextFigure() {
        final int thisEdit = ++_editCounter;
        final String oldText = _lastKnownText;
        final String newText = _textFigure.getText();

        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // avoiding a concurrency issue that results in an infinite
                    // editing loop
                    if (thisEdit == _editCounter && !newText.equals(oldText)) {
                        int caret = _textField.getCaretPosition();
                        setText(newText);
                        _textField.setCaretPosition(caret);
                    }
                }
            });
    }

    /**
     * Sets the text field's text and saves it to the member variable {@link
     * #_lastKnownText}.
     *
     * @param text the text
     */
    private void setText(final String text) {
        _textField.setText(text);
        _lastKnownText = text;
    }

    /**
     * Ends the edit. Based on {@link TextTool#endEdit()}. This method removes
     * the text figure if the text field's text is empty, ends the text field
     * overlay and calls the appropriate methods on the editor to signal that
     * editing has ended. Additionally, listeners are informed that the step
     * has finished.
     */
    public void endEdit() {
        String newText = getText();
        _textFigure.setText(newText);
        if (newText.trim().length() == 0) {
            getEditor().drawing().remove(_textFigure);
        }

        _textField.endOverlay();
        getEditor().view().checkDamage();
        getEditor().toolDone();

        informListenersFinished();
    }

    /**
     * Copied from {@link TextTool}
     */
    private static Rectangle fieldBounds(TextHolder figure) {
        Rectangle box = figure.textDisplayBox();
        /*
         * width and height are actually overridden by FloatingTextField.
         * So only x,y coordinates are needed.
         */
        return box;
    }

    private static DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }
}