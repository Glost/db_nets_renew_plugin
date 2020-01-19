package de.renew.formalism.java;

import de.renew.expression.ConstantExpression;
import de.renew.expression.Expression;
import de.renew.expression.VariableExpression;

import de.renew.formalism.base.AbstractSingleNetCompiler;

import de.renew.net.ExpressionTokenSource;
import de.renew.net.Net;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;
import de.renew.net.UplinkInscription;
import de.renew.net.arc.Arc;
import de.renew.net.arc.ClearArc;
import de.renew.net.inscription.ActionInscription;
import de.renew.net.inscription.DownlinkInscription;
import de.renew.net.inscription.ExpressionInscription;
import de.renew.net.inscription.GuardInscription;

import de.renew.shadow.SequentialOnlyExtension;
import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowDeclarationNode;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;

import de.renew.unify.Tuple;

import de.renew.util.Types;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/**
 * Compiles a Java reference net.
 *
 * @author Olaf Kummer
 * @author Michael Duvigneau (documentation)
 **/
public class SingleJavaNetCompiler extends AbstractSingleNetCompiler {
    private boolean allowTimeInscriptions;
    private boolean wantEarlyTokens;
    private transient Hashtable<ShadowPlace, Class<?>> placeTypes = new Hashtable<ShadowPlace, Class<?>>(); // Maps shadow places to types.

    /**
     * Stores the declaration node, once it has been parsed.
     **/
    protected ParsedDeclarationNode declaration;

    /**
     * Creates a new <code>SingleJavaNetCompiler</code> instance.
     *
     * @param allowDangerousArcs (UNUSED) whether arcs without partial order
     *   semantics (inhibitor and clear arcs) are allowed in the net.
     * @param allowTimeInscriptions  whether time inscriptions are allowed
     *   in the net.
     * @param wantEarlyTokens  whether instances of nets compiled by this
     *   compiler should create initial markings early.
     **/
    public SingleJavaNetCompiler(boolean allowDangerousArcs,
                                 boolean allowTimeInscriptions,
                                 boolean wantEarlyTokens) {
        this.allowTimeInscriptions = allowTimeInscriptions;
        this.wantEarlyTokens = wantEarlyTokens;
    }

    /**
     * Creates a standard {@link Net} object with the given name and
     * transfers the <code>wantEarlyTokens</code> flag to the net.
     *
     * @param name {@inheritDoc}
     * @return a standard {@link Net} object
     **/
    public Net createNet(String name) {
        Net net = super.createNet(name);
        net.setEarlyTokens(wantEarlyTokens);
        return net;
    }

    /**
     * Creates an inscription parser for the given inscription.
     * <p>
     * This method may be overridden by subclasses. The default
     * implementation instantiates a {@link JavaNetParser} and configures
     * the parser to use the {@link #loopbackNetLoader}.
     * </p>
     *
     * @param inscr the inscription to parse.
     * @return the parser instance for the inscription.
     **/
    protected InscriptionParser makeParser(String inscr) {
        JavaNetParser parser = new JavaNetParser(new StringReader(inscr));
        parser.setNetLoader(loopbackNetLoader);
        return parser;
    }

    /**
     * Converts a JavaCC <code>ParseException</code> into a Renew
     * <code>SyntaxException</code>.
     *
     * @param e  the parser exception to convert.
     * @return  a <code>SyntaxException</code> with all information from the
     *          given parser exception.
     **/
    public static SyntaxException makeSyntaxException(ParseException e) {
        Token t = e.currentToken;
        if (t.next != null) {
            t = t.next;
        }

        if (e instanceof ExtendedParseException) {
            Object o = ((ExtendedParseException) e)
                           .getProblemSpecificInformation();
            if (o != null) {
                return new SyntaxException(e.getMessage(), null, t.beginLine,
                                           t.beginColumn, e, o);
            }
        }
        return new SyntaxException(e.getMessage(), null, t.beginLine,
                                   t.beginColumn, e);

    }

    // ------------------------------------------------------ syntax checks --
    public String checkDeclarationNode(String inscr, boolean special,
                                       ShadowNet shadowNet)
            throws SyntaxException {
        parseDeclarationNode(inscr);
        return "declaration";
    }

    public String checkArcInscription(String inscr, boolean special,
                                      ShadowNet shadowNet)
            throws SyntaxException {
        makeDeclarationNode(shadowNet);
        parseArcInscription(inscr);
        return "inscription";
    }

