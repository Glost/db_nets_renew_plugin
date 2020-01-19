package de.renew.navigator.vc.svn;

import de.renew.navigator.vc.VersionControlAggregator;

import de.renew.plugin.annotations.Inject;
import de.renew.plugin.di.DIPlugin;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-14
 */
final public class NavigatorSVNPlugin extends DIPlugin {
    private final VersionControlAggregator versionControlAggregator;
    private final SVNVersionControl versionControl;

    @Inject
    public NavigatorSVNPlugin(VersionControlAggregator versionControlAggregator) {
        this.versionControlAggregator = versionControlAggregator;
        versionControl = new SVNVersionControl();
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