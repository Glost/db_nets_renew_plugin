package de.renew.net;

import de.renew.engine.searcher.DeltaSet;
import de.renew.engine.searcher.DeltaSetFactory;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.simulator.SimulationThreadPool;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * A token reserver is responsible for simulating
 * the state of several place instances that would occur if
 * a number of token removals and tests was done.
 */
public class TokenReserver implements DeltaSet {
    private final static Factory FACTORY = new Factory();

    private static class Factory implements DeltaSetFactory {
        public String getCategory() {
            return "de.renew.nets.TokenReserver";
        }

        public DeltaSet createDeltaSet() {
            return new TokenReserver();
        }
    }

    public static TokenReserver getInstance(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return (TokenReserver) searcher.getDeltaSet(FACTORY);
    }

    Hashtable<PlaceInstance, TokenReservation> reservations = new Hashtable<PlaceInstance, TokenReservation>();

    TokenReserver() {
    }

    public void reset() {
        // In theory, a reservations.clear() should suffice
        // at this point. For some entirely obscure point, however,
        // the Java garbage collector does not like it.
        // This is annoying.
        reservations = new Hashtable<PlaceInstance, TokenReservation>();
    }

    public double computeEarliestTime() {
        double result = 0;
        Enumeration<TokenReservation> enumeration = reservations
                                                        .elements();
        while (enumeration.hasMoreElements()) {
            TokenReservation reservation = enumeration.nextElement();
            double time = reservation.computeEarliestTime();
            if (time > result) {
                result = time;
            }
        }
        return result;
    }

    private TokenReservation getReservation(PlaceInstance place) {
        if (reservations.containsKey(place)) {
            return reservations.get(place);
        } else {
            TokenReservation reservation = new TokenReservation(place);
            reservations.put(place, reservation);
            return reservation;
        }
    }

    private void disposeReservation(PlaceInstance place) {
        TokenReservation reservation = reservations.get(place);
        if (reservation.isRemovable()) {
            reservations.remove(place);
        }
    }

    public synchronized boolean containsRemovableToken(PlaceInstance place,
                                                       Object token,
                                                       double delay) {
        TokenReservation reservation = getReservation(place);
        boolean result = reservation.containsRemovableToken(token, delay);
        disposeReservation(place);
        return result;
    }

    public synchronized boolean containsTestableToken(PlaceInstance place,
                                                      Object token) {
        TokenReservation reservation = getReservation(place);
        boolean result = reservation.containsTestableToken(token);
        disposeReservation(place);
        return result;
    }

    public synchronized boolean removeToken(PlaceInstance place, Object token,
                                            double time) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        TokenReservation reservation = getReservation(place);
        boolean result = reservation.removeToken(token, time);
        disposeReservation(place);
        return result;
    }

    public synchronized boolean testToken(PlaceInstance place, Object token) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        TokenReservation reservation = getReservation(place);
        boolean result = reservation.testToken(token);
        disposeReservation(place);
        return result;
    }

    public synchronized void unremoveToken(PlaceInstance place, Object token,
                                           double time) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        TokenReservation reservation = getReservation(place);
        reservation.unremoveToken(token, time);
        disposeReservation(place);
    }

    public synchronized void untestToken(PlaceInstance place, Object token) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        TokenReservation reservation = getReservation(place);
        reservation.untestToken(token);
        disposeReservation(place);
    }

    public synchronized Collection<Object> getRemovableTokens(PlaceInstance place,
                                                              Object pattern) {
        TokenReservation reservation = getReservation(place);
        Collection<Object> result = reservation.getRemovableTokens(pattern);
        disposeReservation(place);
        return result;
    }

    public synchronized Collection<Object> getTestableTokens(PlaceInstance place,
                                                             Object pattern) {
        TokenReservation reservation = getReservation(place);
        Collection<Object> result = reservation.getTestableTokens(pattern);
        disposeReservation(place);
        return result;
    }
}