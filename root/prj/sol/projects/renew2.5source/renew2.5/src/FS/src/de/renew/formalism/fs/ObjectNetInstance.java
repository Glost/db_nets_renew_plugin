package de.renew.formalism.fs;

import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.Net;
import de.renew.net.NetInstanceImpl;
import de.renew.net.NetNotFoundException;
import de.renew.net.Place;
import de.renew.net.PlaceInstance;

import de.renew.unify.Impossible;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ObjectNetInstance extends NetInstanceImpl {
    protected ObjectNetInstance(String netName,
                                Hashtable<String, Vector<Object>> marking)
            throws Impossible, NetNotFoundException {
        this(Net.forName(netName), marking);
    }

    protected ObjectNetInstance(final Net net,
                                final Hashtable<String, Vector<Object>> marking)
            throws Impossible {
        super(net);
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    // Build hashtable of place names.
                    Hashtable<String, Place> nameToPlace = new Hashtable<String, Place>();
                    synchronized (net) {
                        Iterator<Place> iterator = net.places().iterator();
                        while (iterator.hasNext()) {
                            Place place = iterator.next();
                            nameToPlace.put(place.toString(), place);
                        }
                    }


                    // Transfer tokens.
                    Enumeration<String> enumeration = marking.keys();
                    while (enumeration.hasMoreElements()) {
                        String name = enumeration.nextElement();
                        PlaceInstance instance = getInstance(nameToPlace.get(name));

                        Enumeration<Object> tokens = (marking.get(name))
                                    .elements();
                        while (tokens.hasMoreElements()) {
                            // This used to be done silently. As there should be no 
                            // listeners yet, it can be done openly just as well.
                            instance.insertToken(tokens.nextElement(),
                                                 SearchQueue.getTime());
                        }
                    }
                    return null;
                }
            });
        try {
            future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }
    }
}