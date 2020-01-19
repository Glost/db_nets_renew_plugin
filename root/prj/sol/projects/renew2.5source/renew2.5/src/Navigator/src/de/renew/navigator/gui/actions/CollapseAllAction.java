package de.renew.navigator.gui.actions;

import de.renew.navigator.NavigatorAction;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.gui.NavigatorIcons;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class CollapseAllAction extends NavigatorAction {
    private final NavigatorGui gui;

    public CollapseAllAction(final NavigatorGui gui) {
        super("Collapse All (Ctrl+Shift+C)", NavigatorIcons.ICON_COLLAPSE_ALL,
              KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                     InputEvent.CTRL_MASK
                                     | InputEvent.SHIFT_MASK));
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        gui.collapseAll();
    }
}