package de.renew.refactoring.wizard;

import java.awt.Desktop;

import java.io.File;
import java.io.IOException;

import java.util.List;


/**
 * ListPanel for a list of files that are opened on double-click.
 *
 * @author 2mfriedr
 */
public class FileListPanel extends ListPanel<File> {
    private static final long serialVersionUID = -8898690074143812196L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FileListPanel.class);

    public FileListPanel(List<File> list, String title) {
        super(list, title);
    }

    @Override
    protected String getTitleForItem(File item) {
        return item.getName();
    }

    @Override
    protected void openItem(File item) {
        try {
            Desktop.getDesktop().open(item);
        } catch (IOException ignored) {
        }
    }
}