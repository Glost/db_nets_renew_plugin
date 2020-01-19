package de.renew.navigator.gui;

import CH.ifa.draw.application.AbstractFileDragDropListener;

import de.renew.navigator.FilesystemController;

import java.awt.Point;

import java.io.File;

import java.util.Arrays;


/**
 * @version Renew 2.5
 */
class DragDropListener extends AbstractFileDragDropListener {
    private final FilesystemController filesystemCtrl;

    public DragDropListener(FilesystemController filesystemCtrl) {
        this.filesystemCtrl = filesystemCtrl;
    }

    @Override
    protected void handleFiles(File[] files, Point loc) {
        filesystemCtrl.loadRootDirectories(Arrays.asList(files));
    }
}