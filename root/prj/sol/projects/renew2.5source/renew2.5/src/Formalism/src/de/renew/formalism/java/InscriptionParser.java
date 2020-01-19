package de.renew.formalism.java;

import de.renew.net.Transition;

import de.renew.shadow.ShadowLookup;

import java.util.Collection;


public interface InscriptionParser {
    void setDeclarationNode(ParsedDeclarationNode decl);

    void setLookup(ShadowLookup lookup);

    ParsedDeclarationNode DeclarationNode() throws ParseException;

    Collection<Object> PlaceInscription() throws ParseException;

    Collection<TimedExpression> ArcInscription() throws ParseException;

    Collection<de.renew.net.TransitionInscription> TransitionInscription(boolean create,
                                                                         Transition transition)
            throws ParseException;

    TypedExpression VariableInscription() throws ParseException;

    ChannelInscription tryParseChannelInscription() throws ParseException;
}