package de.renew.formalism.bool;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;


public class BoolNetCompiler implements ShadowCompilerFactory {
    static final long serialVersionUID = -2063740003806585503L;

    public ShadowCompiler createCompiler() {
        return new SingleBoolNetCompiler();
    }
}