package de.renew.dbnets.shadow;

import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.dbnets.shadow.node.ShadowReadArc;
import de.renew.dbnets.shadow.node.ShadowRollbackArc;
import de.renew.dbnets.shadow.node.ShadowViewPlace;
import de.renew.formalism.java.SingleJavaNetCompiler;
import de.renew.net.DBNetControlLayer;
import de.renew.net.DBNetTransition;
import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.TransitionInscription;
import de.renew.net.UplinkInscription;
import de.renew.net.ViewPlace;
import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;

import java.util.Optional;
import java.util.Vector;

public class SingleJavaDBNetCompiler extends SingleJavaNetCompiler {

    private static final ReadArcFactory READ_ARC_FACTORY = new ReadArcFactory();

    private static final RollbackArcFactory ROLLBACK_ARC_FACTORY = new RollbackArcFactory();

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

    private void compile(ShadowViewPlace shadowPlace, DBNetControlLayer net) throws SyntaxException {
        String placeName = Optional.ofNullable(shadowPlace.getName()).orElseGet(() -> "P" + (++placeNum) + "VPDBN");

        // TODO: query.
        ViewPlace place = new ViewPlace(net, placeName, new NetElementID(shadowPlace.getID()), null);

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
                new NetElementID(shadowTransition.getID()),
                null // TODO: action.
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

//    @Override
//    protected void compileTransitionInscriptions(
//            ShadowTransition shadowTransition,
//            Vector<TransitionInscription> parsedInscriptions,
//            Vector<ShadowInscription> errorShadows) throws SyntaxException {
//        if (shadowTransition instanceof ShadowDBNetTransition) {
//            compileTransitionInscriptions((ShadowDBNetTransition) shadowTransition, parsedInscriptions, errorShadows);
//        } else {
//            super.compileTransitionInscriptions(shadowTransition, parsedInscriptions, errorShadows);
//        }
//    }
//
//    private void compileTransitionInscriptions(
//            ShadowDBNetTransition shadowTransition,
//            Vector<TransitionInscription> parsedInscriptions,
//            Vector<ShadowInscription> errorShadows) throws SyntaxException {
//        for (ShadowNetElement element : shadowTransition.elements()) {
//            compileTransitionElement(element, parsedInscriptions, errorShadows);
//        }
//    }
//
//    private void compileTransitionElement(
//            ShadowNetElement element,
//            Vector<TransitionInscription> parsedInscriptions,
//            Vector<ShadowInscription> errorShadows) throws SyntaxException {
//        if (element instanceof ShadowInscription) {
//            compileTransitionInscription(element, parsedInscriptions, errorShadows);
//        } else if (element instanceof ShadowArc) {
//            compileArc((ShadowArc) element);
//        } else if (Objects.nonNull(element)) {
//            compileNonStandardTransitionInscription(element);
//        } else {
//            throw new SyntaxException("The transition inscription was null").addObject(element);
//        }
//    }
//
//    private void compileTransitionInscription(
//            ShadowNetElement element,
//            Vector<TransitionInscription> parsedInscriptions,
//            Vector<ShadowInscription> errorShadows) throws SyntaxException {
//        ShadowInscription inscription = (ShadowInscription) element;
//
//        Collection<TransitionInscription> subinscriptions;
//
//        try {
//            subinscriptions = compileTransitionInscription(inscription);
//        } catch (SyntaxException e) {
//            throw e.addObject(inscription);
//        }
//
//        subinscriptions.forEach(subinscription -> {
//            parsedInscriptions.addElement(subinscription);
//            errorShadows.addElement(inscription);
//        });
//    }

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
