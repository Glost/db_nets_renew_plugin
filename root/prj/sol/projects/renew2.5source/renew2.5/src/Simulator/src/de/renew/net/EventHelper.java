package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import java.util.Iterator;


public class EventHelper {
    public static Place getNamedPlace(Net net, String name) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        synchronized (net) {
            Iterator<Place> enumeration = net.places().iterator();
            while (enumeration.hasNext()) {
                Place place = enumeration.next();


                // Using the toString() method is ugly.
                // Maybe create a getName() method?
                if (name.equals(place.getName())) {
                    return place;
                }
            }
            return null;
        }
    }

    public static void deposit(NetInstance ni, String name, Object token) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Place place = getNamedPlace(ni.getNet(), name);
        if (place == null) {
            throw new RuntimeException("No such place.");
        }
        PlaceInstance pi = ni.getInstance(place);
        pi.lock.lock();
        try {
            pi.insertToken(token, 0);
        } finally {
            pi.lock.unlock();
        }
    }
}