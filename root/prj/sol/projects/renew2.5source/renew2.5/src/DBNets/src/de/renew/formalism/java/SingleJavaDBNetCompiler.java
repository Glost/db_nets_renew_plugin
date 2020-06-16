package de.renew.formalism.java;

import de.renew.dbnets.datalogic.QueryCall;
import de.renew.dbnets.shadow.ParsedDBNetDeclarationNode;
import de.renew.dbnets.shadow.ReadArcFactory;
import de.renew.dbnets.shadow.RollbackArcFactory;
import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.dbnets.shadow.node.ShadowReadArc;
import de.renew.dbnets.shadow.node.ShadowRollbackArc;
import de.renew.dbnets.shadow.node.ShadowViewPlace;
import de.renew.dbnets.shadow.parser.DBNetInscriptionParser;
import de.renew.dbnets.shadow.parser.JavaDBNetParser;
import de.renew.expression.Expression;
import de.renew.net.DBNetControlLayer;
import de.renew.net.DBNetTransition;
import de.renew.net.ExpressionTokenSource;
import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.Place;
import de.renew.net.TransitionInscription;
import de.renew.net.UplinkInscription;
import de.renew.net.ViewPlace;
import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowDeclarationNode;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;
import de.renew.util.Types;
import org.apache.log4j.Logger;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Vector;

public class SingleJavaDBNetCompiler extends SingleJavaNetCompiler {

    private static final Logger logger = Logger.getLogger(SingleJavaDBNetCompiler.class);

    private static final ReadArcFactory READ_ARC_FACTORY = new ReadArcFactory();

    private static final RollbackArcFactory ROLLBACK_ARC_FACTORY = new RollbackArcFactory();

    // Maps shadow places to types.
    private final transient Hashtable<ShadowPlace, Class<?>> placeTypes = new Hashtable<>();

    public SingleJavaDBNetCompiler() {
        super(false, false, false);
    }

    public SingleJavaDBNetCompiler(boolean allowDangerousArcs, boolean allowTimeInscriptions, boolean wantEarlyTokens) {
        super(allowDangerousArcs, allowTimeInscriptions, wantEarlyTokens);
    }

    @Override
    public Net createNet(String name) {
        Net net = new DBNetControlLayer(name);
//        net.setEarlyTokens(wantEarlyTokens); // TODO: ...
        return net;
    }

    @Override
    protected ParsedDBNetDeclarationNode makeEmptyDeclarationNode(ShadowNet net) {
        return new ParsedDBNetDeclarationNode();
    }

    @Override
    protected void compile(ShadowPlace shadowPlace, Net net) throws SyntaxException {
        if (shadowPlace instanceof ShadowViewPlace) {
            compile((ShadowViewPlace) shadowPlace, (DBNetControlLayer) net);
        } else {
            super.compile(shadowPlace, net);
        }
    }

    @Override
    protected void compile(ShadowTransition shadowTransition, Net net) throws SyntaxException {
        if (shadowTransition instanceof ShadowDBNetTransition) {
            compile((ShadowDBNetTransition) shadowTransition, (DBNetControlLayer) net);
        } else {
            super.compile(shadowTransition, net);
        }
    }

    @Override
    protected void compilePlaceInscriptions(ShadowPlace shadowPlace, Place place) throws SyntaxException {
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
            } else if (place instanceof ViewPlace && parsedInscriptions.elementAt(i) instanceof QueryCall) {
                ((ViewPlace) place).setQueryCall((QueryCall) parsedInscriptions.elementAt(i));
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

    @Override
    Class<?> getType(ShadowPlace place) {
        return placeTypes.getOrDefault(place, Types.UNTYPED);
    }

    @Override
    protected DBNetInscriptionParser makeParser(String inscr) {
        logger.info("Making JavaDBNetParser");
        JavaDBNetParser parser = new JavaDBNetParser(new StringReader(inscr));
        parser.setNetLoader(loopbackNetLoader);
        return parser;
    }

    private void compile(ShadowViewPlace shadowPlace, DBNetControlLayer net) throws SyntaxException {
        String placeName = Optional.ofNullable(shadowPlace.getName()).orElseGet(() -> "P" + (++placeNum) + "VPDBN");

        ViewPlace place = new ViewPlace(net, placeName, new NetElementID(shadowPlace.getID()));

        place.setTrace(shadowPlace.getTrace());

        Optional.ofNullable(shadowPlace.getComment()).ifPresent(place::setComment);

        lookup.set(shadowPlace, place);

        compilePlaceInscriptions(shadowPlace, place);
    }

    private void compile(ShadowDBNetTransition shadowTransition, DBNetControlLayer net) throws SyntaxException {
        String transitionName = Optional.ofNullable(shadowTransition.getName())
                .orElseGet(() -> "T" + (++transitionNum) + "DBN");

        DBNetTransition transition = new DBNetTransition(
                net,
                transitionName,
                new NetElementID(shadowTransition.getID())
        );

        transition.setTrace(shadowTransition.getTrace());

        Optional.ofNullable(shadowTransition.getComment()).ifPresent(transition::setComment);

        lookup.set(shadowTransition, transition);

        Vector<TransitionInscription> parsedInscriptions = new Vector<TransitionInscription>();
        Vector<ShadowInscription> errorShadows = new Vector<ShadowInscription>();

        compileTransitionInscriptions(shadowTransition, parsedInscriptions,
                errorShadows);

        long uplinkCount = parsedInscriptions.stream()
                .filter(transitionInscription -> transitionInscription instanceof UplinkInscription)
                .count();

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

    @Override
    protected void compileArc(ShadowArc shadowArc) throws SyntaxException {
        if (shadowArc instanceof ShadowReadArc) {
            compileReadArc((ShadowReadArc) shadowArc);
        } else if (shadowArc instanceof ShadowRollbackArc) {
            compileRollbackArc((ShadowRollbackArc) shadowArc);
        } else {
            super.compileArc(shadowArc);
        }
    }

    private void compileReadArc(ShadowReadArc shadowArc) throws SyntaxException {
        compileArcInscriptions(shadowArc, READ_ARC_FACTORY);
    }

    private void compileRollbackArc(ShadowRollbackArc shadowArc) throws SyntaxException {
        compileArcInscriptions(shadowArc, ROLLBACK_ARC_FACTORY);
    }
}
