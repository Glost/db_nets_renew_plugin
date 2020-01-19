package de.renew.navigator.diff;

import de.renew.navigator.NavigatorAction;
import de.renew.navigator.NavigatorPlugin;
import de.renew.navigator.gui.FileTreeNode;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 * Performs a Git diff on the current selected file.
 *
 * @author cabac, 1kmoelle
 */
final class FileDiffAction extends NavigatorAction {
    public static final String ICON = "/de/renew/navigator/diff/images/icon.gif";
    private final NavigatorPlugin plugin;
    private final FileDiffer differ;

    public FileDiffAction(NavigatorPlugin plugin, FileDiffer differ) {
        super("Diffs two files.", ICON, null);
        this.plugin = plugin;
        this.differ = differ;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTree tree = plugin.getTree();
        TreePath[] paths = tree.getSelectionPaths();

        if (paths != null && paths.length == 2) {
            TreeNode a = (TreeNode) paths[0].getLastPathComponent();
            TreeNode b = (TreeNode) paths[1].getLastPathComponent();

            if (a instanceof FileTreeNode && b instanceof FileTreeNode) {
                FileTreeNode atn = ((FileTreeNode) a);
                FileTreeNode btn = ((FileTreeNode) b);
                File aFile = atn.getFile();
                File bFile = btn.getFile();

                // Execute the actual file diff. (Thank you, Java!)
                if (!aFile.isDirectory() && !bFile.isDirectory()) {
                    differ.showFileDiff(aFile, bFile);
                }
            }
        }
    }
}