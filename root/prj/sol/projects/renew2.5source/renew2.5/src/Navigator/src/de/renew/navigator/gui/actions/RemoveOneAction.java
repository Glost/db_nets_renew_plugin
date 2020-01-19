package de.renew.navigator.gui.actions;

import de.renew.navigator.NavigatorAction;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.gui.NavigatorIcons;
import de.renew.navigator.models.NavigatorFileTree;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class RemoveOneAction extends NavigatorAction {
    private final NavigatorGui gui;
    private final NavigatorFileTree model;

    public RemoveOneAction(final NavigatorGui gui, final NavigatorFileTree model) {
        super("Remove Node", NavigatorIcons.ICON_REMOVE_ONE,
              KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        this.gui = gui;
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        gui.removeSelectedNodes();
    }
}