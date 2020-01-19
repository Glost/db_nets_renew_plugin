/*
 * Created on 24.07.2003
 *
 */
package de.renew.formalism;

import de.renew.formalism.bool.BoolNetCompiler;
import de.renew.formalism.java.JavaNetCompiler;
import de.renew.formalism.pt.PTNetCompiler;

import de.renew.plugin.CollectionLister;
import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.command.CLCommand;

import de.renew.shadow.ShadowCompilerFactory;

import de.renew.util.StringUtil;

import java.io.PrintStream;

import java.net.URL;

import java.util.Iterator;
import java.util.Vector;


/**
 * This class provides an administration facility to the several compilers.
 * It provides command line commands to list and choose the available
 * formalisms (a compiler represents a formalism, so both terms are used
 * to mean the same thing):
 * <ul>
 * <li>listFormalisms</li>
 * <li>setFormalism</li>
 * </ul>
 * The following compilers are provided by this Plugin:
 * <ul>
 * <li>Bool Net Compiler</li>
 * <li>Java Net Compiler</li>
 * <li>Timed Java Compiler</li>
 * <li>P/T Net Compiler</li>
 * </ul>
 * A list of compilers can be retrieved programmatically by calling the getKnownFormalisms()
 * method, which returns an Iterator containing String IDs.
 * The formalism to be used to run a simulation can be changed by calling
 * the setCompiler() method.
 * If a plugin is interested which formalism is chosen, it can listen to formalism changes
 * by registering a FormalismChangeListener object.
 *
 * @author J&ouml;rn Schumacher
 */
public class FormalismPlugin extends PluginAdapter {

    /**
     * The compiler that will become active as soon as its factory is known.
     * May be active already.
     **/
    private String _chosenCompiler;

    /**
     * The current compiler.
     **/
    private String _activeCompiler;
    private Vector<FormalismChangeListener> _listeners = new Vector<FormalismChangeListener>();
    CompilerStore _store;
    public static final String BOOL_COMPILER = "Bool Net Compiler";
    public static final String JAVA_COMPILER = "Java Net Compiler";
    public static final String TIMED_COMPILER = "Timed Java Compiler";
    public static final String PT_COMPILER = "P/T Net Compiler";
    public static final String COMPILER_PROP_NAME = "renew.compiler";

    public FormalismPlugin(URL location) throws PluginException {
        super(location);
    }

    public FormalismPlugin(PluginProperties props) {
        super(props);
    }

    public void init() {
        _store = new CompilerStore();
        _chosenCompiler = _properties.getProperty(COMPILER_PROP_NAME,
                                                  FormalismPlugin.JAVA_COMPILER);

        PluginManager.getInstance()
                     .addCLCommand("listFormalisms", new ListFormalismsCommand());
        PluginManager.getInstance()
                     .addCLCommand("setFormalism", new SetFormalismCommand());
        /*
         * Add the set of standard compilers to the CompilerStore
         */
        addCompilerFactory(BOOL_COMPILER, new BoolNetCompiler());

        ShadowCompilerFactory javaFac = new JavaNetCompiler();
        addCompilerFactory(JAVA_COMPILER, javaFac);
        addCompilerFactory(TIMED_COMPILER, new JavaNetCompiler(true, true, true));
        addCompilerFactory(PT_COMPILER, new PTNetCompiler());
        setCompiler(_chosenCompiler);
    }

    /**
     * Set the formalism to be used in simulation.
     * A list of valid arguments can be retrieved via the
     * getKnownFormalisms() method.
     *
     * @param compilerName
     */
    public void setCompiler(String compilerName) {
        logger.debug("FormalismPlugin setting compiler " + compilerName);
        if (compilerName == null) {
            return;
        }
        if (!_store.containsKey(compilerName)) {
            // store for later use
            _chosenCompiler = compilerName;
            return;
        }
        if (compilerName.equals(_activeCompiler)) {
            return;
        }
        _activeCompiler = compilerName;
        getProperties().setProperty(COMPILER_PROP_NAME, _activeCompiler);
        notifyListeners(compilerName, FormalismChangeListener.CHOOSE);
    }

