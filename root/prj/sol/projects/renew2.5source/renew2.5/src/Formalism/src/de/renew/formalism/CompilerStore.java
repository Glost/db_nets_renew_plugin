package de.renew.formalism;

import de.renew.shadow.ShadowCompilerFactory;

import java.util.HashMap;
import java.util.Iterator;


/**
 * @author 6schumac
 *
 * A class that keeps track of all available shadow compilers.
 * These compilers represent the different formalisms in which a net may be compiled.
 */
public class CompilerStore extends HashMap<String, ShadowCompilerFactory> {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CompilerStore.class);

    CompilerStore() {
        super();
    }

    /**
     * Register a RenewMode under the specified name.
     */
    public void addCompilerFactory(String compilerName,
                                   ShadowCompilerFactory compilerFactory) {
        logger.debug("CompilerStore: compiler " + compilerName + " registered.");
        put(compilerName, compilerFactory);
    }

    /**
     *
     * @return an Iterator containing the (String) names of the registered Modes
     */
    Iterator<String> getKnownFormalisms() {
        return keySet().iterator();
    }
}