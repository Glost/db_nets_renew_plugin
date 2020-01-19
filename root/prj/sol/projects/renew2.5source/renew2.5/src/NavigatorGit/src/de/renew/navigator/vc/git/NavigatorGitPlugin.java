package de.renew.navigator.vc.git;

import de.renew.navigator.vc.VersionControlAggregator;

import de.renew.plugin.annotations.Inject;
import de.renew.plugin.di.DIPlugin;


/**
 * Representative for the NavigatorGit plug-in.
 *
 * @author Konstantin Simon Maria Moellers
 */
final public class NavigatorGitPlugin extends DIPlugin {
    private final VersionControlAggregator versionControlAggregator;
    private final GitVersionControl versionControl;

    @Inject
    public NavigatorGitPlugin(VersionControlAggregator versionControlAggregator) {
        this.versionControlAggregator = versionControlAggregator;
        versionControl = new GitVersionControl();
    }

    @Override
    public void init() {
        versionControlAggregator.addVersionControl(versionControl);
    }

    @Override
    public boolean cleanup() {
        return versionControlAggregator.removeVersionControl(versionControl);
    }
}