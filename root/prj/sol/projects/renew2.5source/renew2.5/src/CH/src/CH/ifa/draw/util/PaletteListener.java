/*
 * @(#)PaletteListener.java 5.1
 *
 */
package CH.ifa.draw.util;

import CH.ifa.draw.standard.ToolButton;


/**
 * Interface for handling palette events.
 *
 * @see ToolButton
 */
public interface PaletteListener {

    /**
     * The user selected a palette entry. The selected button is
     * passed as an argument.
     */
    void paletteUserSelected(ToolButton button, boolean doubleclick);

    /**
     * The user moved the mouse over the palette entry.
     */
    void paletteUserOver(ToolButton button, boolean inside);
}