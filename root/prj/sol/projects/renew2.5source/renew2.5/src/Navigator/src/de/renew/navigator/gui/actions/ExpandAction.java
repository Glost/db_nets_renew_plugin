package de.renew.navigator.gui.actions;

import de.renew.navigator.NavigatorAction;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.gui.NavigatorIcons;

import java.awt.event.ActionEvent;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class ExpandAction extends NavigatorAction {
    private final NavigatorGui gui;

    public ExpandAction(final NavigatorGui gui) {
        super("Expand complete folder structure",
              NavigatorIcons.ICON_EXPAND_RECURSIVE, null);
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        gui.expand();
    }
}