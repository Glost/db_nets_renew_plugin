package de.renew.formalism.efsnet;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;


public class EFSNetCompiler implements ShadowCompilerFactory {
    static final long serialVersionUID = 6074891576110156092L;

    public ShadowCompiler createCompiler() {
        return new SingleEFSNetCompiler();
    }
}