package de.renew.shadow;

import de.renew.net.Place;
import de.renew.net.Transition;

import java.io.Serializable;

import java.util.Enumeration;
import java.util.Hashtable;


public class ContextLookup {
    private Hashtable<Object, Serializable> map;

    public ContextLookup(ShadowLookup shadowLookup) {
        map = new Hashtable<Object, Serializable>();

        Enumeration<ShadowPlace> places = shadowLookup.allPlaces();
        while (places.hasMoreElements()) {
            ShadowPlace shadowPlace = places.nextElement();
            Place place = shadowLookup.get(shadowPlace);
            map.put(shadowPlace.context, place);
        }

        Enumeration<ShadowTransition> transitions = shadowLookup.allTransitions();
        while (transitions.hasMoreElements()) {
            ShadowTransition shadowTransition = transitions.nextElement();
            Transition transition = shadowLookup.get(shadowTransition);
            map.put(shadowTransition.context, transition);
        }
    }

    public Place getPlace(Object context) {
        return (Place) map.get(context);
    }

    public Transition getTransition(Object context) {
        return (Transition) map.get(context);
    }

    public Hashtable<Object, Serializable> getMap() {
        return map;
    }
}