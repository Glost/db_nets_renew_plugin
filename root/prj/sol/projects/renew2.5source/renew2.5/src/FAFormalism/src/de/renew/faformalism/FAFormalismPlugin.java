package de.renew.faformalism;

import de.renew.faformalism.compiler.FANetCompiler;

import de.renew.formalism.FormalismPlugin;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginProperties;

import de.renew.util.ClassSource;

import java.net.URL;


/**
 * The wrapper for the FAFormalism Plugin.
 *
 * <pre>
 * 0.1.0 -
 *
 * @author Pascale Möller
 * @version 0.0.1
 */
public class FAFormalismPlugin extends PluginAdapter {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FAFormalismPlugin.class);

    public FAFormalismPlugin(URL url) throws PluginException {
        super(url);
    }

    public FAFormalismPlugin(PluginProperties props) {
        super(props);
    }

    @Override
    public void init() {
        // register the FA compiler in the compiler store
        FormalismPlugin.getCurrent()
                       .addCompilerFactory("FA Compiler", new FANetCompiler());
        logger.debug("FA Compiler factory added");
    }

    @Override
    public boolean cleanup() {
        return super.cleanup();
    }
}