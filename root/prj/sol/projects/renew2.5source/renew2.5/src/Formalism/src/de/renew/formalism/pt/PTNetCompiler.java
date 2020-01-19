package de.renew.formalism.pt;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;


public class PTNetCompiler implements ShadowCompilerFactory {
    static final long serialVersionUID = -4718604739313246751L;

    public ShadowCompiler createCompiler() {
        return new SinglePTNetCompiler();
    }
}