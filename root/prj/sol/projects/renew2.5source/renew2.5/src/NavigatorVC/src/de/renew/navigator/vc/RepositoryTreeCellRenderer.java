package de.renew.navigator.vc;

import de.renew.navigator.gui.FileTreeCellRenderer;
import de.renew.navigator.models.TreeElement;

import java.awt.Color;
import java.awt.Font;

import java.util.Set;

import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-14
 */
class RepositoryTreeCellRenderer extends FileTreeCellRenderer {
    private final Set<Repository> repositories;
    public static final Color MODIFIED_COLOR = new Color(0, 51, 153);
    public static final Color ADDED_COLOR = new Color(0, 102, 0);

    public RepositoryTreeCellRenderer(Set<Repository> repositories) {
        this.repositories = repositories;
    }

    @Override
    public void render(DefaultTreeCellRenderer target, TreeElement element,
                       boolean selected, boolean expanded) {
        for (Repository repository : repositories) {
            if (repository.getRootDirectory().equals(element.getFile())) {
                target.setText(element.getFile().getName() + " @ "
                               + repository.getBranch());
            }

            if (repository.getIgnored().contains(element.getFile())) {
                target.setForeground(new Color(150, 150, 150));
                target.setText(target.getText());
                return;
            }

            final boolean isModified = repository.getModified()
                                                 .contains(element.getFile());
            final boolean isAdded = repository.getAdded()
                                              .contains(element.getFile());

            if (isModified || isAdded) {
                target.setFont(new Font(target.getFont().getName(), Font.BOLD,
                                        target.getFont().getSize()));
                target.setText(target.getText() + " - ");
            }

            if (isModified) {
                target.setText(target.getText() + "M");
                target.setForeground(MODIFIED_COLOR);
            }

            if (isAdded) {
                target.setText(target.getText() + "A");
                target.setForeground(ADDED_COLOR);
            }
        }
    }
}