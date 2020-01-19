package de.renew.navigator.gui;

import de.renew.navigator.NavigatorAction;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Action;
import javax.swing.KeyStroke;


/**
 * The TreeKeyListener is listening on all key events of the Navigator-tree.
 * On different events it calls methods of the NavigatorGUI.
 *
 * @author Hannes Ahrens (4ahrens)
 * @date March 2009
 */
class TreeKeyListener implements KeyListener {
    private NavigatorGuiImpl gui = null;

    /**
     * @param navGUI the NavigatorGUI to act on
     */
    public TreeKeyListener(NavigatorGuiImpl navGUI) {
        gui = navGUI;
    }

    /**
     * is doing nothing
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
    }

    /**
     * This method currently contains the whole functionality of this class.
     * It gets invoked when a key gets released and supports the following commands:
     * - VK_ENTER/VK_ACCEPT         for opening selected files in Renew
     * - VK_DELETE                  to close selected files in the Navigator
     * - VK_DELETE + SHIFT + CTRL   to close all files in the Navigator
     * - VK_C + SHIFT + CTRL        to collapse all files in the Navigator
     * - VK_O + SHIFT + CTRL        to open/add files in the Navigator
     * - VK_R + CTRL                to refresh all files in the Navigator
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 0) {
            return;
        }

        // Check accelerator of actions.
        if (checkActionAccelerators(e)) {
            return;
        }

        switch (e.getKeyCode()) {
        case KeyEvent.VK_ACCEPT:
        case KeyEvent.VK_ENTER:
            gui.openSelected();
            break;
        }
    }

    /**
     * is doing nothing
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Checks the press of accelerators of navigator actions.
     *
     * @param e the key event to check for
     * @return true, if an action occurred
     */
    private boolean checkActionAccelerators(KeyEvent e) {
        for (NavigatorAction action : gui.getActions()) {
            KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

            if (stroke == null) {
                continue;
            }

            boolean shiftOk = (InputEvent.SHIFT_MASK & stroke.getModifiers()) == 0
                              || e.isShiftDown();
            boolean ctrlOk = (InputEvent.CTRL_MASK & stroke.getModifiers()) == 0
                             || e.isControlDown();

            if (stroke.getKeyCode() == e.getKeyCode() && shiftOk && ctrlOk) {
                action.actionPerformed(null);
                return true;
            }
        }

        return false;
    }
}