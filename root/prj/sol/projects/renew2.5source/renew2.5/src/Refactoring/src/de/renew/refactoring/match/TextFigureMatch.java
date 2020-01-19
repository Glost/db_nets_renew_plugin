package de.renew.refactoring.match;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;

import de.renew.refactoring.util.StringHelper;


/**
 * Parent class for string matches within a text figure's text.
 *
 * @see StringMatch
 * @author 2mfriedr
 */
public class TextFigureMatch extends Match {
    private final Drawing _drawing;
    private final TextFigure _textFigure;

    /**
     * Constructs a TextFigureMatch object.
     *
     * @param drawing the drawing
     * @param textFigure the text figure
     * @param stringMatch the string match - if more than one match is needed,
     * this should be the outermost match.
     */
    protected TextFigureMatch(final Drawing drawing,
                              final TextFigure textFigure,
                              final StringMatch stringMatch) {
        super(stringMatch);
        _drawing = drawing;
        _textFigure = textFigure;
    }

    /**
     * Returns the text figure.
     *
     * @return the text figure
     */
    public TextFigure getTextFigure() {
        return _textFigure;
    }

    /**
     * Returns the drawing.
     *
     * @return the drawing
     */
    public Drawing getDrawing() {
        return _drawing;
    }

    /**
    * Returns the match's text figure's text. Shortcut for {@code
    * getTextFigure().getText()}.
    *
    * @return the text
    */
    public String getText() {
        return _textFigure.getText();
    }

    @Override
    public String toString() {
        return getMatch();
    }

    public String matchWithContext() {
        return matchWithContext(40);
    }

    public String matchWithContext(final int length) {
        return StringHelper.substringWithContext(getText(), getStart(),
                                                 getEnd(), length);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ _drawing.hashCode() ^ _textFigure.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof TextFigureMatch) {
            TextFigureMatch other = (TextFigureMatch) obj;
            return getDrawing() == other.getDrawing()
                   && getTextFigure() == other.getTextFigure();
        }
        return false;
    }
}