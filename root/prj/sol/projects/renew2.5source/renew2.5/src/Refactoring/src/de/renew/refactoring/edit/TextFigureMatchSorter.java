package de.renew.refactoring.edit;

import CH.ifa.draw.figures.TextFigure;

import de.renew.refactoring.match.TextFigureMatch;


/**
 * Sorts {@link TextFigureMatch} objects to an order that allows renaming them.
 *
 * @author 2mfriedr
 */
class TextFigureMatchSorter<T extends TextFigureMatch> extends MatchSorter<T, TextFigure> {
    @Override
    protected TextFigure group(T match) {
        return match.getTextFigure();
    }
}