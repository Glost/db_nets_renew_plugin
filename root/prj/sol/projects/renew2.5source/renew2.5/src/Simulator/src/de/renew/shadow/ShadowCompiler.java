package de.renew.shadow;

import de.renew.net.Net;
import de.renew.net.loading.NetLoader;


/**
 * Compiles one shadow net.  Implementations of this interface are usually
 * instantiated by a {@link ShadowCompilerFactory} implementation.
 * <p>
 * An individual instance of the compiler is needed for each net to
 * compile.  However, it is not mandatory that the {@link #createNet} call
 * and the {@link #compile} call occur on the same compiler instance.
 * The information can be transferred between compiler instances by passing
 * the a common lookup object to {@link #setShadowLookup}.
 * </p>
 *
 * @author Olaf Kummer
 * @author Michael Duvigneau (documentation)
 **/
public interface ShadowCompiler {

    /**
     * Set the net loader that this compiler uses when a
     * net is found missing amidst compiling.
     *
     * @param loopbackNetLoader the net loader
     */
    public abstract void setLoopbackNetLoader(NetLoader loopbackNetLoader);

    /**
     * Set the shadow lookup that will be used for lookups during
     * compilation and that will receive the compilation results.
     *
     * @param shadowLookup the lookup
     */
    public abstract void setShadowLookup(ShadowLookup shadowLookup);

    /**
     * Create one net of the appropriate class, using the given
     * name as the net's name. This method is called before any
     * preprocessors have run and before the compilation has been
     * started.
     *
     * @param name the net's name
     * @return a net with the correct name and class
     */
    public Net createNet(String name);

    /**
     * Get the list of preprocessors that must run before this compiler
     * can start the compilation.
     *
     * @return the preprocessors in an array or the empty array,
     *   if no preprocessors must run
     */
    public ShadowPreprocessor[] getRequiredPreprocessors();

    /**
     * Compiles the given net.  It is assumed that the method
     * {@link #createNet} with the shadow net's name has been called
     * before.  The {@link Net} object created by that call is retrieved
     * from the shadow lookup and filled with the compilation results.
     * <p>
     * The effects of this method are undefined if it is called more than
     * once on the compiler instance.  It is also undefined if other
     * compiler instances handle a net with an identical name.
     * </p>
     *
     * @param shadowNet  the shadow net to compile.
     *
     * @exception SyntaxException if the net cannot be compiled
     *   successfully.
     *
     * @see #createNet
     * @see #setShadowLookup
     **/
    public abstract void compile(ShadowNet shadowNet) throws SyntaxException;

    public String checkDeclarationNode(String inscr, boolean special,
                                       ShadowNet shadowNet)
            throws SyntaxException;

    public String checkArcInscription(String inscr, boolean special,
                                      ShadowNet shadowNet)
            throws SyntaxException;

    public String checkTransitionInscription(String inscription,
                                             boolean special,
                                             ShadowNet shadowNet)
            throws SyntaxException;

    public String checkPlaceInscription(String inscr, boolean special,
                                        ShadowNet shadowNet)
            throws SyntaxException;
}