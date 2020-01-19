package de.renew.navigator.vc;

import de.renew.logging.CliColor;

import de.renew.navigator.NavigatorConfigurator;
import de.renew.navigator.NavigatorExtension;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.gui.FileTreeNode;
import de.renew.navigator.models.NavigatorFileTree;
import de.renew.navigator.models.TreeElement;
import de.renew.navigator.vc.impl.VersionControlAggregatorImpl;

import de.renew.plugin.annotations.Inject;
import de.renew.plugin.annotations.Provides;
import de.renew.plugin.di.DIPlugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-13
 */
final public class NavigatorVCPlugin extends DIPlugin
        implements NavigatorExtension {
    private final VersionControlAggregator aggregator;
    private final NavigatorGui gui;
    private final RepositoryTreeCellRenderer renderer;

    @Inject
    public NavigatorVCPlugin(NavigatorGui gui, final NavigatorFileTree model) {
        this.gui = gui;
        aggregator = new VersionControlAggregatorImpl();

        final HashSet<Repository> repositories = new HashSet<Repository>();
        renderer = new RepositoryTreeCellRenderer(repositories);

        model.addObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    for (TreeElement root : model.getTreeRoots()) {
                        final Repository repository = aggregator
                            .findRepository(root.getFile());

                        if (null != repository) {
                            repositories.add(repository);
                        }
                    }
                }
            });

        gui.addExtension(this);
    }

    @Override
    public boolean cleanup() {
        return gui.removeExtension(this);
    }

    @Provides
    public VersionControlAggregator getVersionControlAggregator() {
        return aggregator;
    }

    @Override
    public void configure(NavigatorConfigurator config) {
        config.addFileTreeCellRenderer(renderer);
    }

    @Override
    public JMenuItem getMenuItem(JTree tree, int x, int y,
                                 Object lastPathComponent, MutableTreeNode mtn) {
        if (!(mtn instanceof FileTreeNode)) {
            return null;
        }
        final File file = ((FileTreeNode) mtn).getFile();

        if (file.isDirectory()) {
            return null;
        }

        final Repository repository = aggregator.findRepository(file);
        if (null == repository) {
            return null;
        }

        // Build the menu.
        return buildVersionControlMenu(file, repository);
    }

    /**
     * Builds the Version Control menu.
     */
    private JMenuItem buildVersionControlMenu(final File file,
                                              final Repository repository) {
        JMenu menu = new JMenu(repository.getVersionControl().getName());

        JMenuItem diffItem = new JMenuItem("Diff with HEAD/BASE");
        diffItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    aggregator.diff(file);
                }
            });
        menu.add(diffItem);

        JMenuItem logItem = new JMenuItem("Log");
        logItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    aggregator.log(file);
                }
            });
        menu.add(logItem);

        JMenuItem infoItem = new JMenuItem("Info");
        infoItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(buildRepositoryInfo(repository));
                }
            });
        menu.add(infoItem);

        return menu;
    }

    /**
     * Builds a CLI info for a repository.
     */
    public String buildRepositoryInfo(Repository repository) {
        final StringBuilder info = new StringBuilder();
        info.append(CliColor.color(repository.getVersionControl().getName(),
                                   CliColor.BOLD));
        info.append('\n');

        info.append(CliColor.color("Root Directory: ", CliColor.YELLOW));
        info.append(repository.getRootDirectory().getAbsolutePath());
        info.append('\n');

        info.append(CliColor.color("Remote URL:     ", CliColor.YELLOW));
        info.append(repository.getRemoteURL());
        info.append('\n');

        info.append(CliColor.color("Branch:         ", CliColor.YELLOW));
        info.append(repository.getBranch());
        info.append('\n');

        info.append(CliColor.color("Revision:       ", CliColor.YELLOW));
        info.append(repository.getLastCommit().getRevision());
        info.append('\n');

        info.append(CliColor.color("Author:         ", CliColor.YELLOW));
        info.append(repository.getLastCommit().getAuthor());
        info.append('\n');

        info.append(CliColor.color("Date:           ", CliColor.YELLOW));
        info.append(repository.getLastCommit().getDate().toString());
        info.append('\n');

        info.append(CliColor.color("Message:        ", CliColor.YELLOW));
        info.append(Commit.formatMessage(repository.getLastCommit().getMessage()));
        info.append('\n');

        return info.toString();
    }
}
