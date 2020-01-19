package de.renew.navigator.gui;

import de.renew.navigator.models.Directory;
import de.renew.navigator.models.Leaf;
import de.renew.navigator.models.TreeElement;

import java.awt.Font;

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * The FileTreeCellRenderer class can be applied to a JTree as CellRenderer.
 * It extends the DefaultTreeCellRenderer with the ability to store and render different Icons for different file types.
 *
 * @author Konstantin Moellers (1kmoelle), Hannes Ahrens (4ahrens)
 * @version August 2015
 */
class DefaultFileTreeCellRenderer extends FileTreeCellRenderer {
    private final HashMap<String, Icon> icons;
    private Icon defaultIcon = null;

    /**
     * The default constructor i initializing the internal icon HashMap.
     */
    public DefaultFileTreeCellRenderer() {
        icons = new HashMap<String, Icon>();

        this.setDefaultIcon(NavigatorIcons.FILE);
        this.addIcon(".aip", NavigatorIcons.FILE_AIP);
        this.addIcon(".draw", NavigatorIcons.FILE_DRAW);
        this.addIcon(".rnw", NavigatorIcons.FILE_NET);
        this.addIcon(".net", NavigatorIcons.FILE_NET);
        this.addIcon(".pnml", NavigatorIcons.FILE_NET);
        this.addIcon(".arm", NavigatorIcons.FILE_ARM);
        this.addIcon(".mad", NavigatorIcons.FILE_ARM);
        this.addIcon(".java", NavigatorIcons.FILE_JAVA);
        this.addIcon("build.xml", NavigatorIcons.FILE_ANT);
        this.addIcon(".xml", NavigatorIcons.FILE_XML);
        this.addIcon(".xmi", NavigatorIcons.FILE_XML);
        this.addIcon(".md", NavigatorIcons.FILE_MARKDOWN);
        this.addIcon(".markdown", NavigatorIcons.FILE_MARKDOWN);
    }

    /**
     * @param filename the Icon to set as default
     */
    public void setDefaultIcon(String filename) {
        defaultIcon = loadIcon(filename);
    }

    /**
     * @param key the file extension ".*" the icon shall be displayed for
     * @param filename the Icon to render
     * @return previous fileIcon if key already being used, else null
     */
    public Icon addIcon(String key, String filename) {
        return icons.put(key, loadIcon(filename));
    }

    /**
     * This method overwrites the corresponding virtual method of the DefaultTreeCellRenderer.
     * It's used by the JTree to render its nodes and sets the corresponding Icon for each node when rendered.
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
    public void render(DefaultTreeCellRenderer target, TreeElement element,
                       boolean selected, boolean expanded) {
        // Primarily set default icon
        if (defaultIcon != null) {
            target.setIcon(defaultIcon);
        }

        // Set tooltip text.
        target.setToolTipText(element.getFile().getAbsolutePath());

        // For files
        if (element instanceof Leaf) {
            renderLeaf(target, (Leaf) element);
        }

        // For directories
        if (element instanceof Directory) {
            renderDirectory(target, (Directory) element, expanded);
        }
    }

    private void renderLeaf(DefaultTreeCellRenderer target, Leaf leaf) {
        // Load extension specific extension
        final String name = leaf.getFile().getName();

        // Place DC icon.
        if (name.contains("_DC") || name.contains("DC_")) {
            applyIcon(target, NavigatorIcons.FILE_DC);
            return;
        }

        // Place Ontology DC icon.
        if (name.contains("Ontology")) {
            applyIcon(target, NavigatorIcons.FILE_ONTOLOGY);
            return;
        }

        for (String key : icons.keySet()) {
            if (!name.endsWith(key)) {
                continue;
            }

            final Icon fileIcon = icons.get(key);
            target.setIcon(fileIcon);
            break;
        }
    }

    private void renderDirectory(DefaultTreeCellRenderer target,
                                 Directory directory, boolean expanded) {
        // For unclassified directories
        if (directory.getType() == null) {
            if (expanded) {
                applyIcon(target, NavigatorIcons.PILE_OPENED);
            } else {
                applyIcon(target, NavigatorIcons.PILE_CLOSED);
            }

            return;
        }

        switch (directory.getType()) {
        // Renew/Java packages
        case PACKAGE:
            applyIcon(target, NavigatorIcons.PACKAGE);
            return;

        // Plugin directories
        case PLUGIN:
            if (expanded) {
                applyIcon(target, NavigatorIcons.PLUGIN_OPENED);
            } else {
                applyIcon(target, NavigatorIcons.PLUGIN_CLOSED);
            }
            target.setFont(new Font(target.getFont().getName(), Font.BOLD,
                                    target.getFont().getSize()));

            return;

        // Source directories
        case SOURCE:
            if (expanded) {
                applyIcon(target, NavigatorIcons.BLUE_OPENED);
            } else {
                applyIcon(target, NavigatorIcons.BLUE_CLOSED);
            }
            return;

        // Test source directories
        case TEST_SOURCE:
            if (expanded) {
                applyIcon(target, NavigatorIcons.GREEN_OPENED);
            } else {
                applyIcon(target, NavigatorIcons.GREEN_CLOSED);
            }
            return;

        // Any other directory
        default:
            if (expanded) {
                applyIcon(target, NavigatorIcons.PILE_OPENED);
            } else {
                applyIcon(target, NavigatorIcons.PILE_CLOSED);
            }
        }
    }
}