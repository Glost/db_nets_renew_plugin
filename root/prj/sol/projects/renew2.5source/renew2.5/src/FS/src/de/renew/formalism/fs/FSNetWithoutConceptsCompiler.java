package de.renew.formalism.fs;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;


/**
 * A compiler factory for Java nets with feature structure expressions,
 * but <em>without</em> concept preprocessing.
 * <p>
 * This formalism is not suitable to start a FSNet simulation because it
 * does not compile the concept system.  It can only be used for dynamic
 * net loading when the simulation has been started with a full-fledged
 * formalism from the FSNet family.
 * </p>
 *
 * @author Michael Duvigneau
 * @see FSNetCompiler
 * @see de.renew.gui.fs.FSPlugin
 **/
public class FSNetWithoutConceptsCompiler implements ShadowCompilerFactory {

    /**
     * {@inheritDoc}
     * @return a {@link SingleFSNetWithoutConceptsCompiler} instance.
     **/
    public final ShadowCompiler createCompiler() {
        return new SingleFSNetWithoutConceptsCompiler();
    }
}