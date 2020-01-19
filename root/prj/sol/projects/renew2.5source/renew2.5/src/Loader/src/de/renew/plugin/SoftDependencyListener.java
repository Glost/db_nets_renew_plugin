package de.renew.plugin;



/**
 * Instances of this class are informed a <code>SoftDependency</code>
 * instance about changes in the availability of a certain plugin service.
 * <p>
 * Implementations of this interface should provide a constructor with the
 * following signature because they are instantiated via reflection:
 * <pre>
 *    public SoftDependencyListener(IPlugin caller);
 * </pre>
 * The <code>caller</code> parameter references the plugin object that
 * established the soft dependency. This reference can be used to inform
 * the plugin about state changes or to retreive information needed when
 * connecting to the service.
 * </p>
 * <p>
 * You should be aware that the plugin code may not use classes from the
 * service the soft dependency refers to. Keep such code encapsulated to
 * the implementation of this interface!
 * </p>
 *
 * @author Michael Duvigneau
 * @see SoftDependency
 **/
public interface SoftDependencyListener {

    /**
     * Called by the <code>SoftDependency</code> when the requested service
     * is available. It is guaranteed that this method is called exactly
     * once when the service becomes available. This comprises the following
     * situations:
     * <ul>
     * <li>The service has already been available when the
     *     <code>SoftDependency</code> is instantiated.</li>
     * <li>The service becomes available for the first time after the
     *     <code>SoftDependency</code> has been instantiated.</li>
     * <li>The service becomes available after it became unavailable before
     *     (meaning that <code>serviceRemoved</code> has been called).</li>
     * </ul>
     *
     * @param plugin  the provider of the requested service.
     *                If multiple providers exist, one is chosen
     *                non-deterministically.
     **/
    public void serviceAvailable(IPlugin plugin);

    /**
     * Called by the <code>SoftDependency</code> when the requested service
     * is removed. It is guaranteed that this method is called exactly once
     * when the service becomes unavailable and that the method
     * <code>serviceAvailable</code> has been called before.
     * <p>
     * This method is also called when the <code>SoftDependency.discard()</code>
     * is called.
     * </p>
     *
     * @param plugin  the provider of the requested service that has also
     *                been passed at the last <code>serviceAvailable</code>
     *                call.
     **/
    public void serviceRemoved(IPlugin plugin);
}