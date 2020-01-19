package de.renew.refactoring.inline;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;


/**
 * Editing filters that result in only a part of a text field's text being
 * navigable and editable.
 *
 * @author 2mfriedr
 */
public class RestrictedEditingFilters {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RestrictedEditingFilters.class);
    private JTextComponent _textField;
    private final int _startIndex;
    private int _endIndex;
    private RestrictedEditingNavigationFilter _navigationFilter;
    private RestrictedEditingDocumentFilter _documentFilter;

    public RestrictedEditingFilters(JTextComponent textField, int startIndex,
                                    int endIndex) {
        _textField = textField;
        _startIndex = startIndex;
        _endIndex = endIndex;

        _navigationFilter = new RestrictedEditingNavigationFilter();
        _documentFilter = new RestrictedEditingDocumentFilter();
    }

    public NavigationFilter getNavigationFilter() {
        return _navigationFilter;
    }

    public DocumentFilter getDocumentFilter() {
        return _documentFilter;
    }

    /**
     * Returns the start index. It remains unchanged throughout the object's
     * lifetime.
     *
     * @return the start index
     */
    public int getStartIndex() {
        return _startIndex;
    }

    /**
     * Returns the end index. It can be changed by inserting or deleting text.
     *
     * @return the end index
     */
    public synchronized int getEndIndex() {
        return _endIndex;
    }

    /**
     * Moves the end index.
     *
     * @param n the number of steps by which the index is moved.
     */
    private synchronized void moveEndIndex(final int n) {
        _endIndex += n;
    }

    /**
     * Checks if navigation to or insertion at a position is allowed.
     *
     * @param position the position
     * @return {@code true} if navigation or insertion is allowed at the
     * position, otherwise {@code false}
     */
    private synchronized boolean positionIsAllowed(int position) {
        return !(position < _startIndex || position > _endIndex);
    }

    /**
     * Returns an allowed position near the specified position.
     * If the specified position is allowed, it is returned.
     * If it is smaller than the start index, the start index is returned.
     * If it is greater than the end index, the end index is returned.
     *
     * @param position the position
     * @return an allowed position
     */
    private synchronized int allowedPositionNear(int position) {
        if (positionIsAllowed(position)) {
            return position;
        }
        return (position > _endIndex) ? _endIndex : _startIndex;
    }

    /**
     * Navigation filter that forbids navigation beyond the start and end
     * indices.
     *
     * @author 2mfriedr
     */
    class RestrictedEditingNavigationFilter extends NavigationFilter {
        @Override
        public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
            super.setDot(fb, allowedPositionNear(dot), bias);
        }

        @Override
        public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
            super.moveDot(fb, allowedPositionNear(dot), bias);
        }

        @Override
        public int getNextVisualPositionFrom(JTextComponent text, int pos,
                                             Position.Bias bias, int direction,
                                             Position.Bias[] biasRet)
                throws BadLocationException {
            int proposed = super.getNextVisualPositionFrom(text, pos, bias,
                                                           direction, biasRet);
            return allowedPositionNear(proposed);
        }
    }

    /**
     * Document filter that forbids deletion before the start index and updates
     * {@link RestrictedEditingFilters#_endIndex} upon insertion.
     *
     * This filter assumes that {@link RestrictedEditingNavigationFilter} is
     * active and the user can only insert or replace text at allowed offsets.
     * Only if an edit takes place within the start and end index, the indices
     * are updated.
     *
     * @author 2mfriedr
     */
    class RestrictedEditingDocumentFilter extends DocumentFilter {
        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            if (positionIsAllowed(offset) && positionIsAllowed(offset + length)) {
                moveEndIndex(-length);
                super.remove(fb, offset, length);
            }
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {
            if (positionIsAllowed(offset)) {
                moveEndIndex(string.length());
            }
            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                            String text, AttributeSet attrs)
                throws BadLocationException {
            if (positionIsAllowed(offset) && positionIsAllowed(offset + length)) {
                moveEndIndex(text.length() - length);
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }
}