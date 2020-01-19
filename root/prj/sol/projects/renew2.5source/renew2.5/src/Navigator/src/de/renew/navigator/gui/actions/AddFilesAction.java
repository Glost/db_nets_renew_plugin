package de.renew.navigator.gui.actions;

import CH.ifa.draw.DrawPlugin;

import de.renew.navigator.FilesystemController;
import de.renew.navigator.NavigatorAction;
import de.renew.navigator.gui.NavigatorIcons;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.io.File;

import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public class AddFilesAction extends NavigatorAction {
    private final FilesystemController filesystemCtrl;

    public AddFilesAction(FilesystemController filesystemCtrl) {
        super("Add Files (Ctrl+Shift+O)", NavigatorIcons.ICON_ADD,
              KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                     InputEvent.CTRL_MASK
                                     | InputEvent.SHIFT_MASK));
        this.filesystemCtrl = filesystemCtrl;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // choose file or directory with JFileChooser
        JFileChooser fc = new JFileChooser(DrawPlugin.getCurrent().getIOHelper()
                                                     .getCurrentDirectory(null));
        final FileFilter fileFilter = filesystemCtrl.getFileFilter();

        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(fileFilter);
        fc.setFileFilter(fileFilter);
        fc.setFileHidingEnabled(true);
        fc.setMultiSelectionEnabled(true);
        int returnVal = fc.showOpenDialog(null);

        // File chooser was not approved?
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] files = fc.getSelectedFiles();

        // Loads root directories
        filesystemCtrl.loadRootDirectories(Arrays.asList(files));

        File parent = files[0].getParentFile();
        if (parent != null && parent.isDirectory()) {
            DrawPlugin.getCurrent().getIOHelper().setLastPath(parent);
        }
    }
}