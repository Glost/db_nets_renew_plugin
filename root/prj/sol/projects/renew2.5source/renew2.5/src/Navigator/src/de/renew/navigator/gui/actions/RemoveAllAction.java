package de.renew.navigator.gui.actions;

import de.renew.navigator.NavigatorAction;
import de.renew.navigator.gui.NavigatorIcons;
import de.renew.navigator.models.NavigatorFileTree;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class RemoveAllAction extends NavigatorAction {
    private final NavigatorFileTree model;

    public RemoveAllAction(final NavigatorFileTree model) {
        super("Remove All (Ctrl+Shift+Delete)", NavigatorIcons.ICON_REMOVE_ALL,
              KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                                     InputEvent.CTRL_MASK
                                     | InputEvent.SHIFT_MASK));
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        model.clearTreeRoots();
        model.notifyObservers();
    }
}