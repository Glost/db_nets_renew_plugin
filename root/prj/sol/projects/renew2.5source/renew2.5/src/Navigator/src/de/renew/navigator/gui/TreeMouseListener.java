package de.renew.navigator.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * The TreeMouseListener listens on mouse events of the Navigator-tree.
 * On different events it calls methods of the NavigatorGUI.
 *
 * @author Hannes Ahrens (4ahrens)
 * @version  March 2009
 */
class TreeMouseListener extends MouseAdapter {
    private final NavigatorGuiImpl gui;

    /**
     * @param gui the NavigatorGUI to act on
     */
    public TreeMouseListener(NavigatorGuiImpl gui) {
        this.gui = gui;
    }

    /**
     * This method currently contains the whole functionality of this class.
     * It gets invoked when a mouse button gets clicked and supports the following commands:
     * - double-click for opening selected files in Renew
     * - right or middle mouse button click to open a context menu
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        int iButton = e.getButton();
        if (iButton == MouseEvent.BUTTON1) {
            if (e.getClickCount() > 1) {
                gui.openSelected();
            }
        } else if (iButton == MouseEvent.BUTTON2
                           || iButton == MouseEvent.BUTTON3) {
            gui.showContextMenu(e.getX(), e.getY());
        }
    }
}