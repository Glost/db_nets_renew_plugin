package de.renew.refactoring.inline;

import CH.ifa.draw.util.FloatingTextField;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;


/**
 * A text field overlay that is used to edit a restricted part (with a start
 * and end index) of a text figure. It has document and navigation filters that
 * are responsible for discarding illegal edits and caret movements.
 *
 * @see FloatingTextField
 * @author 2mfriedr
 */
public class FloatingRestrictedEditingTextField extends FloatingTextField {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FloatingRestrictedEditingTextField.class);
    private final RestrictedEditingFilters _restrict;

    public FloatingRestrictedEditingTextField(int startIndex, int endIndex) {
        super();
        _restrict = new RestrictedEditingFilters(fEditWidget, startIndex,
                                                 endIndex);
    }

    @Override
    public void createOverlay(Container container, Rectangle minimum, Font font) {
        ((AbstractDocument) fEditWidget.getDocument()).setDocumentFilter(_restrict
                                                                         .getDocumentFilter());
        fEditWidget.setNavigationFilter(_restrict.getNavigationFilter());
        super.createOverlay(container, minimum, font);
    }

    /**
     * Returns the text field's document.
     *
     * @return the document
     */
    public Document getDocument() {
        return fEditWidget.getDocument();
    }

    /**
     * Returns the start index of the restricted editing part of the text. It
     * will not change in the lifetime of the text field.
     *
     * @return the start index
     */
    public int getStartIndex() {
        return _restrict.getStartIndex();
    }

    /**
     * <p>Returns the end index of the restricted editing part of the text. It
     * may change in the lifetime of the text field.</p>
     *
     * <p>Example: If the restricted part of "ab" was "b" (start: 1, end: 2)
     * and the user appends "c", the restricted part is "bc" and the end index
     * is 3.</p>
     *
     * @return the end index
     */
    public int getEndIndex() {
        return _restrict.getEndIndex();
    }

    /**
     * Sets the border color of the text field's scroll pane.
     *
     * @param color the border color
     */
    public void setBorderColor(Color color) {
        scrollPane.setBorder(new LineBorder(color));
    }

    /**
     * Returns the edited text, i.e. the text between the start and end indices
     * that the user was allowed to edit.
     *
     * @return the edited text
     */
    public String getEditedText() {
        String newText = fEditWidget.getText();
        try {
            return newText.substring(getStartIndex(), getEndIndex());
        } catch (StringIndexOutOfBoundsException e) {
            // if the text is only a variable name and the variable is removed,
            // the end index is 1 for a short time while the text is empty.
            // try/catch fixes this.
            return "";
        }
    }

    public void setCaretPosition(int index) {
        fEditWidget.select(index, index); // clear selection
        fEditWidget.setCaretPosition(index);
    }

    public int getCaretPosition() {
        return fEditWidget.getCaretPosition();
    }

    public void select(int startIndex, int endIndex) {
        fEditWidget.select(startIndex, endIndex);
    }
}