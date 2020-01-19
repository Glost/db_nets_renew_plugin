package de.renew.faformalism.shadow;

import de.renew.net.Place;
import de.renew.net.Transition;

import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowLookupExtension;
import de.renew.shadow.ShadowLookupExtensionFactory;

import java.util.Enumeration;
import java.util.Hashtable;


public class FAShadowLookupExtension implements ShadowLookupExtension {
    private static final ShadowLookupExtensionFactory _factory = new Factory();
    private Hashtable<ShadowFAState, Place> stateMap;
    private Hashtable<ShadowFAArc, Transition> arcMap;

    public FAShadowLookupExtension() {
        stateMap = new Hashtable<ShadowFAState, Place>();
        arcMap = new Hashtable<ShadowFAArc, Transition>();
    }

    public static FAShadowLookupExtension lookup(ShadowLookup lookup) {
        return (FAShadowLookupExtension) lookup.getShadowLookupExtension(_factory);
    }

    //------------------------------ ShadowFAState management ----------   
    public void set(ShadowFAState shadowFAState, Place place) {
        stateMap.put(shadowFAState, place);
    }

    public Place get(ShadowFAState shadowFAState) {
        return stateMap.get(shadowFAState);
    }

    public Enumeration<ShadowFAState> allFAStates() {
        return stateMap.keys();
    }

    //------------------------------ ShadowFAArc management ----------   
    public void set(ShadowFAArc shadowFAArc, Transition transition) {
        arcMap.put(shadowFAArc, transition);
    }

    public Transition get(ShadowFAArc shadowFAArc) {
        return arcMap.get(shadowFAArc);
    }

    public Enumeration<ShadowFAArc> allFAArcs() {
        return arcMap.keys();
    }

    public static class Factory implements ShadowLookupExtensionFactory {
        @Override
        public String getCategory() {
            return "de.renew.fa";
        }

        @Override
        public ShadowLookupExtension createExtension() {
            return new FAShadowLookupExtension();
        }
    }
}