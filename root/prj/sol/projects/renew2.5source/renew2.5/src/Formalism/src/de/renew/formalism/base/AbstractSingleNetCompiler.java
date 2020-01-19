package de.renew.formalism.base;

import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;
import de.renew.net.UplinkInscription;
import de.renew.net.loading.NetLoader;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowDeclarationNode;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowPreprocessor;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


/**
 * Default implementation framework suitable for most net compilers.
 * <p>
 * The <code>AbstractSingleNetCompiler</code> provides a default
 * compilation process that iterates over the declaration node, places,
 * transitions, arcs and other node inscriptions of a net.
 * To obtain a working comiler implementation, the abstract methods
 * specified by this class and the <code>ShadowCompiler</code> interface
 * have to be implemented with respect to the semantics of the desired
 * net formalism. These are:
 * </p>
 * <ul>
 * <li>{@link #parseDeclarations},</li>
 * <li>{@link #compilePlaceInscriptions},</li>
 * <li>{@link #compileTransitionInscription},</li>
 * <li>{@link #compileNonStandardTransitionInscription} (optional),</li>
 * <li>{@link #compileArc}, and</li>
 * <li>the various syntax <code>check</code>... methods declared in the
 *     {@link ShadowCompiler} interface.</li>
 * </ul>
 * <p>
 * The <code>AbstractSingleNetCompiler</code> additionally manages the
 * shadow lookup (get and set methods) and an optional loopback net loader
 * (protected field).  However, the latter is not used by default.
 * </p>
 *
 * @author Olaf Kummer
 * @author Michael Duvigneau (documentation)
 * @since Renew 2.0
 **/
public abstract class AbstractSingleNetCompiler implements ShadowCompiler {

    /**
     * The configured lookup from shadow nets to compiled nets for the
     * current compilation process.
     **/
    protected ShadowLookup lookup;

    /**
     * The configured net loader to use when a net is missing amidst
     * compilation.
     * <p>
     * Before the net loader may be engaged, the compiler implementation
     * has to be sure that the missing net is neither in the configured
     * lookup nor in the set of known compiled nets (possibly by using
     * {@link Net#isKnownNet}, for example).
     * </p>
     **/
    protected NetLoader loopbackNetLoader;

    /**
     * Counter that is used to name unnamed places.
     **/
    protected transient int placeNum = 0;

    /**
     * Counter that is used to name unnamed transitions.
     **/
    protected transient int transitionNum = 0;

    /**
     * {@inheritDoc}
     *
     * @param lookup {@inheritDoc}
     **/
    public void setShadowLookup(ShadowLookup lookup) {
        this.lookup = lookup;
    }

    /**
     * Returns the lookup configured for this compiler.
     *
     * @return the configured <code>ShadowLookup</code>.
     *   Returns <code>null</code>, if there is none.
     **/
    public ShadowLookup getLookup() {
        return lookup;
    }

    /**
     * {@inheritDoc}
     *
     * @param loopbackNetLoader {@inheritDoc}
     **/
    public void setLoopbackNetLoader(NetLoader loopbackNetLoader) {
        this.loopbackNetLoader = loopbackNetLoader;
    }

    /**
     * {@inheritDoc}
     *
     * @param name  {@inheritDoc}
     * @return The default implementation returns a standard
     *   <code>Net</code> object with the given name.
     *
     **/
    public Net createNet(String name) {
        return new Net(name);
    }

    /**
     * {@inheritDoc}
     *
     * @return The default implementation returns an empty array because no
     *   preprocessors are needed.
     **/
    public ShadowPreprocessor[] getRequiredPreprocessors() {
        return new ShadowPreprocessor[0];
    }

    /**
     * Compiles all inscriptions of the given <code>ShadowPlace</code> into
     * the given compiled <code>Place</code>.
     * <p>
     * It is not recommended to compile arc inscriptions inside this
     * method, although they are accessible as place inscriptions.
     * The problem is that the adjacent transitions have not been compiled
     * yet.
     * </p>
     *
     * @param shadowPlace  the current <code>ShadowPlace</code>
     * @param place  the associated compiled <code>Place</code>
     * @exception SyntaxException  if the inscriptions cannot be
     *   successfully compiled.
     **/
    protected abstract void compilePlaceInscriptions(ShadowPlace shadowPlace,
                                                     Place place)
            throws SyntaxException;

    /**
     * Compiles the given place into the given net.
     * <p>
     * The default implementation creates the corresponding compiled place,
     * determines its name, copies ID and trace information, and calls
     * {@link #compilePlaceInscriptions}.
     * </p>
     *
     * @param shadowPlace the <code>ShadowPlace</code> to compile
     * @param net the compiled <code>Net</code> where the place belongs to
     * @exception SyntaxException  if the place cannot be compiled
     *   successfully.
     **/
    protected void compile(ShadowPlace shadowPlace, Net net)
            throws SyntaxException {
        // Determine the name.
        String pname = shadowPlace.getName();
        if (pname == null) {
            placeNum++;
            pname = "P" + placeNum;
        }

        // Create the new place;
        Place place = new Place(net, pname,
                                new NetElementID(shadowPlace.getID()));
        place.setTrace(shadowPlace.getTrace());

        String pcomment = shadowPlace.getComment();
        if (pcomment != null) {
            place.setComment(pcomment);
        }

        lookup.set(shadowPlace, place);

        compilePlaceInscriptions(shadowPlace, place);
    }

