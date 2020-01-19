package de.renew.formalism.pt;

import de.renew.expression.ConstantExpression;

import de.renew.formalism.base.AbstractSingleNetCompiler;

import de.renew.net.ConstantTokenSource;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;
import de.renew.net.arc.Arc;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowDeclarationNode;
import de.renew.shadow.ShadowInscribable;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.SyntaxException;

import de.renew.unify.Tuple;

import java.util.Collection;
import java.util.Iterator;


public class SinglePTNetCompiler extends AbstractSingleNetCompiler {
    // Check the validity of certain inscriptions.
    public String checkDeclarationNode(String inscr, boolean special,
                                       ShadowNet shadowNet)
            throws SyntaxException {
        throw new SyntaxException("Declaration node is not allowed.");
    }

    public String checkArcInscription(String inscr, boolean special,
                                      ShadowNet shadowNet)
            throws SyntaxException {
        parseNumber(inscr);
        return "inscription";
    }

    public String checkTransitionInscription(String inscription,
                                             boolean special,
                                             ShadowNet shadowNet)
            throws SyntaxException {
        throw new SyntaxException("Transition inscription is not allowed.");
    }

    public String checkPlaceInscription(String inscr, boolean special,
                                        ShadowNet shadowNet)
            throws SyntaxException {
        parseNumber(inscr);
        return "initialMarking";
    }

    private static int parseNumber(String inscr) throws SyntaxException {
        try {
            return Integer.parseInt(inscr.trim());
        } catch (NumberFormatException nfe) {
            throw new SyntaxException("Number expected", nfe).addObject(inscr);
        }
    }

    protected static int parseNumericalInscription(ShadowInscribable inscribable,
                                                   int defaultValue,
                                                   int minValue)
            throws SyntaxException {
        int result = defaultValue;
        boolean inscriptionFound = false;


        // Insert the inscriptions.
        Iterator<ShadowNetElement> iterator = inscribable.elements().iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowInscription) {
                if (inscriptionFound) {
                    throw new SyntaxException("Multiple inscriptions not supported")
                          .addObject(inscribable);
                }
                inscriptionFound = true;
                result = parseNumber(((ShadowInscription) elem).inscr);
            } else if (elem instanceof ShadowArc) {
            } else {
                throw new SyntaxException("Unsupported inscription").addObject(inscribable)
                                                                    .addObject(elem);
            }
        }
        if (result < minValue) {
            throw new SyntaxException("Inscribed value must not be lower than "
                                      + minValue).addObject(inscribable);
        }
        return result;
    }

    protected void compilePlaceInscriptions(ShadowPlace shadowPlace, Place place)
            throws SyntaxException {
        int marking = parseNumericalInscription(shadowPlace, 0, 0);
        for (int i = 0; i < marking; i++) {
            place.add(new ConstantTokenSource(Tuple.NULL));
        }
    }

    protected void compileArc(ShadowArc shadowArc) throws SyntaxException {
        int shadowArcType = shadowArc.shadowArcType;
        if (shadowArcType != ShadowArc.ordinary) {
            throw new SyntaxException("Unsupported arc type").addObject(shadowArc);
        }
        int arcType = shadowArc.placeToTransition ? Arc.in : Arc.out;
        int multiplicity = parseNumericalInscription(shadowArc, 1, 1);
        Transition transition = lookup.get(shadowArc.transition);
        Place place = lookup.get(shadowArc.place);
        for (int i = 0; i < multiplicity; i++) {
            Arc arc = new Arc(place, transition, arcType,
                              new ConstantExpression(null, Tuple.NULL),
                              ConstantExpression.doubleZeroExpression);
            transition.add(arc);
        }
    }

    protected Collection<TransitionInscription> compileTransitionInscription(ShadowInscription inscription)
            throws SyntaxException {
        throw new SyntaxException("Transitions may not carry inscriptions")
                  .addObject(inscription);
    }

    public void parseDeclarations(ShadowNet shadowNet)
            throws SyntaxException {
        Iterator<ShadowNetElement> iterator = shadowNet.elements().iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowDeclarationNode) {
                SyntaxException e = new SyntaxException("Declaration node is not allowed.");
                e.addObject(elem);
                while (iterator.hasNext()) {
                    elem = iterator.next();
                    if (elem instanceof ShadowDeclarationNode) {
                        e.addObject(elem);
                    }
                }
                throw e;
            }
        }
    }
}