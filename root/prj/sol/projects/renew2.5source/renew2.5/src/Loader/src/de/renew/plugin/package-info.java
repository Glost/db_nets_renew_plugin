/**
 * <p>
 * Comprises the basic plug-in system for the Renew application.
 * Classes in this package have several ground-level responsibilities:
 * <ul>
 * <li> Locate plug-ins in the file system
 * </li>
 * <li> Load plug-ins into the Java virtual machine.
 * </li>
 * <li> Manage dependencies between plug-ins.
 * </li>
 * <li> Control the life cycle of the application and all its plug-ins.
 * </li>
 * <li> Manage (and provide some) basic commands to control the plug-in
 *      system configuration and individual plug-in functionality.
 * </li>
 * <li> Provide initial configuration for the <em>Log4J</em> environment.
 * </li>
 * </ul>
 * </p>
 * <h3>Class Loading</h3>
 * <p>
 * The {@link de.renew.plugin.PluginManager} creates and maintains a set of
 * {@link ClassLoader}s for itself, all loaded plug-ins and user-provided
 * classes.
 * </p>
 * <dl>
 * <dt> {@linkplain PluginManager#getSystemClassLoader Renew system class loader}
 * </dt>
 * <dd> The <em>defining class loader</em> of the
 *      <code>PluginManager</code> (the one that has been used to load the
 *      <code>PluginManager</code> class itself) is regarded as <em>system
 *      class loader</em>.  This class loader is expected to know all
 *      classes from this package as provided in <code>loader.jar</code>.
 *      <em>Attention: The class {@link de.renew.plugin.Loader} known to
 *      the Java system class loader usually differs from the class
 *      <code>Loader</code> known to the <code>PluginManager</code>'s
 *      system class loader.</em>
 * </dd>
 * <dt> {@linkplain PluginManager#getPluginClassLoader Plug-in class loader}
 * </dt>
 * <dd> The <code>PluginManager</code> creates one special class loader to
 *      use for the loading of classes of all plug-ins and their libraries.
 *      This class loader is configured dynamically by the
 *      <code>PluginManager</code> to include code sources when a plug-in
 *      is loaded.  The current implementation is {@link de.renew.plugin.PluginClassLoader}.
 * </dd>
 * <dt> {@linkplain #getBottomClassLoader Bottom class loader}
 * </dt>
 * <dd> The <code>PluginManager</code> also creates one special class
 *      loader to use for user-provided classes.  This class loader is
 *      initially configured based on the property
 *      <code>de.renew.classPath</code>.  Later changes to the property do
 *      not have any effect.  The current implementation is
 *      {@link de.renew.plugin.BottomClassLoader}.
 *      <em>Attention: The {@link de.renew.application.SimulatorPlugin} may
 *      create its own bottom class loaders.  These are not related to the
 *      <code>PluginManager</code>'s bottom class loader.</em>
 * </dd>
 * </dl>
 * <p>
 * Although the <code>PluginManager</code> provides a <code>main</code>
 * method to invoke the system, it is usual to invoke
 * {@link de.renew.plugin.Loader} instead.  The <code>Loader</code> replaces
 * its own class loader (usually the <em>Java system class loader</em>) by
 * a copy.  The copy's classpath is augmented to include all global
 * libraries and the plug-in management code for the Renew application.
 * This is needed to provide basic services like <em>Log4J</em> for the
 * whole system without requiring the user to setup the
 * <code>CLASSPATH</code> environment variable or specify a
 * <code>-classpath</code> command line option.
 * </p>
 * <p>
 * The class loaders are organized in a delegation hierarchy as shown below.
 * </p>
 * <p align="center">
 * <object data="doc-files/RenewClassloaders.svg"
 *         type="image/svg+xml" width="858px" height="600px"
 *         alt="Complete Renew class loader hierarchy"/>
 * </p>
 *
 * @since Renew 2.0
 * @author J&ouml;rn Schumacher (first prototype)
 * @author Michael Duvigneau (documentation and refinements)
 * @see "Loader/etc/README.loader (in the source tree)"
 * @see "J&ouml;rn Schumacher: <em>Eine Plug-in-Architektur f&uuml;r Renew:
 *      Konzepte, Methoden, Umsetzung.</em>  Diplomarbeit, Universit&auml;t
 *      Hamburg, 2003."
 **/
package de.renew.plugin;