    public String checkTransitionInscription(String inscription,
                                             boolean special,
                                             ShadowNet shadowNet)
            throws SyntaxException {
        makeDeclarationNode(shadowNet);
        Collection<TransitionInscription> inscriptions = makeInscriptions(inscription,
                                                                          null,
                                                                          false);

        if (inscriptions.size() != 1) {
            return null;
        }

        Object res = inscriptions.iterator().next();
        if (res instanceof ActionInscription) {
            return "action";
        } else if (res instanceof GuardInscription) {
            return "guard";
        } else if (res instanceof UplinkInscription) {
            return "uplink";
        } else if (res instanceof DownlinkInscription) {
            return "downlink";
        } else if (res instanceof ExpressionInscription) {
            return "expression";
        } else {
            return null;
        }
    }

    public String checkPlaceInscription(String inscr, boolean special,
                                        ShadowNet shadowNet)
            throws SyntaxException {
        makeDeclarationNode(shadowNet);
        Collection<Object> placeinscriptions = parsePlaceInscription(inscr);

        if (placeinscriptions.size() != 1) {
            return null;
        }

        Object element = placeinscriptions.iterator().next();
        if (element instanceof Class) {
            return "type";
        } else if (element instanceof PlaceBehaviourModifier) {
            return "placebehaviour";
        } else {
            return "initialMarking";
        }
    }

    // --------------------------------------------------- place processing --


