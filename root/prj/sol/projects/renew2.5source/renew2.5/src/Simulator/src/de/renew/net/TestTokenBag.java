package de.renew.net;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


// I allow nulls to be stored in a place. I do this by
// converting nulls to and from an internal representation using
// the class Null.
public class TestTokenBag implements Serializable {
    // I prepare an array with small integers to avoid
    // creating excessive amounts of temporary integer objects.
    static final int maxStaticMult = 15;
    static final Integer[] multiplicities;

    static {
        multiplicities = new Integer[maxStaticMult + 1];
        for (int i = 0; i <= maxStaticMult; i++) {
            multiplicities[i] = new Integer(i);
        }
    }

    private int size;
    private Map<Object, Integer> testCount;
    private Map<Object, Double> orgTime;

    TestTokenBag() {
        testCount = new HashMap<Object, Integer>();
        orgTime = new HashMap<Object, Double>();
        size = 0;
    }

    // Synchronize just to be safe. Maybe this is overly cautious.
    public synchronized int getUniqueSize() {
        return testCount.size();
    }

    public synchronized int getTestMultiplicity(Object elem) {
        if (testCount.containsKey(elem)) {
            return (testCount.get(elem)).intValue();
        } else {
            return 0;
        }
    }

    private final static Integer toInteger(int result) {
        if (result <= maxStaticMult) {
            return multiplicities[result];
        } else {
            return new Integer(result);
        }
    }

    // Unfortunately, we have to make a copy of the
    // elements, because the hashtable might be updated
    // asynchronously.
    public synchronized Collection<Object> uniqueElements() {
        return new ArrayList<Object>(testCount.keySet());
    }

    public synchronized boolean includesTested(Object elem) {
        return testCount.containsKey(elem);
    }

    // A token bag should only be updated under control of its place.
    // Therefore this method is not made public.
    //
    // If this is supposed to change, a place instance may not
    // expose its token bag any longer.
    synchronized void addTested(Object elem, double time) {
        if (testCount.containsKey(elem)) {
            Integer num = testCount.get(elem);
            testCount.put(elem, toInteger(num.intValue() + 1));
        } else {
            // We are testing the first token.
            // For this token, we record its time.
            testCount.put(elem, toInteger(1));
            orgTime.put(elem, new Double(time));
        }
        ++size;
    }

    // A token bag should only be updated under control of its place.
    // Therefore this method is not made public.
    //
    // If this is supposed to change, a place instance may not
    // expose its token bag any longer.
    synchronized double removeTested(Object elem) {
        if (testCount.containsKey(elem)) {
            int newMult = (testCount.get(elem)).intValue() - 1;
            double time = (orgTime.get(elem)).doubleValue();
            if (newMult == 0) {
                testCount.remove(elem);
                orgTime.remove(elem);
            } else {
                testCount.put(elem, toInteger(newMult));
            }
            --size;
            return time;
        } else {
            throw new RuntimeException("Negative number of tokens detected.");
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TestTokenBag(size: ");
        buffer.append(size);
        buffer.append("; count'token@time:");
        Iterator<Object> enumeration = testCount.keySet().iterator();
        while (enumeration.hasNext()) {
            Object token = enumeration.next();
            buffer.append(' ');
            buffer.append(testCount.get(token));
            buffer.append('\'');
            buffer.append(token);
            buffer.append('@');
            buffer.append(orgTime.get(token));
        }
        buffer.append(')');
        return buffer.toString();
    }
}