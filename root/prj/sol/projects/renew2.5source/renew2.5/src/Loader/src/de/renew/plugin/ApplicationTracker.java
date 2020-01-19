package de.renew.plugin;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Instances of this class represent a service tracker for plug-ins requiring a certain service.<br>
 * To get notified of tracked plug-ins, classes extending this class must override <br>
 * {@code bindService(IPlugin application)} and {@code unbindService(IPlugin application)}.
 *
 * @author Eva Mueller
 * @date Nov 14, 2010
 * @version 0.1
 */
public class ApplicationTracker implements IPluginManagerListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ApplicationTracker.class);

    /**
     * The required service to listen for.
     **/
    protected String requiredService;


    /**
     * List of plug-ins requiring the tracker's <b>requiredService</b>.
     */
    protected Collection<IPlugin> applicationList;

    /**
     * The plugin manager this tracker refers to.
     **/
    private PluginManager mgr;

    /**
     * Instantiates a new <b>ApplicationTracker</b> tracking plug-ins<br>
     * which require the given <b>requiredService</b>.<br>
     * <br>
     * To start tracking the method {@code open()} must called.
     *
     * @param requiredService [String] The service to track for
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public ApplicationTracker(String requiredService) {
        this.requiredService = requiredService;
        this.applicationList = new ArrayList<IPlugin>();
        this.mgr = PluginManager.getInstance();
    }


    /**
     * Start tracking of plug-ins which require the tracker's <b>requiredService</b>.<br>
     * <br>
     * <span style="color: red;">Note</span> : Plug-ins which are already added to the {@link PluginManager}<br>
     * and require the tracker's <b>requiredService</b> are tracked as well.
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public void open() {
        mgr.addPluginManagerListener(this);
        bindServices(mgr.getPluginsRequiring(requiredService));
    }

    /**
     * Stop tracking of plug-ins which require the tracker's <b>requiredService</b>.
         *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public synchronized void close() {
        mgr.removePluginManagerListener(this);
        unbindServices(mgr.getPluginsRequiring(requiredService));
    }

    /**
     * Bind given <b>applicationList</b> to the tracker.
     *
     * @param applicationList [Collection&lt;{@link IPlugin}&gt;] List of {@link IPlugin}'s
     *         requiring the tracker's <b>requiredService</b>
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    private void bindServices(Collection<IPlugin> applicationList) {
        if (applicationList != null) {
            for (IPlugin provider : applicationList) {
                bindService(provider);
            }
        }
    }

    /**
     * Unbind given <b>applicationList</b> from the tracker.
     *
     * @param applicationList [Collection&lt;{@link IPlugin}&gt;] List of {@link IPlugin}'s
     *         requiring the tracker's <b>requiredService</b>
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    private void unbindServices(Collection<IPlugin> applicationList) {
        for (IPlugin provider : applicationList) {
            unbindService(provider);
        }
        assert this.applicationList.isEmpty() : "There is a difference between the \n"
        + "list of plugins requiring " + requiredService
        + " of the plugin manager and " + "the application service tracker ("
        + this + ").\n" + "[Tracked required service: " + requiredService
        + "]." + "\nRemaining plugins : " + this.applicationList + ".";
    }

    /**
     * Bind given <b>application</b> to the tracker.
     *
     * @param application [{@link IPlugin}] The plug-in requiring the tracker's <b>requiredService</b>
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    protected void bindService(IPlugin application) {
        if (application != null
                    && application.getProperties().getRequirements()
                                          .contains(requiredService)) {
            this.applicationList.add(application);
        }
    }

    /**
     * Unbind given <b>application</b> from the tracker.
     *
     * @param application [{@link IPlugin}] The plug-in requiring the tracker's <b>requiredService</b>
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    protected void unbindService(IPlugin application) {
        if (application != null && this.applicationList.contains(application)) {
            this.applicationList.remove(application);
        }
    }

    /**
     * <span style="color: red;">To be called by the plugin manager only.</span>
     * @see de.renew.plugin.IPluginManagerListener#serviceAdded(java.lang.String, de.renew.plugin.IPlugin)
     *
     * @param service [String] {@code UNUSED}
     * @param application [{@link IPlugin}] The newly added plug-in
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public synchronized void serviceAdded(final String service,
                                          IPlugin application) {
        if (application != null) {
            bindService(application);
        }
    }

    /**
     * <span style="color: red;">To be called by the plugin manager only.</span>
     * @see de.renew.plugin.IPluginManagerListener#serviceRemoved(java.lang.String, de.renew.plugin.IPlugin)
     *
     * @param service [String] {@code UNUSED}
     * @param application [{@link IPlugin}] The newly removed plug-in
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public synchronized void serviceRemoved(String service, IPlugin application) {
        if (application != null) {
            unbindService(application);
        }
    }
}