    /**
     * Compiles the given arc.  The default compilation process guarantees
     * that both the adjacent transition and place have been compiled
     * before and are available in the lookup.
     *
     * @param shadowArc  the <code>ShadowArc</code> to compile.
     * @exception SyntaxException  if the arc cannot be
     *   compiled successfully.
     **/
    protected abstract void compileArc(ShadowArc shadowArc)
            throws SyntaxException;

    /**
     * Compiles the given textual transition inscription.  The default
     * compilation process guarantees that the corresponding transition has
     * been compiled before and is available in the lookup.
     * <p>
     * The returned collection of inscriptions will be attached to the
     * respective transition by the default compilation process, so
     * implementations of this method should not do it by themselves.
     * </p>
     *
     * @param inscription  the <code>ShadowInscription</code> to compile
     * @return a collection of compiled {@link TransitionInscription}
     *   objects
     * @exception SyntaxException if the transition inscription cannot be
     *   compiled successfully.
     **/
    protected abstract Collection<TransitionInscription> compileTransitionInscription(ShadowInscription inscription)
            throws SyntaxException;

    /**
     * Compiles the given non-standard transition inscription.  The default
     * compilation process guarantees that the corresponding transition has
     * been compiled before and is available in the lookup.
     * <p>
     * The default implementation throws a <code>SyntaxException</code>
     * that states that non-standard inscriptions are not supported.
     * </p>
     *
     * @param elem  the <code>ShadowNetElement</code> to compile that
     *              denotes the transition inscription.
     * @exception SyntaxException if the transition inscription cannot be
     *   compiled successfully.
     **/
    protected void compileNonStandardTransitionInscription(ShadowNetElement elem)
            throws SyntaxException {
        throw new SyntaxException("Unsupported type of transition inscription")
              .addObject(elem);
    }

    /**
     * Compiles all transition inscriptions and arcs adjacent to the given
     * transition.  The default compilation process guarantees that the
     * corresponding transition has been compiled before and is available
     * in the lookup.
     * <p>
     * The default implementation calls the appropriate compile method for
     * each inscription, namely {@link #compileTransitionInscription},
     * {@link #compileNonStandardTransitionInscription} or {@link #compileArc}.
     * </p>
     *
     * @param shadowTransition  the <code>ShadowTransition</code> whose
     *   inscriptions are to be compiled.
     * @param parsedInscriptions  a list that is filled during the
     *   compilation.  When the method returns, it contains all standard
     *   textual {@link TransitionInscription} objects that have been
     *   parsed successfully.  Arcs and non-standard inscriptions are not
     *   included.
     * @param errorShadows   a list that is filled during the compilation.
     *   When the method returns, it contains the same number of elements
     *   as <code>parsedInscriptions</code>, namely the individual
     *   {@link ShadowInscription} objects corresponding to the parsed
     *   inscriptions.
     * @exception SyntaxException  if any inscription cannot be compiled
     *   successfully.
     **/
    protected void compileTransitionInscriptions(ShadowTransition shadowTransition,
                                                 Vector<TransitionInscription> parsedInscriptions,
                                                 Vector<ShadowInscription> errorShadows)
            throws SyntaxException {
        // The transition already exists.
        // Only insert the inscriptions.
        Iterator<ShadowNetElement> inscriptions = shadowTransition.elements()
                                                                  .iterator();
        while (inscriptions.hasNext()) {
            Object elem = inscriptions.next();
            if (elem instanceof ShadowInscription) {
                ShadowInscription inscription = (ShadowInscription) elem;

                Iterator<TransitionInscription> subinscriptions = null;
                try {
                    subinscriptions = compileTransitionInscription((ShadowInscription) elem)
                                          .iterator();
                } catch (SyntaxException e) {
                    throw e.addObject(inscription);
                }

                while (subinscriptions.hasNext()) {
                    parsedInscriptions.addElement(subinscriptions.next());
                    errorShadows.addElement(inscription);
                }
            } else if (elem instanceof ShadowArc) {
                compileArc((ShadowArc) elem);
            } else if (elem instanceof ShadowNetElement) {
                compileNonStandardTransitionInscription((ShadowNetElement) elem);
            } else {
                throw new SyntaxException("Unsupported type of transition inscription")
                      .addObject(elem);
            }
        }
    }

