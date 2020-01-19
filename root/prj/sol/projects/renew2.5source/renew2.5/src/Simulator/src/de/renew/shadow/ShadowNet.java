package de.renew.shadow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class ShadowNet implements java.io.Serializable {
    static final long serialVersionUID = 5180248532101141649L;
    public transient Object context = null;
    private Set<ShadowNetElement> elements;
    private String name;

    /**
     * Return the compiler factory that has been specifically set
     * for this net, or <var>null</var> if this net defaults to
     * the net system compiler factory for compilation.
     **/
    private ShadowCompilerFactory compilerFactory;
    private ShadowNetSystem netSystem;

    public ShadowNet(String name, ShadowNetSystem netSystem) {
        elements = new HashSet<ShadowNetElement>();
        this.name = name;
        this.netSystem = netSystem;
        netSystem.add(this);
    }

    /**
     * Return the compiler factory that has been specifically set
     * for this net, or <var>null</var> if this net defaults to
     * the net system compiler factory for compilation.
     *
     * @return the factory
     */
    public ShadowCompilerFactory getCompilerFactory() {
        return compilerFactory;
    }

    /**
     * Set the compiler factory for use when compiling this net,
     * or <var>null</var> if compilation should default to
     * the net system compiler factory.
     *
     * @param compilerFactory the factory
     */
    public void setCompilerFactory(ShadowCompilerFactory compilerFactory) {
        this.compilerFactory = compilerFactory;
    }

    private ShadowCompiler createShadowNetCompiler() throws SyntaxException {
        if (compilerFactory != null) {
            return compilerFactory.createCompiler();
        } else if (netSystem.getDefaultCompilerFactory() != null) {
            return netSystem.getDefaultCompilerFactory().createCompiler();
        } else {
            throw new SyntaxException("No compiler or default compiler set for net "
                                      + getName(), new String[0]);
        }
    }

    ShadowCompiler createInititalizedShadowNetCompiler(ShadowLookup lookup)
            throws SyntaxException {
        ShadowCompiler compiler = createShadowNetCompiler();
        compiler.setLoopbackNetLoader(netSystem.createNetLoader(lookup));
        compiler.setShadowLookup(lookup);
        return compiler;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<ShadowNetElement> elements() {
        return Collections.unmodifiableSet(elements);
    }

    public void discard() {
        netSystem.remove(this);
    }

    /**
     * Inserts this shadow net into another shadow net system. This
     * immediately implies removal from the previous net system.
     * If the old and the new net system use different compilers, the
     * net-specific compiler attribute is modified to retain the compiler
     * assigment for the net.
     * <p>
     * This method should not be executed after compilation of the
     * shadow net: The results and relations to the compiled net
     * and other shadow nets of the previous net system are not
     * defined.
     * </p>
     **/
    public void switchNetSystem(ShadowNetSystem newNetSystem) {
        // Add net-specific compiler information if the net has none, and
        // the new net system uses a different compiler than the old one.
        ShadowCompilerFactory currentFactory = getCompilerFactory();
        if (currentFactory == null) {
            currentFactory = netSystem.getDefaultCompilerFactory();
            if ((currentFactory != null)
                        && !currentFactory.equals(newNetSystem
                            .getDefaultCompilerFactory())) {
                setCompilerFactory(currentFactory);
            }
        }

        // Switch the net system.
        netSystem.remove(this);
        netSystem = newNetSystem;
        netSystem.add(this);


        // Remove specific compiler information if the new net system has
        // the same setting as the net.        
        if ((currentFactory != null)
                    && currentFactory.equals(netSystem.getDefaultCompilerFactory())) {
            setCompilerFactory(null);
        }
    }

    void add(ShadowNetElement element) {
        elements.add(element);
    }

    void remove(ShadowNetElement element) {
        elements.remove(element);
        element.context = null;
    }

    public String toString() {
        return "ShadowNet \"" + name + "\" (" + elements.size() + " elements)";
    }

    /**
     * Check the text of a declaration node for syntax errors.
     *
     * @param newText the text that must be checked
     * @param special a boolean flag that the compiler may use to implement different node types
     */
    public String checkDeclarationNode(String newText, boolean special)
            throws SyntaxException {
        return createInititalizedShadowNetCompiler(new ShadowLookup())
                   .checkDeclarationNode(newText, special, this);
    }

    /**
     * Check the text of a transition inscription for syntax errors.
     *
     * @param inscr   the text that must be checked
     * @param special a boolean flag that the compiler may use to implement different node types
     */
    public String checkTransitionInscription(String inscr, boolean special)
            throws SyntaxException {
        return createInititalizedShadowNetCompiler(new ShadowLookup())
                   .checkTransitionInscription(inscr, special, this);
    }

    /**
     * Check the text of a place inscription for syntax errors.
     *
     * @param newText the text that must be checked
     * @param special a boolean flag that the compiler may use to implement different node types
     */
    public String checkPlaceInscription(String newText, boolean special)
            throws SyntaxException {
        return createInititalizedShadowNetCompiler(new ShadowLookup())
                   .checkPlaceInscription(newText, special, this);
    }

    /**
     * Check the text of an arc inscription for syntax errors.
     *
     * @param newText the text that must be checked
     * @param special a boolean flag that the compiler may use to implement different node types
     */
    public String checkArcInscription(String newText, boolean special)
            throws SyntaxException {
        return createInititalizedShadowNetCompiler(new ShadowLookup())
                   .checkArcInscription(newText, special, this);
    }
}