    /**
     * Compiles all <code>ShadowInscription</code>s attached to the given
     * place.  Determines the type, behavior and initial marking of the
     * place.  Arcs are ignored, other inscription types produce a syntax
     * exception.
     *
     * @param shadowPlace  {@inheritDoc}
     * @param place  {@inheritDoc}
     * @throws SyntaxException  {@inheritDoc}
     **/
    protected void compilePlaceInscriptions(ShadowPlace shadowPlace, Place place)
            throws SyntaxException {
        // Store the initialisation inscriptions for later use.
        Vector<Object> parsedInscriptions = new Vector<Object>();
        Vector<ShadowNetElement> errorShadows = new Vector<ShadowNetElement>();


        // Insert the inscriptions.
        Iterator<ShadowNetElement> iterator = shadowPlace.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowInscription) {
                String inscr = ((ShadowInscription) elem).inscr;

                try {
                    Iterator<Object> exprEnum = parsePlaceInscription(inscr)
                                                    .iterator();
                    while (exprEnum.hasNext()) {
                        parsedInscriptions.addElement(exprEnum.next());
                        errorShadows.addElement(elem);
                    }
                } catch (SyntaxException e) {
                    throw e.addObject(elem);
                }
            } else if (elem instanceof ShadowArc) {
            } else {
                throw new SyntaxException("Unsupported place inscription").addObject(shadowPlace)
                                                                          .addObject(elem);
            }
        }


        // Remember the type of this place.
        Class<?> type = Types.UNTYPED;
        int typeCount = 0;
        PlaceBehaviourModifier behaviour = new PlaceBehaviourModifier(Place.MULTISETPLACE);
        int behaviourCount = 0;

        for (int i = 0; i < parsedInscriptions.size(); i++) {
            if (parsedInscriptions.elementAt(i) instanceof Class) {
                type = (Class<?>) parsedInscriptions.elementAt(i);
                typeCount++;
            } else if (parsedInscriptions.elementAt(i) instanceof PlaceBehaviourModifier) {
                behaviour = (PlaceBehaviourModifier) parsedInscriptions
                                .elementAt(i);
                behaviourCount++;
            }
        }

        // Check for multiple types.
        if (typeCount > 1) {
            SyntaxException e = new SyntaxException("Place is typed more than once.");
            for (int i = 0; i < parsedInscriptions.size(); i++) {
                if (parsedInscriptions.elementAt(i) instanceof Class) {
                    e.addObject(errorShadows.elementAt(i));
                }
            }
            throw e;
        }

        // Check for multiple behaviours.
        if (behaviourCount > 1) {
            SyntaxException e = new SyntaxException("Place has more than one assigned behaviour.");
            for (int i = 0; i < parsedInscriptions.size(); i++) {
                if (parsedInscriptions.elementAt(i) instanceof PlaceBehaviourModifier) {
                    e.addObject(errorShadows.elementAt(i));
                }
            }
            throw e;
        }

        // Remember the type information for this place.
        if (type != Types.UNTYPED) {
            placeTypes.put(shadowPlace, type);
        }


        // Set the behaviour information for this place.
        place.setBehaviour(behaviour.behaviour);

        // Now add the initial markings.
        for (int i = 0; i < parsedInscriptions.size(); i++) {
            if (parsedInscriptions.elementAt(i) instanceof TypedExpression) {
                TypedExpression expr = (TypedExpression) parsedInscriptions
                                           .elementAt(i);

                Expression castedExpression = null;
                try {
                    castedExpression = JavaNetHelper.makeCastedOutputExpression(type,
                                                                                expr);
                } catch (SyntaxException e) {
                    throw e.addObject(errorShadows.elementAt(i));
                }
                place.add(new ExpressionTokenSource(castedExpression));
            }
        }
    }

    /**
     * Retrieves the type of a place that has already been compiled.
     *
     * @param place  the place to query.
     * @return  the assigned type. Returns {@link Types.UNTYPED}, if there
     *   is no type assignment known.
     **/
    Class<?> getType(ShadowPlace place) {
        if (placeTypes.containsKey(place)) {
            return placeTypes.get(place);
        } else {
            return Types.UNTYPED;
        }
    }

    /**
     * Parses the given place inscription.
     * Uses a fresh inscription parser with all known declarations.
     * @param inscr  the inscription to parse.
     * @return a collection of {@link TimedExpression} that contain a
     *   {@link TypedExpression} parsed from the inscription.
     * @exception SyntaxException  if the inscription cannot be parsed
     *   successfully.
     **/
    Collection<Object> parsePlaceInscription(String inscr)
            throws SyntaxException {
        if ((inscr != null) && !inscr.equals("")) {
            InscriptionParser parser = makeParser(inscr);
            parser.setDeclarationNode(declaration);
            try {
                return parser.PlaceInscription();
            } catch (ParseException e) {
                // I really do not know what this could be.
                // I'll give out the error message of the
                // second parse, although it might be
                // appropriate to tell the user that I also
                // tried to parse a type.
                throw makeSyntaxException(e);
            }
        }

        return Collections.emptySet();
    }

    // ----------------------------------------------------- arc processing --


    /**
     * Compiles the given arc and its inscriptions.
     * <p>
     * Delegates to {@link #compileArcInscriptions} with the appropriate
     * {@link ArcFactory}, except for clear arcs.  Clear arcs are delegated
     * to {@link #compileClearArc}.
     * </p>
     *
     * @param shadowArc  {@inheritDoc}
     * @throws SyntaxException  {@inheritDoc}
     **/
    protected void compileArc(ShadowArc shadowArc) throws SyntaxException {
        ArcFactory factory = getArcFactory(shadowArc);
        if (shadowArc.shadowArcType == ShadowArc.doubleHollow) {
            compileClearArc(shadowArc);
        } else {
            compileArcInscriptions(shadowArc, factory);
        }
    }

    /**
     * Retrieves the single textual inscription of the given arc.
     *
     * @param arc  the arc that is queried.
     * @return  the arc inscription.
     *   Returns <code>null</code>, if there is none.
     * @exception SyntaxException  if more than one inscription was found.
     * @exception ClassCastException  if the arc inscription is not a
     *   <code>ShadowInscription</code>.
     **/
    protected ShadowInscription getSingleArcInscription(ShadowArc arc)
            throws SyntaxException {
        Set<ShadowNetElement> inscriptions = arc.elements();
        if (inscriptions.size() >= 2) {
            SyntaxException e = new SyntaxException("This arc must not be inscribed "
                                                    + "multiple times.")
                                    .addObject(arc);
            Iterator<ShadowNetElement> iterator = inscriptions.iterator();
            while (iterator.hasNext()) {
                e.addObject(iterator.next());
            }
            throw e;
        } else if (inscriptions.size() == 1) {
            return (ShadowInscription) inscriptions.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Parses the given inscription as a variable.
     * Uses a fresh inscription parser with all known declarations.
     *
     * @param inscr  the inscription to parse.
     * @return  the parsed variable expression
     * @exception SyntaxException if the variable cannot be parsed
     *   successfully.
     **/
    protected TypedExpression parseVariable(String inscr)
            throws SyntaxException {
        InscriptionParser parser = makeParser(inscr);
        parser.setDeclarationNode(declaration);
        try {
            return parser.VariableInscription();
        } catch (ParseException e) {
            throw makeSyntaxException(e);
        }
    }

    /**
     * Compiles a clear arc.
     *
     * @param shadowArc  the arc to compile.
     * @exception SyntaxException if the arc cannot be compiled
     *   successfully.
     **/
    protected void compileClearArc(ShadowArc shadowArc)
            throws SyntaxException {
        Transition transition = lookup.get(shadowArc.transition);
        Place place = lookup.get(shadowArc.place);
        Class<?> placeType = getType(shadowArc.place);

        ShadowInscription shadowInscription = getSingleArcInscription(shadowArc);
        String inscr = null;
        if (shadowInscription != null) {
            inscr = shadowInscription.inscr;
        }

        if (inscr == null || inscr.equals("")) {
            // For empty clear arcs, prepare an anonymous variable.
            ClearArc arc = new ClearArc(place, transition,
                                        new VariableExpression(Types.UNTYPED,
                                                               null),
                                        Object.class);
            arc.setTrace(shadowArc.getTrace());
            transition.add(arc);
        } else {
            // We have got a expression to parse.
            TypedExpression expr;
            try {
                expr = parseVariable(inscr);
            } catch (SyntaxException e) {
                throw e.addObject(shadowInscription);
            }
            Class<?> exprType = expr.getType();

            Class<?> elementType;
            if (placeType == Types.UNTYPED) {
                if (exprType != Types.UNTYPED) {
                    throw new SyntaxException("Cannot clear untyped place using "
                                              + "typed variable.").addObject(shadowArc);
                }


                // If primitive values happen to occur in the
                // place, they are wrapped in Value objects.
                //
                // This exposes the Value class to users, so maybe we should
                // complain? Or should we disable the transition at runtime,
                // if primitive values occur in the place?
                elementType = Object.class;
            } else {
                if (exprType == Types.UNTYPED) {
                    elementType = placeType;
                } else if (!exprType.isArray()) {
                    throw new SyntaxException("Variable of array type expected.")
                          .addObject(shadowInscription);
                } else {
                    elementType = exprType.getComponentType();

                    if (!Types.allowsLosslessWidening(placeType, elementType)) {
                        throw new SyntaxException("Cannot losslessly convert "
                                                  + JavaHelper
                                  .makeTypeErrorString(placeType) + " to "
                                                  + JavaHelper
                                  .makeTypeErrorString(elementType) + ".")
                                  .addObject(shadowInscription);
                    }
                }
            }

            ClearArc arc = new ClearArc(place, transition,
                                        expr.getExpression(), elementType);
            arc.setTrace(shadowArc.getTrace());
            transition.add(arc);
        }
    }

    /**
     * Generates timed and typed expressions from the given arc
     * inscription.
     * <p>
     * Delegates to {@link #parseArcInscription(String)} and checks the
     * parsed expressions.  Marks the net as sequential-only if time
     * expressions are encountered.
     * </p>
     * TODO: This method does not do a lot. Simply checking for
     *       void expressions now. Possible simplification.
     * @param inscription  the inscription to parse.
     * @return a collection of timed and typed expressions (see return
     * @exception SyntaxException  if the inscription cannot be parsed
     *   successfully or results in a <code>void</code> expression.
     **/
    protected Collection<TimedExpression> makeArcExpressions(String inscription)
            throws SyntaxException {
        Iterator<TimedExpression> timedExprs = parseArcInscription(inscription)
                                                   .iterator();
        List<TimedExpression> seq = new ArrayList<TimedExpression>();

        while (timedExprs.hasNext()) {
            TimedExpression expr = timedExprs.next();

            if (expr.getExpression().getType() == Void.TYPE) {
                throw new SyntaxException("Cannot use void expressions as arc inscriptions.");
            }
            if (expr.isTimed()) {
                setSequential(true);
            }

            seq.add(expr);
        }

        return seq;
    }

    /**
     * Generates default expressions for an arc without inscription.
     * @return a collection of timed and typed expressions.
     * @exception SyntaxException  if empty arc expressions cannot be
     *   parsed.
     **/
    protected Collection<TimedExpression> makeEmptyArcExpressions()
            throws SyntaxException {
        return makeArcExpressions(null);
    }

    /**
     * Compiles the given collection of expressions (that have been parsed
     * from an arc inscription before) in the given context.
     * <p>
     * The implementation first checks the validity of any associated time
     * expression, then delegates the compilation to the arc factory.
     * </p>
     *
     * @param exprs  a <code>Collection</code> of parsed timed and typed
     *   expressions.
     * @param placeType  the type assigned to the arc's adjacent place.
     * @param factory  the arc factory that knows about the arc type.
     * @param place  the compiled place adjacent to the arc.
     * @param transition the compiled transition adjacent to the arc.
     * @param trace  the trace flag of the arc.
     * @exception SyntaxException  if the arc inscription cannot be
     *   compiled successfully.
     **/
    protected void compileSingleArcInscription(Collection<TimedExpression> exprs,
                                               Class<?> placeType,
                                               ArcFactory factory, Place place,
                                               Transition transition,
                                               boolean trace)
            throws SyntaxException {
        Iterator<TimedExpression> iterator = exprs.iterator();
        while (iterator.hasNext()) {
            TimedExpression timedExpr = iterator.next();

            if (factory.allowsTime() && allowTimeInscriptions) {
                // Verify that the time expression is numeric.
                if (timedExpr.isTimed() && timedExpr.getTime().isTyped()) {
                    if (!Types.allowsWideningConversion(timedExpr.getTime()
                                                                         .getType(),
                                                                Double.TYPE)) {
                        throw new SyntaxException("Non-numeric time expression.");
                    }
                }
            } else if (timedExpr.isTimed()) {
                if (allowTimeInscriptions) {
                    throw new SyntaxException("This arc type does not allow time inscriptions.");
                } else {
                    throw new SyntaxException("Time annotations are not allowed.");
                }
            }

            factory.compileArc(place, transition, trace, placeType, timedExpr);
        }
    }

    /**
     * Compiles all inscriptions of the given arc, using the given arc
     * factory.
     * <p>
     * For each inscription, first delegates to {@link #makeArcExpressions}.
     * If there is no inscription, and the given arc factory allows empty
     * arc inscriptions, first delegates to {@link #makeEmptyArcExpressions}.
     * Afterwards, the parsed expressions along with the arc's context are
     * passed to {@link #compileSingleArcInscription}.
     * </p>
     *
     * @param shadowArc the arc whose inscriptions are to be compiled.
     * @param factory  the arc factory that knows about the arc type.
     * @exception SyntaxException if any inscription cannot be compiled
     *   successfully.
     **/
    protected void compileArcInscriptions(ShadowArc shadowArc,
                                          ArcFactory factory)
            throws SyntaxException {
        Transition transition = lookup.get(shadowArc.transition);
        Place place = lookup.get(shadowArc.place);

        Iterator<ShadowNetElement> inscriptions = shadowArc.elements().iterator();
        if (inscriptions.hasNext()) {
            do {
                ShadowInscription inscription = (ShadowInscription) inscriptions
                                                .next();

                // Create an individual arc for each inscription.
                try {
                    compileSingleArcInscription(makeArcExpressions(inscription.inscr),
                                                getType(shadowArc.place),
                                                factory, place, transition,
                                                shadowArc.getTrace());
                } catch (SyntaxException e) {
                    throw e.addObject(inscription);
                }
            } while (inscriptions.hasNext());
        } else {
            // Create a single arc with the default inscription.
            try {
                factory.emptyArcCheck();

                compileSingleArcInscription(makeEmptyArcExpressions(),
                                            getType(shadowArc.place), factory,
                                            place, transition,
                                            shadowArc.getTrace());
            } catch (SyntaxException e) {
                throw e.addObject(shadowArc);
            }
        }
    }

    /**
     * Creates a simple arc factory for the given arc type.
     *
     * @param arcType  the type of the arc (see {@link Arc} constants).
     * @param allowTime  whether time inscriptions are allowed on the arc.
     * @return the generated {@link SimpleArcFactory}.
     **/
    protected ArcFactory getArcFactory(int arcType, boolean allowTime) {
        return new SimpleArcFactory(arcType, allowTime);
    }

    /**
     * Retrieves the appropriate compiled arc factory for the given shadow
     * arc.  Sets the sequential-only flag when certain arc types are
     * encountered.
     * <p>
     * Override this method to map some or all shadow arc types to
     * different simulator arc types.
     * </p>
     * @param shadowArc the shadow arc that determines the factory type.
     * @return the appropriate arc factory.
     * @exception SyntaxException if an appropriate arc factory cannot be
     *   determined.
     *
     **/
    protected ArcFactory getArcFactory(ShadowArc shadowArc)
            throws SyntaxException {
        switch (shadowArc.shadowArcType) {
        case ShadowArc.test:
            return getArcFactory(Arc.test, false);
        case ShadowArc.both:
            return getArcFactory(Arc.bothOT, true);
        case ShadowArc.ordinary:
            return getArcFactory(shadowArc.placeToTransition ? Arc.in : Arc.out,
                                 true);
        case ShadowArc.inhibitor:
            setSequential(true);
            return getArcFactory(Arc.inhibitor, false);
        case ShadowArc.doubleOrdinary:
            if (shadowArc.placeToTransition) {
                return FlexibleInArcFactory.INSTANCE;
            } else {
                return FlexibleOutArcFactory.INSTANCE;
            }
        case ShadowArc.doubleHollow:
            setSequential(true);
            return null; // special case: there is no factory!
        }
        throw new SyntaxException("Unsupported arc type.").addObject(shadowArc);
    }

    /**
     * Parses the given arc inscription.
     * Uses a fresh inscription parser with all known declarations.
     * An empty inscription is parsed into a 0-tuple.
     * @param inscr  the inscription to parse.
     * @return a collection of {@link TimedExpression} that contain a
     *   {@link TypedExpression} parsed from the inscription.
     * @exception SyntaxException  if the inscription cannot be parsed
     *   successfully.
     **/
    protected Collection<TimedExpression> parseArcInscription(String inscr)
            throws SyntaxException {
        if (inscr != null && !inscr.equals("")) {
            InscriptionParser parser = makeParser(inscr);
            parser.setDeclarationNode(declaration);
            try {
                return parser.ArcInscription();
            } catch (ParseException e) {
                throw makeSyntaxException(e);
            }
        } else {
            return Collections.singleton(new TimedExpression(new TypedExpression(Tuple.class,
                                                                                 new ConstantExpression(Tuple.class,
                                                                                                        new Tuple(0))),
                                                             null));
        }
    }

    // ---------------------------------------------- transition processing --


    /**
     * Compiles the given textual transition inscription.
     * <p>
     * Delegates to
     * {@link #makeInscriptions(ShadowInscription, ShadowLookup, boolean)}
     * with the current shadow lookup and the enabled net creation flag.
     * </p>
     *
     * @param inscription  {@inheritDoc}
     * @return  {@inheritDoc}
     * @throws SyntaxException  {@inheritDoc}
     **/
    protected Collection<TransitionInscription> compileTransitionInscription(ShadowInscription inscription)
            throws SyntaxException {
        return makeInscriptions(inscription, lookup, true);
    }

    /**
     * Parses {@link TransitionInscription} objects from the given
     * textual inscription.
     * <p>
     * Delegates to {@link #makeInscriptions(String, Transition, boolean)}
     * with the inscription text and the compiled <code>Transition</code>.
     * </p>
     *
     * @param inscription  the <code>ShadowInscription</code> to parse.
     * @param lookup  the compilation lookup.
     * @param create  whether a full compilation of net creation
     *   expressions is requested.
     * @return a <code>Collection</code> of {@link TransitionInscription}
     *   objects.
     * @exception SyntaxException if the inscription cannot be compiled
     *   successfully.
     **/
    protected Collection<TransitionInscription> makeInscriptions(ShadowInscription inscription,
                                                                 ShadowLookup lookup,
                                                                 boolean create)
            throws SyntaxException {
        // The input string must be parsed, it denotes either a
        // creation inscription, an uplink, a downlink, ...
        //
        // Syntax:
        //   x:new NetName          creation
        //   x:channel(...)         downlink
        //   :channel(...)          uplink
        //   x=new a.java.Type(...) creation
        //   x=y.method(...)        call
        //   guard ...              guard
        //   action x= ...          action
        String str = inscription.inscr;
        Transition transition = lookup.get((ShadowTransition) inscription.inscribable);

        return makeInscriptions(str, transition, create);
    }

    /**
     * Parses {@link TransitionInscription} objects from the given
     * textual inscription.
     *
     * @param str  the inscription to parse.
     * @param transition  the compiled transition the inscription belongs to.
     * @param create  whether a full compilation of net creation
     *   expressions is requested.
     * @return a <code>Collection</code> of {@link TransitionInscription}
     *   objects.
     * @exception SyntaxException if the inscription cannot be compiled
     *   successfully.
     **/
    protected Collection<TransitionInscription> makeInscriptions(String str,
                                                                 Transition transition,
                                                                 boolean create)
            throws SyntaxException {
        InscriptionParser parser = makeParser(str);
        parser.setLookup(lookup);
        parser.setDeclarationNode(declaration);

        try {
            return parser.TransitionInscription(create, transition);
        } catch (ParseException e) {
            throw makeSyntaxException(e);
        }
    }

    // ---------------------------------------- declaration node processing --


    /**
     * Parses the given declaration node.
     * <p>
     * Extracts the text from the given node and delegates to
     * {@link #parseDeclarationNode(String)}.
     * </p>
     *
     * @param declaration  the shadow declaration node to parse.
     * @return  the parsed declaration node.
     * @exception SyntaxException if the node cannot be parsed
     *   successfully.
     **/
    protected ParsedDeclarationNode compile(ShadowDeclarationNode declaration)
            throws SyntaxException {
        try {
            return parseDeclarationNode(declaration.inscr);
        } catch (SyntaxException e) {
            throw e.addObject(declaration);
        }
    }

    /**
     * Parses the given textual declarations.
     * Uses a fresh inscription parser.
     *
     * @param inscr  the declarations as text.
     * @return the parsed declaration node.
     * @exception SyntaxException if the declarations cannot be parsed
     *   successfully.
     **/
    ParsedDeclarationNode parseDeclarationNode(String inscr)
            throws SyntaxException {
        if (inscr != null) {
            InscriptionParser parser = makeParser(inscr);
            try {
                return parser.DeclarationNode();
            } catch (ParseException e) {
                throw makeSyntaxException(e);
            }
        } else {
            return makeEmptyDeclarationNode(null);
        }
    }

    /**
     * Creates an empty parsed declaration node for the given net.
     *
     * @param net  the net that has no declaration node.
     * @return  the created empty declaration node.
     **/
    protected ParsedDeclarationNode makeEmptyDeclarationNode(ShadowNet net) {
        return new ParsedDeclarationNode();
    }

    /**
     * Compiles the declarations of the given net.  For Java nets, the
     * declarations are specified in a distinct declaration node.
     * <p>
     * Delegates to {@link makeDeclarationNode}.
     * </p>
     *
     * @param shadowNet  {@inheritDoc}
     * @throws SyntaxException  {@inheritDoc}
     **/
    public void parseDeclarations(ShadowNet shadowNet)
            throws SyntaxException {
        makeDeclarationNode(shadowNet);
    }

    /**
     * Compiles the declaration node of the given net.  If the compilation
     * is successful, the parsed declaration node is stored in this
     * compiler instance for future reference.
     * <p>
     * Retrieves the declaration node from the net using
     * {@link #findDeclarationNode} and delegates its compilation to
     * {@link #compile(ShadowDeclarationNode)}.
     * If no declaration node exists, calls
     * {@link makeEmptyDeclarationNode} to create one.
     * </p>
     *
     * @param shadowNet  the net whose declaration node should be
     *   compiled.
     * @return the <code>ParsedDeclarationNode</code>
     * @exception SyntaxException if the declaration node cannot be
     *   compiled succesfully, or more than one declaration node exists.
     **/
    public ParsedDeclarationNode makeDeclarationNode(ShadowNet shadowNet)
            throws SyntaxException {
        // Compile the declaration node.
        ShadowDeclarationNode node = findDeclarationNode(shadowNet);
        if (node == null) {
            declaration = makeEmptyDeclarationNode(shadowNet);
        } else {
            declaration = compile(node);
        }
        return declaration;
    }

    // -----------------------------------------------------------------------


    /**
     * Stores the sequential-only flag for the compilation.
     * Uses the {@link SequentialOnlyExtension}.
     *
     * @param value  whether the net requires the sequential mode.
     **/
    private void setSequential(boolean value) {
        SequentialOnlyExtension seqEx = SequentialOnlyExtension.lookup(lookup);
        seqEx.setSequentialOnly(value);
    }
}