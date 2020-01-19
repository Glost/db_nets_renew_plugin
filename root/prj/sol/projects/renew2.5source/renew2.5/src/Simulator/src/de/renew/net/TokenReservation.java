package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A token reservation keeps track of the number of tokens
 * that would have to be tested and removed from a single
 * place instance.
 *
 * Accesses to objects of this class are unsynchronized.
 * It is assumed that any access is guarded by a lock on the
 * associated token reserver.
 */
class TokenReservation {
    private PlaceInstance instance;
    private int reservations;
    private TestTokenBag testedTokens;
    private TokenBag removedTokenDelays;

    TokenReservation(PlaceInstance instance) {
        this.instance = instance;
        reservations = 0;

        testedTokens = new TestTokenBag();
        removedTokenDelays = new TokenBag();
    }

    boolean isRemovable() {
        return reservations == 0;
    }

    // Return the set of registered delays for the given token.
    // If there are also tests, but the tests are not
    // targeted at an already tested token, an addional zero delay is
    // included.
    private TimeSet getRemovedDelaySet(Object token, boolean isTested) {
        TimeSet delays = removedTokenDelays.getTimeSet(token);
        if (isTested && !instance.containsTestedToken(token)) {
            delays = delays.including(0);
        }
        return delays;
    }

    public double computeEarliestTime() {
        double result;
        instance.lock.lock();
        try {
            result = 0;
            Set<Object> tokens = new HashSet<Object>();
            tokens.addAll(testedTokens.uniqueElements());
            tokens.addAll(removedTokenDelays.uniqueElements());
            Iterator<Object> enumeration = tokens.iterator();
            while (enumeration.hasNext()) {
                Object token = enumeration.next();
                TimeSet delays = getRemovedDelaySet(token,
                                                    testedTokens
                                     .getTestMultiplicity(token) > 0);
                double time = instance.computeEarliestTime(token, delays);
                if (time > result) {
                    result = time;
                }
            }
        } finally {
            instance.lock.unlock();
        }
        return result;
    }

    public boolean containsRemovableToken(Object token, double delay) {
        boolean result;
        instance.lock.lock();
        try {
            TimeSet delays = getRemovedDelaySet(token,
                                                testedTokens.getTestMultiplicity(token) > 0)
                                 .including(delay);
            double time = instance.computeEarliestTime(token, delays);
            result = (time < Double.POSITIVE_INFINITY);
        } finally {
            instance.lock.unlock();
        }
        return result;
    }

    public boolean containsTestableToken(Object token) {
        boolean result;
        instance.lock.lock();
        try {
            TimeSet delays = getRemovedDelaySet(token, true);
            double time = instance.computeEarliestTime(token, delays);
            result = (time < Double.POSITIVE_INFINITY);
        } finally {
            instance.lock.unlock();
        }
        return result;
    }

    public boolean removeToken(Object token, double delay) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        boolean result;
        instance.lock.lock();
        try {
            result = containsRemovableToken(token, delay);
            if (result) {
                removedTokenDelays.add(token, delay);
                reservations++;
            }
        } finally {
            instance.lock.unlock();
        }
        return result;
    }

    public boolean testToken(Object token) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        boolean result;
        instance.lock.lock();
        try {
            result = containsTestableToken(token);
            if (result) {
                // We can ignore the time. It is only used to
                // restore a previous state in the case of places.
                testedTokens.addTested(token, 0);
                reservations++;
            }
        } finally {
            instance.lock.unlock();
        }
        return result;
    }

    public void unremoveToken(Object token, double delay) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        removedTokenDelays.removeOneOf(token, delay);
        reservations--;
    }

    public void untestToken(Object token) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        testedTokens.removeTested(token);
        reservations--;
    }

    public Collection<Object> getRemovableTokens(Object pattern) {
        List<Object> result = new ArrayList<Object>();
        instance.lock.lock();
        try {
            for (Iterator<Object> tokens = instance.getDistinctTokens(pattern)
                                                   .iterator();
                         tokens.hasNext();) {
                Object token = tokens.next();


                // The token must be removable sometimes, not neccessarily
                // now, because the arc might specify a negative time delay.
                if (containsRemovableToken(token, Double.NEGATIVE_INFINITY)) {
                    result.add(token);
                }
            }
        } finally {
            instance.lock.unlock();
        }
        return result;
    }

    public Collection<Object> getTestableTokens(Object pattern) {
        List<Object> result = new ArrayList<Object>();
        instance.lock.lock();
        try {
            for (Iterator<Object> tokens = instance.getDistinctTestableTokens(pattern)
                                                   .iterator();
                         tokens.hasNext();) {
                Object token = tokens.next();
                if (containsTestableToken(token)) {
                    result.add(token);
                }
            }
        } finally {
            instance.lock.unlock();
        }
        return result;
    }
}