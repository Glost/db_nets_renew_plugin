/*
 * Created on 25.07.2003
 *
 */
package de.renew.gui;

import CH.ifa.draw.util.Palette;

import javax.swing.JMenuItem;


/**
 * This interface is implemented by classes that allow special
 * operations if a compiler is activated.
 * <p>
 * Implementations can be added to this plugin by the
 * {@link FormalismGuiPlugin#addGuiConfigurator} method.
 * </p>
 * @author J&ouml;rn Schumacher
 * @author Michael Duvigneau
 */
public interface FormalismGuiCreator {

    /**
     * Returns a formalism-specific menu entry. This entry gets
     * automatically added to the simulation menu as long as this
     * formalism is chosen.
     *
     * @return the menu item to add to the user interface if this
     *         formalism is active. May be <code>null</code>.
     **/
    public JMenuItem createMenu();

    /**
     * Returns a formalism-specific tool palette. This palette gets
     * automatically added to the user interface as long as this
     * formalism is chosen.
     *
     * @return the palette to add to the user interface if this
     *         formalism is active. May be <code>null</code>.
     **/
    public Palette createPalette();

    /**
     * Notifies this <code>FormalismGuiCreator</code> that its
     * associated formalism has been chosen by the user.
     **/
    public void formalismActivated();

    /**
     * Notifies this <code>FormalismGuiCreator</code> that its
     * associated formalism has been deselected by the user.
     **/
    public void formalismDeactivated();
}