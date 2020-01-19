package de.renew.faformalism.compiler;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;


public class FANetCompiler implements ShadowCompilerFactory {
    @Override
    public ShadowCompiler createCompiler() {
        return new SingleFANetCompiler();
    }
}