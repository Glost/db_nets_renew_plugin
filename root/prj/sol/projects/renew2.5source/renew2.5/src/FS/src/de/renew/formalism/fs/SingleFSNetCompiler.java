package de.renew.formalism.fs;

import de.renew.formalism.java.ArcFactory;
import de.renew.formalism.java.InscriptionParser;
import de.renew.formalism.java.ParsedDeclarationNode;
import de.renew.formalism.java.SingleJavaNetCompiler;
import de.renew.formalism.java.TimedExpression;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowPreprocessor;
import de.renew.shadow.SyntaxException;

import java.util.Collection;


/**
 * Compiles Java nets with feature structure expressions and concept
 * preprocessing.  Instances of this class are created by the factory
 * {@link FSNetCompiler}.
 *
 * @author Frank Wienberg (FS-Net compiler)
 * @author Olaf Kummer (factory architecture)
 * @author Michael Duvigneau (documentation)
 * @since Renew 2.0
 **/
public class SingleFSNetCompiler extends SingleJavaNetCompiler {

    /**
     * Creates a new <code>SingleFSNetCompiler</code> instance.
     * <p>
     * The <code>SingleFSNetCompiler</code> is a specific
     * <code>SingleJavaNetCompiler</code> that allows dangerous arcs, but
     * no time inscriptions. It needs early tokens.
     * </p>
     **/
    public SingleFSNetCompiler() {
        super(true, false, true);
    }

    /**
     * The <code>SingleFSNetCompiler</code> needs a
     * {@link FSNetPreprocessor} to compile the concept system before the
     * nets are compiled.
     *
     * @return  {@inheritDoc}
     **/
    public ShadowPreprocessor[] getRequiredPreprocessors() {
        return new ShadowPreprocessor[] { new FSNetPreprocessor() };
    }

    /**
     * The <code>SingleFSNetCompiler</code> provides the
     * {@link FSNetParser} to compile inscriptions.
     *
     * @param inscr  {@inheritDoc}
     * @return  {@inheritDoc}
     **/
    protected InscriptionParser makeParser(String inscr) {
        return new FSNetParser(new java.io.StringReader(inscr));
    }

    /**
     * Parses the given arc inscription using the super class
     * implementation.  However, the empty inscription is parsed into an
     * empty feature structure.
     *
     * @param inscr {@inheritDoc}
     * @return {@inheritDoc}
     * @throws SyntaxException {@inheritDoc}
     * @see SingleJavaNetCompiler#parseArcInscription(String)
     **/
    protected Collection<TimedExpression> parseArcInscription(String inscr)
            throws SyntaxException {
        if (inscr == null || inscr.equals("")) {
            inscr = "[]";
        }
        return super.parseArcInscription(inscr);
    }

    /**
     * Creates an empty {@link ParsedFSDeclarationNode} for the given net.
     *
     * @param net {@inheritDoc}
     * @return {@inheritDoc}
     **/
    protected ParsedDeclarationNode makeEmptyDeclarationNode(ShadowNet net) {
        if (net != null) {
            return new ParsedFSDeclarationNode(net.getName());
        }
        return new ParsedFSDeclarationNode();
    }

    /**
     * {@inheritDoc}
     *
     * @param arcType {@inheritDoc}
     * @param allowTime {@inheritDoc}
     * @return a <code>FSArcFactory</code> instance configured according to
     *   the method parameters.
     **/
    protected ArcFactory getArcFactory(int arcType, boolean allowTime) {
        return new FSArcFactory(arcType, allowTime);
    }
}