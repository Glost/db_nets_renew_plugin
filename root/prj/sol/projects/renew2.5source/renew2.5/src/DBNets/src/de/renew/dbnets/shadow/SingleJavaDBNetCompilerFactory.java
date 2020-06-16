package de.renew.dbnets.shadow;

import de.renew.formalism.java.SingleJavaDBNetCompiler;
import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;

public class SingleJavaDBNetCompilerFactory implements ShadowCompilerFactory {

    @Override
    public ShadowCompiler createCompiler() {
        return new SingleJavaDBNetCompiler();
    }
}
