package de.renew.shadow;

import de.renew.net.Net;
import de.renew.net.Place;
import de.renew.net.Transition;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;


/**
 * This is simply a typesafe hash table to identify the
 * compiled objects that arise from shadow objects.
 **/
public class ShadowLookup {
    private Hashtable<String, Net> netMap;
    private HashSet<String> namesOfNewlyCompiledNets;
    private Hashtable<ShadowPlace, Place> placeMap;
    private Hashtable<ShadowTransition, Transition> transitionMap;
    private Hashtable<String, ShadowLookupExtension> extensions;

    public ShadowLookup() {
        netMap = new Hashtable<String, Net>();
        namesOfNewlyCompiledNets = new HashSet<String>();
        placeMap = new Hashtable<ShadowPlace, Place>();
        transitionMap = new Hashtable<ShadowTransition, Transition>();
        extensions = new Hashtable<String, ShadowLookupExtension>();
    }

    public void setNet(String name, Net net) {
        netMap.put(name, net);
    }

    public Net getNet(String name) {
        return netMap.get(name);
    }

    public Enumeration<String> allNetNames() {
        return netMap.keys();
    }

    public Iterator<String> allNewlyCompiledNetNames() {
        return namesOfNewlyCompiledNets.iterator();
    }

    public void set(ShadowPlace element, Place obj) {
        placeMap.put(element, obj);
        namesOfNewlyCompiledNets.add(element.getNet().getName());
    }

    public Place get(ShadowPlace element) {
        return placeMap.get(element);
    }

    public Enumeration<ShadowPlace> allPlaces() {
        return placeMap.keys();
    }

    public void set(ShadowTransition element, Transition obj) {
        transitionMap.put(element, obj);
        namesOfNewlyCompiledNets.add(element.getNet().getName());
    }

    public Transition get(ShadowTransition element) {
        return transitionMap.get(element);
    }

    public Enumeration<ShadowTransition> allTransitions() {
        return transitionMap.keys();
    }

    /**
     * Return an extension to this shadow lookup
     * for the extension category specified by
     * the given factory. If no extension of said
     * category is currently available, use the
     * factory to create an instance.
     * On multiple calls with factories for the same
     * category, this method will consistently
     * return the same object.
     **/
    public synchronized ShadowLookupExtension getShadowLookupExtension(ShadowLookupExtensionFactory factory) {
        ShadowLookupExtension extension = extensions.get(factory.getCategory());
        if (extension == null) {
            extension = factory.createExtension();
            extensions.put(factory.getCategory(), extension);
        }
        return extension;
    }


    /**
     * Publicly announces all compiled nets. This has to be done
     * at the end of the compilation process.
     **/
    public void makeNetsKnown() {
        for (Net net : netMap.values()) {
            net.makeKnown();
        }
    }

    public boolean containsNewlyCompiledNets() {
        return !namesOfNewlyCompiledNets.isEmpty();
    }
}