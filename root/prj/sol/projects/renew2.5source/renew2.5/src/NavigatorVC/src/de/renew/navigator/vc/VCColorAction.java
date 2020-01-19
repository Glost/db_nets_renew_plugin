/**
 *
 */
package de.renew.navigator.vc;

import org.apache.log4j.Logger;

import de.renew.navigator.NavigatorAction;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.models.TreeElement;

import java.awt.event.ActionEvent;

import java.util.List;
import java.util.Set;


/**
 * Performs a Git diff on the current selected file.
 *
 * @author cabac, 1kmoelle
 */
final class VCColorAction extends NavigatorAction {
    public static final String ICON = "/de/renew/navigator/vc/images/diff.gif";
    private final Set<Repository> repositories;
    private final VersionControlAggregator holder;
    private final NavigatorGui gui;

    /**
     * Log4j logger instance.
     */
    public static final Logger logger = Logger.getLogger(VCColorAction.class);

    public VCColorAction(Set<Repository> repositories,
                         VersionControlAggregator aggregator, NavigatorGui gui) {
        super("Color changed files.", ICON, null);
        this.repositories = repositories;
        this.holder = aggregator;
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final List<TreeElement> selectedElements = gui.getSelectedElements();
        if (selectedElements.isEmpty()) {
            return;
        }

        final TreeElement treeElement = selectedElements.get(0);
        final Repository newRepository = holder.findRepository(treeElement
                                             .getFile());
        if (newRepository != null) {
            repositories.add(newRepository);

            gui.getTree().repaint();
        }
    }
}