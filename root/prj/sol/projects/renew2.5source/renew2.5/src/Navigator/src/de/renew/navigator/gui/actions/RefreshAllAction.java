package de.renew.navigator.gui.actions;

import de.renew.navigator.FilesystemController;
import de.renew.navigator.NavigatorAction;
import de.renew.navigator.gui.NavigatorIcons;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


/**
 * Refreshes all files contained in the navigator.
 *
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class RefreshAllAction extends NavigatorAction {
    private final FilesystemController filesystem;

    public RefreshAllAction(FilesystemController filesystem) {
        super("Refresh All (Ctrl+R)", NavigatorIcons.ICON_REFRESH_ALL,
              KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        this.filesystem = filesystem;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        filesystem.refreshPaths();
    }
}