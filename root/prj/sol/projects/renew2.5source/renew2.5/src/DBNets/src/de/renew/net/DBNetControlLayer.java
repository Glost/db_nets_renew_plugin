package de.renew.net;

import java.util.Collection;
import java.util.HashSet;

public class DBNetControlLayer extends Net {

    private Collection<ViewPlace> viewPlaces = new HashSet<>();

    public DBNetControlLayer() {
    }

    public DBNetControlLayer(String name) {
        super(name);
    }

    @Override
    void add(Place place) {
        super.add(place);
        if (place instanceof ViewPlace) {
            viewPlaces.add((ViewPlace) place);
        }
    }

    @Override
    void add(Transition transition) {
        checkTransitionType(transition);
        super.add(transition);
    }

    @Override
    void remove(Place place) {
        super.remove(place);
        if (place instanceof ViewPlace) {
            viewPlaces.remove(place);
        }
    }

    @Override
    void remove(Transition transition) {
        checkTransitionType(transition);
        super.remove(transition);
    }

    private void checkTransitionType(Transition transition) {
        if (!(transition instanceof DBNetTransition)) {
            throw new IllegalArgumentException("The transition should be instance of DBNetTransition.");
        }
    }
}
