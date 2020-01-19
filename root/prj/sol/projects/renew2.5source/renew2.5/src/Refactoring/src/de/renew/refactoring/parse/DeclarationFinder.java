package de.renew.refactoring.parse;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.gui.DeclarationFigure;


/**
 * Finds the declaration figure in a drawing.
 *
 * @author 2mfriedr
 */
public class DeclarationFinder {
    private final Drawing _drawing;

    public DeclarationFinder(final Drawing drawing) {
        _drawing = drawing;
    }

    /**
     * Finds the declaration figure in the drawing by iterating through the
     * drawing's figures.
     *
     * @return the declaration figure, or {@code null} if the drawing does not
     * have a declaration figure
     */
    public DeclarationFigure declarationFigure() {
        FigureEnumeration figures = _drawing.figures();
        while (figures.hasMoreElements()) {
            Figure figure = figures.nextFigure();
            if (figure instanceof DeclarationFigure) {
                return (DeclarationFigure) figure;
            }
        }
        return null;
    }

    /**
     * Returns the text of the declaration figure found by {@link
     * #declarationFigure()}.
     *
     * @return the declaration text, or an empty string if the drawing does not
     * have a declaration figure
     */
    public String declarationText() {
        DeclarationFigure declaration = declarationFigure();
        return (declaration != null) ? declaration.getText() : "";
    }

    /**
     * Checks if the drawing has a declaration figure by iterating through the
     * drawing's figures.
     *
     * @return {@code true} if the drawing has a declaration figure, otherwise
     * {@code false}
     */
    public boolean hasDeclarationFigure() {
        return declarationFigure() != null;
    }
}