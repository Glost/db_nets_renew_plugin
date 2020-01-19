package de.renew.faformalism.compiler;

import de.renew.expression.ConstantExpression;
import de.renew.expression.Expression;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableExpression;

import de.renew.faformalism.shadow.FAShadowLookupExtension;
import de.renew.faformalism.shadow.ShadowFAArc;
import de.renew.faformalism.shadow.ShadowFAState;

import de.renew.formalism.java.InscriptionParser;
import de.renew.formalism.java.JavaNetHelper;
import de.renew.formalism.java.ParseException;
import de.renew.formalism.java.SingleJavaNetCompiler;
import de.renew.formalism.java.TypedExpression;

import de.renew.net.ExpressionTokenSource;
import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;
import de.renew.net.arc.Arc;

import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.SyntaxException;

import de.renew.util.Types;
import de.renew.util.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;


public class SingleFANetCompiler extends SingleJavaNetCompiler {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SingleFANetCompiler.class);

    // TODO: remove
    private static final Value valueActive = new Value(new Integer(1));
    private ExpressionTokenSource tokenSource;
    private static final LocalVariable letter = new LocalVariable("a");
    private static final VariableExpression letterExpr = new VariableExpression(Types.UNTYPED,
                                                                                letter);

//    private static final VariableExpression letterExpr = new VariableExpression(Types.UNTYPED, letter);
    public static final String arcInscriptionPattern = "action System.out.println(%s);";
    private static int stateNum = 0;
    private static int transNum = 0;

    public SingleFANetCompiler() {
        this(false, false, false);
    }

    public SingleFANetCompiler(boolean allowDangerousArcs,
                               boolean allowTimeInscriptions,
                               boolean wantEarlyTokens) {
        super(allowDangerousArcs, allowTimeInscriptions, wantEarlyTokens);

        try {
            tokenSource = new ExpressionTokenSource(((TypedExpression) parseFAStateInscription("[]")
                                                                           .iterator()
                                                                           .next())
                                                    .getExpression());
        } catch (SyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void compile(ShadowNet shadowNet) throws SyntaxException {
        if (hasMultipleStartstates(shadowNet.elements().iterator())) {
            logger.debug("Found multiple startstate! ~~~~~~~~~~~~~«««");

//            GuiPlugin.getCurrent()
//                     .showStatus("Cannot simulate automaton with multiple startstates.");
            throw new SyntaxException("Cannot simulate automaton with multiple startstates.");
        }

        // Get the name of the net that is to be compiled.
        Net net = getLookup().getNet(shadowNet.getName());
        logger.debug("compile(ShadowNet) compiling " + net);

        parseDeclarations(shadowNet);


        // Compile each shadow element
        // Compilation has to be done in the following total order

        // 1. ShadowFAStates
        Iterator<ShadowNetElement> iterator = shadowNet.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowFAState) {
                compile((ShadowFAState) elem, net);
            }
        }
        logger.debug("»~ All shadow states compiled ~«");

        // 2. ShadowFAArcs
        iterator = shadowNet.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowFAArc) {
                compile((ShadowFAArc) elem, net);
            }
        }
        logger.debug("»~ All shadow states compiled ~«");
        logger.debug("»»~ Compilation of " + shadowNet.getName()
                     + " finished! ~««");
    }

    protected void compile(ShadowFAState shadowFAState, Net net)
            throws SyntaxException {
        logger.debug("compile(ShadowFAState, Net) compiling " + shadowFAState);

        // Determine the name.
        String sname = shadowFAState.getName();
        if (sname == null) {
            stateNum++;
            sname = "State" + stateNum;
        }

        // Create the new place.
        Place place = new Place(net, sname,
                                new NetElementID(shadowFAState.getID()));
        place.setTrace(shadowFAState.getTrace());

        // Mark startstates
        if (shadowFAState.stateType == ShadowFAState.START
                    || shadowFAState.stateType == ShadowFAState.STARTEND) {
            place.add(tokenSource);
        }

        // Add ShadowFAState and mapped Place to lookup
        FAShadowLookupExtension.lookup(lookup).set(shadowFAState, place);

        compileFAStateInscriptions(shadowFAState, place);
    }

    protected void compileFAStateInscriptions(ShadowFAState shadowFAState,
                                              Place place)
            throws SyntaxException {
        logger.debug("compileFAStateInscriptions(ShadowFAState, Place) called with "
                     + shadowFAState + " and " + place);


        // Insert the inscriptions.
        Iterator<ShadowNetElement> iterator = shadowFAState.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            logger.debug(this + " has " + elem + " as child");

            if (elem instanceof ShadowInscription) {
                String inscr = ((ShadowInscription) elem).inscr;

//                logger.debug("On " + shadowFAState + " a ShadowInscription "
//                             + elem + " with " + inscr + " of type "
//                             + ((FATextFigure) elem.context).getType()
//                             + " was found");
//
//                FATextFigure faText = (FATextFigure) elem.context;
//                if (!(faText.getType() == CPNTextFigure.NAME)) {
                try {
                    Iterator<Object> exprEnum = parseFAStateInscription(inscr)
                                                    .iterator();
                    while (exprEnum.hasNext()) {
                        Object expr = exprEnum.next();
                        if (expr instanceof TypedExpression) {
                            TypedExpression typedExpr = (TypedExpression) expr;

                            Expression castedExpression = null;
                            try {
                                castedExpression = JavaNetHelper
                                                       .makeCastedOutputExpression(Types.UNTYPED,
                                                                                   typedExpr);
                            } catch (SyntaxException e) {
                                throw e.addObject(elem);
                            }
                            place.add(new ExpressionTokenSource(castedExpression));
                            logger.debug("Added " + castedExpression + " to "
                                         + shadowFAState);
                        }
                    }
                } catch (SyntaxException e) {
                    throw e.addObject(elem);
//                    }

                    //Only Marking parsing:
//                        TypedExpression typedExpr;
//                        if ("[]".equals(inscr)) {
//                            typedExpr = (TypedExpression) parseFAStateInscription(inscr);
//                            // token = value1;
//                        } else {
//                            throw new SyntaxException("Expected '[]'").addObject(elem);
//                        }
//                        Expression expr = typedExpr.getExpression();
//                        place.add(new ExpressionTokenSource(expr));
                }
            } else if (elem instanceof ShadowFAArc) {
                // ignore it.
            } else {
                throw new SyntaxException("Unsupported place inscription: "
                                          + elem.getClass()).addObject(shadowFAState)
                                                                                              .addObject(elem);
            }
        }
    }

    private Collection<Object> parseFAStateInscription(String inscr)
            throws SyntaxException {
        logger.debug("parseFAStateInscription(String) called with " + inscr);
        if ((inscr != null) && !inscr.equals("")) {
            logger.debug("FAState has inscription » " + inscr + " «");
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

    /**
     * Compiles the given FAArc.
     * <p>
     * Creates the corresponding compiled transition and adds arcs
     * to connect to the adjacent states.
     * </p>
     * <p>
     * For inscriptions to the FAArc, the method {@link #compileFAArcInscriptions(ShadowFAArc)}
     * is called to compile those. Inscriptions are compiled, so that
     * they are printed when transitioning states.
     * </p>
     *
     * @param shadowFAArc
     *            ShadowFAArc to be compiled
     * @param net
     *            Net of that arc
     * @throws SyntaxException
     */
    protected void compile(ShadowFAArc shadowFAArc, Net net)
            throws SyntaxException {
        logger.debug("compile(ShadowFAArc, Net) compiling " + shadowFAArc);

        // inscribe arc so it will output its name 
        logger.debug("ShadowFAArcs name is " + shadowFAArc.getName());
//        String printInscr = arcInscriptionPattern.replace("ARCNAME", shadowFAArc.getName());
//        String printInscr = String.format(arcInscriptionPattern, shadowFAArc.getName());
//        new ShadowInscription(shadowFAArc, printInscr);

        // Determine the name.
        String tname = shadowFAArc.toString();
        if (tname == null) {
            transNum++;
            tname = "Arc" + transNum;
        }


        // Create a transition
        Transition transition = new Transition(net, tname,
                                               new NetElementID(shadowFAArc
                                    .getID()));
        // Map ShadowFAArc to Transition in lookup
        FAShadowLookupExtension.lookup(lookup).set(shadowFAArc, transition);

        // create and add arcs from/to connected Place
        Place src = FAShadowLookupExtension.lookup(lookup).get(shadowFAArc.src);
        Arc arc = new Arc(src, transition, Arc.in, letterExpr,
                          ConstantExpression.doubleZeroExpression);
        arc.setTrace(shadowFAArc.getTrace());
        transition.add(arc);

        Place dest = FAShadowLookupExtension.lookup(lookup).get(shadowFAArc.dest);
        arc = new Arc(dest, transition, Arc.out, letterExpr,
                      ConstantExpression.doubleZeroExpression);
        arc.setTrace(shadowFAArc.getTrace());
        transition.add(arc);

        // Compile inscriptions of FAArc
        compileFAArcInscriptions(shadowFAArc);
    }

    /**
     * All inscriptions of the given ShadowFAArc are parsed by a call to
     * {@link #parseArcInscription(String)} and afterwards added to the
     * compiled Transition, that was earlier compiled from the given
     * ShadowFAArc.
     *
     * @param shadowFAArc                Its inscriptions are to be compiled
     * @throws SyntaxException
     */
    protected void compileFAArcInscriptions(ShadowFAArc shadowFAArc)
            throws SyntaxException {
        logger.debug("compileFAArcInscriptions(ShadowFAArc) called with "
                     + shadowFAArc);


        // Prepare a list to hold the inscriptions.
        Vector<TransitionInscription> parsedInscriptions = new Vector<TransitionInscription>();
        Iterator<ShadowNetElement> inscriptions = shadowFAArc.elements()
                                                             .iterator();
        Transition faArcTrans = FAShadowLookupExtension.lookup(lookup)
                                                       .get(shadowFAArc);

        // parse every FAArc inscription
        while (inscriptions.hasNext()) {
            Object elem = inscriptions.next();
            if (elem instanceof ShadowInscription) {
                ShadowInscription inscription = (ShadowInscription) elem;

                parsedInscriptions.addAll(parseFAArcInscription(inscription,
                                                                faArcTrans));
            }
        }

        for (TransitionInscription transInscr : parsedInscriptions) {
            faArcTrans.add(transInscr);
        }
    }

    /**
     * Compiles the inscription for the given transition of the FAArc. This is
     * done by adding an expression for printing the inscription string to the
     * transition.
     *
     * @param inscription
     *            The FAArc inscription to be printed
     * @param transition
     *            The transition of the FAArc
     * @throws SyntaxException
     */
    private Collection<TransitionInscription> parseFAArcInscription(ShadowInscription inscription,
                                                                    Transition transition)
            throws SyntaxException {
        logger.debug("parseFAArcInscription(ShadowInscription, Transition) called with "
                     + inscription + " and " + transition);
        String inscrType = checkTransitionInscription(inscription.inscr, false,
                                                      inscription.getNet());
        logger.debug("Parsed inscription was » " + inscrType + " «");

        return makeInscriptions(inscription.inscr, transition, true);
//        return makeInscriptions(String.format(arcInscriptionPattern,
//        									  inscription.inscr.replace('"', ' ')),
//        						transition,
//        						true);
    }

    private boolean hasMultipleStartstates(Iterator<ShadowNetElement> shadowNetElements) {
        int startstateCount = 0;

        while (shadowNetElements.hasNext()) {
            ShadowNetElement elem = shadowNetElements.next();
            if (elem instanceof ShadowFAState) {
                ShadowFAState shFAState = (ShadowFAState) elem;
                if (shFAState.stateType == ShadowFAState.START
                            || shFAState.stateType == ShadowFAState.STARTEND) {
                    startstateCount++;
                    if (startstateCount > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}