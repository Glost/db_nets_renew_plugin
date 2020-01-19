package de.renew.navigator.gui;

import CH.ifa.draw.util.Iconkit;

import de.renew.navigator.models.TreeElement;

import java.awt.Component;
import java.awt.Image;

import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-14
 */
public abstract class FileTreeCellRenderer implements TreeCellRenderer {
    private final LinkedList<FileTreeCellRenderer> additionalRenderers;
    private final DefaultTreeCellRenderer cellRenderer;

    public FileTreeCellRenderer() {
        additionalRenderers = new LinkedList<FileTreeCellRenderer>();
        cellRenderer = new DefaultTreeCellRenderer();
    }

    public abstract void render(DefaultTreeCellRenderer target,
                                TreeElement element, boolean selected,
                                boolean expanded);

    final public Component getTreeCellRendererComponent(JTree tree,
                                                        Object value,
                                                        boolean sel,
                                                        boolean exp,
                                                        boolean leaf, int row,
                                                        boolean hasFocus) {
        cellRenderer.getTreeCellRendererComponent(tree, value, sel, exp, leaf,
                                                  row, hasFocus);
        cellRenderer.setFont(tree.getFont());

        if (!(value instanceof FileTreeNode)) {
            return cellRenderer;
        }

        final TreeElement element = ((FileTreeNode) value).getModel();
        render(cellRenderer, element, sel, exp);

        for (FileTreeCellRenderer renderer : additionalRenderers) {
            renderer.render(cellRenderer, element, sel, exp);
        }

        return cellRenderer;
    }

    public void addAdditionalRenderer(FileTreeCellRenderer additionalRenderer) {
        this.additionalRenderers.add(additionalRenderer);
    }

    public boolean removeAdditionalRenderer(FileTreeCellRenderer additionalRenderer) {
        return this.additionalRenderers.remove(additionalRenderer);
    }

    /**
     * Uses the icon kit to load an image from its filename.
     */
    protected static Icon loadIcon(String filename) {
        return new ImageIcon(Iconkit.instance().loadImage(filename)
                                    .getScaledInstance(16, 16,
                                                       Image.SCALE_SMOOTH));
    }

    protected static void applyIcon(JLabel label, String filename) {
        label.setIcon(loadIcon(filename));
    }
}