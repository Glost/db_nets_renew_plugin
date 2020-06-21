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
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;
import de.renew.util.Types;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Vector;

/**
 * The db-net's compiler.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class SingleJavaDBNetCompiler extends SingleJavaNetCompiler {

    /**
     * The read arc factory instance constant.
     */
    private static final ReadArcFactory READ_ARC_FACTORY = new ReadArcFactory();

    /**
     * The rollback arc factory instance constant.
     */
    private static final RollbackArcFactory ROLLBACK_ARC_FACTORY = new RollbackArcFactory();

    /**
     * Maps shadow places to types.
     */
    private final transient Hashtable<ShadowPlace, Class<?>> placeTypes = new Hashtable<>();

    /**
     * The db-net's compiler's constructor.
     */
    public SingleJavaDBNetCompiler() {
        super(false, false, false);
    }

    /**
     * Creates the db-net's control layer.
     *
     * @param name The db-net's name.
     * @return The created db-net's control layer.
     */
    @Override
    public DBNetControlLayer createNet(String name) {
        return new DBNetControlLayer(name);
    }

    /**
     * Compiles the db-net's declaration node.
     *
     * @param shadowNet The db-net whose declaration node should be compiled.
     * @return The db-net's declaration node.
     * @throws SyntaxException If the error occurred during the declaration node compiling.
     */
    @Override
    public ParsedDBNetDeclarationNode makeDeclarationNode(ShadowNet shadowNet) throws SyntaxException {
        ParsedDBNetDeclarationNode declarationNode = (ParsedDBNetDeclarationNode) super.makeDeclarationNode(shadowNet);
        DBNetControlLayer net = (DBNetControlLayer) lookup.getNet(shadowNet.getName());
        net.setJdbcConnection(declarationNode.getJdbcConnection());
        net.setDatabaseSchemaDeclaration(declarationNode.getDatabaseSchemaDeclaration());
        return declarationNode;
    }

    /**
     * Makes the empty declaration node.
     *
     * @param net The db-net that has no declaration node.
     * @return The empty declaration node.
     */
    @Override
    protected ParsedDBNetDeclarationNode makeEmptyDeclarationNode(ShadowNet net) {
        return new ParsedDBNetDeclarationNode();
    }

    /**
     * Compiles the place.
     *
     * @param shadowPlace The place which should be compiled.
     * @param net The db-net's control layer.
     * @throws SyntaxException If the error occurred during the place compiling.
     */
    @Override
    protected void compile(ShadowPlace shadowPlace, Net net) throws SyntaxException {
        if (shadowPlace instanceof ShadowViewPlace) {
            compile((ShadowViewPlace) shadowPlace, (DBNetControlLayer) net);
        } else {
            super.compile(shadowPlace, net);
        }
    }

    /**
     * Compiles the db-net's transition.
     *
     * @param shadowTransition The transition which should be compiled.
     * @param net The db-net's control layer.
     * @throws SyntaxException If the error occurred during the transition compiling or
     * the transition is not the db-net transition.
     */
    @Override
    protected void compile(ShadowTransition shadowTransition, Net net) throws SyntaxException {
        if (shadowTransition instanceof ShadowDBNetTransition) {
            compile((ShadowDBNetTransition) shadowTransition, (DBNetControlLayer) net);
        } else {
            throw new SyntaxException("Transition should be the db-net transition").addObject(shadowTransition);
        }
    }

    /**
     * Compiles the place's inscriptions.
     * Based on the {@link super#compilePlaceInscriptions(ShadowPlace, Place)} implementation.
     *
     * @param shadowPlace The shadow (non-compiled) level place whose inscriptions should be compiled.
     * @param place The place whose inscriptions should be compiled.
     * @throws SyntaxException If the error occurred during the place's inscriptions compiling.
     */
    @Override
    protected void compilePlaceInscriptions(ShadowPlace shadowPlace, Place place) throws SyntaxException {
        // Store the initialisation inscriptions for later use.
        Vector<Object> parsedInscriptions = new Vector<>();
        Vector<ShadowNetElement> errorShadows = new Vector<>();


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

                Expression castedExpression;
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
     * Returns the given place's type.
     * Based on the {@link super#getType(ShadowPlace)} implementation.
     *
     * @param place The place for returning its type.
     * @return The given place's type.
     */
    @Override
    Class<?> getType(ShadowPlace place) {
        return placeTypes.getOrDefault(place, Types.UNTYPED);
    }

    /**
     * Makes the db-net's inscriptions parser.
     *
     * @param inscr The inscription to parse.
     * @return The db-net's inscriptions parser.
     */
    @Override
    protected DBNetInscriptionParser makeParser(String inscr) {
        JavaDBNetParser parser = new JavaDBNetParser(new StringReader(inscr));
        parser.setNetLoader(loopbackNetLoader);
        return parser;
    }

    /**
     * Compiles the db-net's view place.
     *
     * @param shadowPlace The db-net's view place which should be compiled.
     * @param net The db-net's control layer.
     * @throws SyntaxException If the error occurred during the db-net's view place compiling.
     */
    private void compile(ShadowViewPlace shadowPlace, DBNetControlLayer net) throws SyntaxException {
        String placeName = Optional.ofNullable(shadowPlace.getName()).orElseGet(() -> "P" + (++placeNum) + "VPDBN");

        ViewPlace place = new ViewPlace(net, placeName, new NetElementID(shadowPlace.getID()));

        place.setTrace(shadowPlace.getTrace());

        Optional.ofNullable(shadowPlace.getComment()).ifPresent(place::setComment);

        lookup.set(shadowPlace, place);

        compilePlaceInscriptions(shadowPlace, place);
    }

    /**
     * Compiles the db-net's transition.
     *
     * @param shadowTransition The db-net's transition which should be compiled.
     * @param net The db-net's control layer.
     * @throws SyntaxException If the error occurred during the db-net's transition compiling.
     */
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

        Vector<TransitionInscription> parsedInscriptions = new Vector<>();
        Vector<ShadowInscription> errorShadows = new Vector<>();

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

    /**
     * Compiles the arc.
     *
     * @param shadowArc The arc which should be compiled.
     * @throws SyntaxException If the error occurred during the arc compiling.
     */
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

    /**
     * Compiles the db-net's read arc.
     *
     * @param shadowArc The db-net's read arc which should be compiled.
     * @throws SyntaxException If the error occurred during the db-net's read arc compiling.
     */
    private void compileReadArc(ShadowReadArc shadowArc) throws SyntaxException {
        compileArcInscriptions(shadowArc, READ_ARC_FACTORY);
    }

    /**
     * Compiles the db-net's rollback arc.
     *
     * @param shadowArc The db-net's rollback arc which should be compiled.
     * @throws SyntaxException If the error occurred during the db-net's rollback arc compiling.
     */
    private void compileRollbackArc(ShadowRollbackArc shadowArc) throws SyntaxException {
        compileArcInscriptions(shadowArc, ROLLBACK_ARC_FACTORY);
    }
}
