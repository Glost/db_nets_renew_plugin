/*
 * @(#)TextHolder.java 5.1
 *
 */
package CH.ifa.draw.standard;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;


/**
 * The interface of a figure that has some editable text contents.
 * @see CH.ifa.draw.framework.Figure
 */
public interface TextHolder {
    public Rectangle textDisplayBox();

    /**
     * Gets the text shown by the text figure.
     */
    public String getText();

    /**
     * Sets the text shown by the text figure.
     */
    public void setText(String newText);

    /**
     * Tests whether the figure accepts typing.
     */
    public boolean acceptsTyping();

    /**
     * Sets whether the figure accepts typing.
     */
    public void setReadOnly(boolean isReadOnly);

    /**
     * Gets the number of rows and columns to be overlaid when the figure
     *  is edited.
     */
    public Dimension overlayRowsAndColumns();

    /**
     * Gets the font.
     */
    public Font getFont();
}