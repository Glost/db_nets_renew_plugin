package de.renew.formalism.fs;

import de.renew.shadow.ShadowPreprocessor;


/**
 * Compiles Java nets with feature structure expressions, but
 * <em>without</em> concept preprocessing.  Instances of this class are
 * created by the factory {@link FSNetWithoutConceptsCompiler}.
 *
 * @author Michael Duvigneau
 **/
public class SingleFSNetWithoutConceptsCompiler extends SingleFSNetCompiler {

    /**
     * The <code>SingleFSNetWithoutConceptsCompiler</code> needs no
     * preprocessor because it does not change the concept system.
     *
     * @return an empty array.
     **/
    public final ShadowPreprocessor[] getRequiredPreprocessors() {
        return new ShadowPreprocessor[0];
    }
}