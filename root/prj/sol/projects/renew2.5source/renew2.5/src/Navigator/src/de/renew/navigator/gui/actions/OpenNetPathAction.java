package de.renew.navigator.gui.actions;

import de.renew.navigator.FilesystemController;
import de.renew.navigator.NavigatorAction;
import de.renew.navigator.gui.NavigatorIcons;

import java.awt.event.ActionEvent;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class OpenNetPathAction extends NavigatorAction {
    private final FilesystemController filesystem;

    public OpenNetPathAction(final FilesystemController filesystem) {
        super("Open NetPaths of simulator properties",
              NavigatorIcons.ICON_NETPATH, null);
        this.filesystem = filesystem;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            filesystem.loadFromNetPaths();
        } catch (RuntimeException e) {
            error(e.getMessage());
        }
    }
}