    public String getCompiler() {
        return _activeCompiler;
    }

    /**
     * Create a compiler with the factory that was registered under the given name.
     * Returns null if no such factory was registered.
     */
    public ShadowCompilerFactory getCompilerFactoryByName(String name) {
        return _store.get(name);
    }


    /**
     * Register a RenewMode under the specified name.
     */
    public void addCompilerFactory(String compilerName,
                                   ShadowCompilerFactory compilerFactory) {
        logger.debug("FormalismPlugin: adding compiler " + compilerName);
        _store.addCompilerFactory(compilerName, compilerFactory);
        notifyListeners(compilerName, FormalismChangeListener.ADD);
        if (_chosenCompiler.equals(compilerName)) {
            logger.debug("FormalismPlugin: setting compiler");
            setCompiler(_chosenCompiler);
        }
    }

    public void removeCompilerFactory(String compilerName) {
        if (_store.containsKey(compilerName)) {
            logger.debug("FormalismPlugin: removing compiler " + compilerName);
            _store.remove(compilerName);
            notifyListeners(compilerName, FormalismChangeListener.REMOVE);
            if (compilerName.equals(_activeCompiler)) {
                logger.debug("FormalismPlugin: unsetting compiler");
                _chosenCompiler = compilerName;
                setCompiler(JAVA_COMPILER);
            }
        }
    }

    /**
     *  Add a listener object that will be notified when the default compiler is changed.
     */
    public void addFormalismChangeListener(FormalismChangeListener listener) {
        _listeners.add(listener);
    }

    /**
     * Remove a previously registered listener.
     *
     * @param listener
     */
    public void removeFormalismChangeListener(FormalismChangeListener listener) {
        _listeners.remove(listener);
    }

    private void notifyListeners(String name, int action) {
        Iterator<FormalismChangeListener> it = _listeners.iterator();
        while (it.hasNext()) {
            it.next().formalismChanged(name, this, action);
        }
    }

    /**
     * Retrieve an Iterator containing the (String) names of the registered Modes
     *
     * @return an Iterator containing the (String) names of the registered Modes
     */
    public Iterator<String> getKnownFormalisms() {
        return _store.getKnownFormalisms();
    }

    /**
     * Retrieve the current instance of the <code>FormalismPlugin</code>
     * from the {@link PluginManager}.
     *
     * @return  an active <code>FormalismPlugin</code> instance or
     *          <code>null</code> if there isn't any.
     */
    public static FormalismPlugin getCurrent() {
        Iterator<IPlugin> it = PluginManager.getInstance().getPlugins()
                                            .iterator();
        while (it.hasNext()) {
            IPlugin o = it.next();
            if (o instanceof FormalismPlugin) {
                return (FormalismPlugin) o;
            }
        }
        return null;
    }

    /**
     * The Command line command class for listing all available formalisms.
     *
     * @author joern
     */
    public class ListFormalismsCommand implements CLCommand {
        public void execute(String[] args, PrintStream response) {
            response.println(CollectionLister.toString(getKnownFormalisms()));
        }

        public String getDescription() {
            return "display a list of all available formalisms";
        }

        /**
         * @see de.renew.plugin.command.CLCommand#getArguments()
         */
        @Override
        public String getArguments() {
            return null;
        }
    }

    /**
     * The Command line command class for choosing a formalism.
     *
     * @author joern
     */
    public class SetFormalismCommand implements CLCommand {
        public void execute(String[] args, PrintStream response) {
            if (args.length == 0) {
                response.println("usage: setFormalism <formalismName>");
                return;
            }
            String setTo = "";
            for (int i = 0; i < args.length; i++) {
                setTo += args[i] + " ";
            }
            setTo = setTo.trim();
            if (!_store.containsKey(setTo)) {
                response.println("unknown formalism: " + setTo);
                return;
            }

            setCompiler(setTo);
        }

        public String getDescription() {
            return "sets the named formalism.";
        }

        /**
         * @see de.renew.plugin.command.CLCommand#getArguments()
         */
        @Override
        public String getArguments() {
            return StringUtil.join(CollectionLister.toArrayList(getKnownFormalisms()),
                                   " ");
        }
    }
}