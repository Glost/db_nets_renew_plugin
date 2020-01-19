package de.renew.refactoring.edit;

import CH.ifa.draw.framework.Drawing;

import de.renew.refactoring.match.TextFigureMatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * TextFigureMatchEditor performs edits on text figures and returns arbitrary
 * objects of type R. It keeps track of the previous texts of the text figures
 * and is able to restore them.
 *
 * @author 2mfriedr
 */
public abstract class TextFigureMatchEditor<T extends TextFigureMatch, R>
        extends IteratorEditor<T, R> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(TextFigureMatchEditor.class);
    private final Map<T, String> _previousTexts;

    /**
     * Constructs a TextFigureMatchEditor with a list of matches.
     *
     * @param matches a list of matches
     */
    public TextFigureMatchEditor(final List<T> matches) {
        super(new TextFigureMatchSorter<T>().sorted(matches));
        _previousTexts = previousTexts(matches);
    }

    /**
     * Returns the drawing name.
     */
    @Override
    public String getCurrentEditString() {
        return getCurrentEdit().getDrawing().getName();
    }

    /**
     * Changes the text of a text figure and calls {@link
     * Drawing#checkDamage()} on the text figure's drawing.
     *
     * @param match the match, to get the drawing
     * @param newText the new text
     */
    protected void changeText(final T match, final String newText) {
        match.getTextFigure().setText(newText);
        match.getTextFigure().changed();
        match.getDrawing().checkDamage();
    }

    /**
     * Gets the current texts of all text figures that will be edited. The
     * result is a map of matches and strings to be able to use {@link
     * #changeText(T, String)} to restore the texts.
     *
     * For now, matches on the same text figure appear more than once in the map.
     * Since the previous texts are the same, this might only be a performance
     * issue.
     *
     * @param matches a list of matches
     * @return a map of matches and strings
     */
    private Map<T, String> previousTexts(final List<T> matches) {
        Map<T, String> previousTexts = new HashMap<T, String>();
        for (T match : matches) {
            previousTexts.put(match, match.getText());
        }
        return previousTexts;
    }

    /**
     * Restores the previous texts of all matches.
     */
    public void restorePreviousTexts() {
        for (T match : _previousTexts.keySet()) {
            String previous = _previousTexts.get(match);
            logger.debug("Restoring previous text: " + previous);
            changeText(match, previous);
        }
    }
}