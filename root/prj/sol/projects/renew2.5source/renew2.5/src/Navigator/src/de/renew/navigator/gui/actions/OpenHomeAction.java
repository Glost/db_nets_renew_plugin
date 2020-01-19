package de.renew.navigator.gui.actions;

import de.renew.navigator.FilesystemController;
import de.renew.navigator.NavigatorAction;
import de.renew.navigator.gui.NavigatorIcons;

import java.awt.event.ActionEvent;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class OpenHomeAction extends NavigatorAction {
    private final FilesystemController filesystem;

    public OpenHomeAction(final FilesystemController filesystem) {
        super("Open Home paths of Navigator properties",
              NavigatorIcons.ICON_HOME, null);
        this.filesystem = filesystem;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        filesystem.loadFromProperties();
    }
}