    /**
     * Compiles the given transition along with all its inscriptions and
     * arcs.
     * <p>
     * The default implementation creates the corresponding compiled
     * transition, determines its name, copies ID and trace information,
     * and calls {@link #compileTransitionInscriptions}.  Afterwards,
     * a check for multiple uplink inscriptions is applied.  Finally, all
     * compiled inscriptions are attached to the compiled transition.
     * </p>
     *
     * @param shadowTransition a <code>ShadowTransition</code> value
     * @param net a <code>Net</code> value
     * @exception SyntaxException if an error occurs
     **/
    protected void compile(ShadowTransition shadowTransition, Net net)
            throws SyntaxException {
        // Determine the name.
        String tname = shadowTransition.getName();
        if (tname == null) {
            transitionNum++;
            tname = "T" + transitionNum;
        }

        // Create the new transition.
        Transition transition = new Transition(net, tname,
                                               new NetElementID(shadowTransition
                                                                .getID()));
        transition.setTrace(shadowTransition.getTrace());

        String pcomment = shadowTransition.getComment();
        if (pcomment != null) {
            transition.setComment(pcomment);
        }

        lookup.set(shadowTransition, transition);


        // Prepare a list to hold the inscriptions.
        Vector<TransitionInscription> parsedInscriptions = new Vector<TransitionInscription>();
        Vector<ShadowInscription> errorShadows = new Vector<ShadowInscription>();


        // Insert the inscriptions.
        compileTransitionInscriptions(shadowTransition, parsedInscriptions,
                                      errorShadows);

        // Check for multiple uplinks.
        int uplinkCount = 0;
        for (int i = 0; i < parsedInscriptions.size(); i++) {
            if (parsedInscriptions.elementAt(i) instanceof UplinkInscription) {
                uplinkCount++;
            }
        }

        if (uplinkCount > 1) {
            SyntaxException e = new SyntaxException("Transition has more than one uplink.");
            for (int i = 0; i < parsedInscriptions.size(); i++) {
                if (parsedInscriptions.elementAt(i) instanceof UplinkInscription) {
                    e.addObject(errorShadows.elementAt(i));
                }
            }
            throw e;
        }

        // Insert inscriptions.
        for (int i = 0; i < parsedInscriptions.size(); i++) {
            transition.add(parsedInscriptions.elementAt(i));
        }
    }

    /**
     * Compiles all declarations of the given net.  The default compilation
     * process calls this method before all other net elements are compiled.
     * <p>
     * If an implementation of this method needs to find the declaration
     * node, it can delegate that task to {@link #findDeclarationNode}.
     * </p>
     *
     * @param shadowNet  the net whose declarations should be compiled
     * @exception SyntaxException  if the declarations cannot be
     *   successfully compiled.
     **/
    public abstract void parseDeclarations(ShadowNet shadowNet)
            throws SyntaxException;

    /**
     * Determines the single declaration node of the given net.
     *
     * @param shadowNet the <code>ShadowNet</code> to search in.
     * @return the declaration node of the shadow net.  Returns
     *   <code>null</code>, if no declaration node could be found.
     * @exception SyntaxException  if more than one declaration node
     *   exists.
     **/
    protected ShadowDeclarationNode findDeclarationNode(ShadowNet shadowNet)
            throws SyntaxException {
        // Search for declarations.
        Iterator<ShadowNetElement> iterator = shadowNet.elements().iterator();
        ShadowDeclarationNode node = null;
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowDeclarationNode) {
                // Have we already found a declaration node?
                if (node != null) {
                    SyntaxException e = new SyntaxException("Only one declaration node is allowed.");
                    e.addObject(node);
                    e.addObject(elem);
                    while (iterator.hasNext()) {
                        elem = iterator.next();
                        if (elem instanceof ShadowDeclarationNode) {
                            e.addObject(elem);
                        }
                    }
                    throw e;
                }


                // Remember the declaration node we found.
                node = (ShadowDeclarationNode) elem;
            }
        }

        return node;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation uses the following process to compile the
     * net:
     * </p>
     * <ol>
     * <li>declarations: delegated to {@link #parseDeclarations}</li>
     * <li>places: each place delegated to {@link #compile(ShadowPlace, Net)}</li>
     * <li>transitions: for each transition delegated to
     *     {@link #compile(ShadowTransition, Net)};<br>
     *     arcs are compiled along with their adjacent transition</li>
     * </ol>
     * <p>
     * Inscriptions are compiled along with the net element they belong to.
     * </p>
     * @param shadowNet {@inheritDoc}
     * @throws SyntaxException {@inheritDoc}
     **/
    public void compile(ShadowNet shadowNet) throws SyntaxException {
        // Find the net that is compiled.
        Net net = lookup.getNet(shadowNet.getName());

        // Compile the individual net elements.
        // First the declaration node, if any.
        parseDeclarations(shadowNet);


        // Second the places.
        Iterator<ShadowNetElement> iterator = shadowNet.elements().iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowPlace) {
                compile((ShadowPlace) elem, net);
            }
        }


        // Third the transitions.
        iterator = shadowNet.elements().iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowTransition) {
                compile((ShadowTransition) elem, net);
            }
        }
    }
}