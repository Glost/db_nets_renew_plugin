package de.renew.formalism.fsnet;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;


public class XFSNetCompiler implements ShadowCompilerFactory {
    static final long serialVersionUID = -3505281000269059428L;

    public ShadowCompiler createCompiler() {
        return new SingleXFSNetCompiler();
    }
}