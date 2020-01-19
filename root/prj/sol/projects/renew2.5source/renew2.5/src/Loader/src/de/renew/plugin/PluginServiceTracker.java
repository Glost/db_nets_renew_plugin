package de.renew.plugin;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Instances of this class represent a service tracker for plug-ins providing a
 * certain service.<br>
 * To get notified of tracked plug-ins, classes extending this class must
 * override <br> {@code bindService(IPlugin application)} and
 * {@code unbindService(IPlugin application)}.<br>
 * <br>
 * <span style="color: red;">NOTE : </span><br>
 * In opposite of the class {@link SoftDependency}, this class supports<br>
 * services provided by more than one plug-in at the same time.<br>
 *
 * @author Eva Mueller
 * @date Nov 14, 2010
 * @version 0.1
 */
public class PluginServiceTracker implements IPluginManagerListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PluginServiceTracker.class);

    /**
     * The service name to listen for.
     **/
    private String providedService;

    /**
     * List of plug-ins provding the tracker's <b>providedService</b>.
     */
    protected Collection<IPlugin> providerList;

    /**
     * The plugin manager this tracker refers to.
     **/
    private PluginManager mgr;

    /**
     * Instantiates a new <b>PluginServiceTracker</b> tracking plug-ins<br>
     * which provide the given <b>providedService</b>.<br>
     * <br>
     * To start tracking the method {@code open()} must called.
     *
     * @param providedService
     *            [String] The service to track for
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public PluginServiceTracker(String providedService) {
        this.providedService = providedService;
        this.providerList = new ArrayList<IPlugin>();
        this.mgr = PluginManager.getInstance();
    }

    /**
     * Start tracking of plug-ins which provide the tracker's
     * <b>providedService</b>.<br>
     * <br>
     * <span style="color: red;">Note</span> : Plug-ins which are already added
     * to the {@link PluginManager}<br>
     * and provide the tracker's <b>providedService</b> are tracked as well.
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public void open() {
        mgr.addPluginManagerListener(this);
        bindServices(mgr.getPluginsProviding(providedService));
    }

    /**
     * Stop tracking of plug-ins which provide the tracker's
     * <b>providedService</b>.
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public void close() {
        mgr.removePluginManagerListener(this);
        unbindServices(mgr.getPluginsProviding(providedService));
    }

    /**
    * Bind given <b>providerList</b> to the tracker.
    *
    * @param providerList [Collection&lt;{@link IPlugin}&gt;] List of {@link IPlugin}'s
    *         providing the tracker's <b>providedService</b>
    *
    * @author Eva Mueller
    * @date Nov 14, 2010
    * @version 0.1
    */
    private void bindServices(Collection<IPlugin> providerList) {
        if (providerList != null) {
            for (IPlugin provider : providerList) {
                bindService(provider);
            }
        }
    }

    /**
    * Unbind given <b>providerList</b> from the tracker.
    *
    * @param providerList [Collection&lt;{@link IPlugin}&gt;] List of {@link IPlugin}'s
    *         providing the tracker's <b>providedService</b>
    *
    * @author Eva Mueller
    * @date Nov 14, 2010
    * @version 0.1
    */
    private void unbindServices(Collection<IPlugin> providerList) {
        for (IPlugin provider : providerList) {
            unbindService(provider);
        }
        assert this.providerList.isEmpty() : "There is a difference between\n"
        + "the provider list of the plugin manager and "
        + "the plugin service tracker (" + this
        + ").\n[Tracked provider service: " + providedService + "]."
        + "\nRemaining plugins : " + this.providerList + ".";
    }

    /**
    * Bind given <b>provider</b> to the tracker.
    *
    * @param provider [{@link IPlugin}] The plug-in providing the tracker's <b>providedService</b>
    *
    * @author Eva Mueller
    * @date Nov 14, 2010
    * @version 0.1
    */
    protected void bindService(IPlugin provider) {
        if (provider != null
                    && provider.getProperties().getProvisions()
                                       .contains(providedService)) {
            this.providerList.add(provider);
        }
    }

    /**
    * Unbind given <b>provider</b> from the tracker.
    *
    * @param provider [{@link IPlugin}] The plug-in providing the tracker's <b>providedService</b>
    *
    * @author Eva Mueller
    * @date Nov 14, 2010
    * @version 0.1
    */
    protected void unbindService(IPlugin provider) {
        if (provider != null && this.providerList.contains(provider)) {
            this.providerList.remove(provider);
        }
    }

    /**
    * <span style="color: red;">To be called by the plugin manager only.</span>
    * @see de.renew.plugin.IPluginManagerListener#serviceAdded(java.lang.String, de.renew.plugin.IPlugin)
    *
    * @param service [String] {@code UNUSED}
    * @param provider [{@link IPlugin}] The newly added plug-in
    *
    * @author Eva Mueller
    * @date Nov 14, 2010
    * @version 0.1
    */
    public synchronized void serviceAdded(final String service, IPlugin provider) {
        if (provider != null) {
            bindService(provider);
        }
    }

    /**
    * <span style="color: red;">To be called by the plugin manager only.</span>
    * @see de.renew.plugin.IPluginManagerListener#serviceRemoved(java.lang.String, de.renew.plugin.IPlugin)
    *
    * @param service [String] {@code UNUSED}
    * @param provider [{@link IPlugin}] The newly removed plug-in
    *
    * @author Eva Mueller
    * @date Nov 14, 2010
    * @version 0.1
    */
    public synchronized void serviceRemoved(String service, IPlugin provider) {
        if (provider != null) {
            unbindService(provider);
        }
    }
}