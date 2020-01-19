package de.renew.formalism.fs;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.SyntaxException;


/**
 * A compiler factory for Java nets with feature structure expressions and
 * concept preprocessing.
 *
 * @author Frank Wienberg (FS-Net compiler)
 * @author Olaf Kummer (factory architecture)
 * @author Michael Duvigneau (documentation)
 * @since Renew 2.0
 **/
public class FSNetCompiler implements ShadowCompilerFactory {
    static final long serialVersionUID = 4013756042486492570L;

    /**
     * Converts a JavaCC <code>ParseException</code> into a Renew
     * <code>SyntaxException</code>.
     *
     * @param e  the parser exception to convert.  In contrast to the usual
     *           JavaCC generated parser architecture, this
     *           <code>ParseException</code> must belong to the
     *           <code>de.renew.formalism.java</code> package instead of
     *           the current package!
     * @return  a <code>SyntaxException</code> with all information from the
     *          given parser exception.
     **/
    public static SyntaxException makeSyntaxException(de.renew.formalism.java.ParseException e) {
        de.renew.formalism.java.Token t = e.currentToken;
        if (t.next != null) {
            t = t.next;
        }
        return new SyntaxException(e.getMessage(), null, t.beginLine,
                                   t.beginColumn, e);
    }

    /**
     * {@inheritDoc}
     * @return a {@link SingleFSNetCompiler} instance.
     **/
    public ShadowCompiler createCompiler() {
        return new SingleFSNetCompiler();
    }
}