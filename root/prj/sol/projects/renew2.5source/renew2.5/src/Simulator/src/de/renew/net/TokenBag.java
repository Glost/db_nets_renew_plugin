package de.renew.net;

import de.renew.engine.searchqueue.SearchQueue;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


// I allow nulls to be stored in a place. I do this by
// converting nulls to and from an internal representation using
// the class Null.
public class TokenBag implements Serializable {
    private int size;
    private Map<Object, TimeSet> map;

    TokenBag() {
        map = new HashMap<Object, TimeSet>();
        size = 0;
    }

    // Returns the delay until the availability of the tokens.
    // Positive infinity signals that the tokens will never
    // become available by simply waiting.
    public double computeEarliestTime(Object elem, TimeSet delays) {
        if (map.containsKey(elem)) {
            double earliest = (map.get(elem)).computeEarliestTime(delays);
            return earliest;
        } else if (delays.getSize() == 0) {
            // This should not happen, but we are always enabled.
            return 0;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    public synchronized TimeSet getTimeSet(Object elem) {
        if (map.containsKey(elem)) {
            return map.get(elem);
        } else {
            return TimeSet.EMPTY;
        }
    }

    /**
     *  <code>getMultiplicity</code> returns the multiplicity of the
     * elem in the tokenBag.
     *
     * @param elem an <code>Object</code> value
     * @return an <code>int</code> value
     */
    public synchronized int getMultiplicity(Object elem) {
        if (map.containsKey(elem)) {
            return (map.get(elem)).getSize();
        } else {
            return 0;
        }
    }

    public synchronized Collection<Object> uniqueElements() {
        // Unfortunately, we have to make a copy of the
        // elements, because the hashtable might be updated
        // asynchronously.
        return new ArrayList<Object>(map.keySet());
    }

    /**
     *  <code>includesAnytime</code> ignores timestamps on tokens.
     *
     * @param elem an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public synchronized boolean includesAnytime(Object elem) {
        return map.containsKey(elem);
    }

    public synchronized boolean includesBefore(Object elem, double time) {
        if (!map.containsKey(elem)) {
            return false;
        }
        TimeSet times = map.get(elem);
        return time >= times.earliestTime();
    }

    // A token bag should only be updated under control of its place.
    // Therefore this method is not made public.
    //
    // If this is supposed to change, a place instance may not
    // expose its token bag any longer.
    synchronized void add(Object elem, double time) {
        if (map.containsKey(elem)) {
            TimeSet times = map.get(elem);
            map.put(elem, times.including(time));
        } else {
            map.put(elem, TimeSet.make(time));
        }
        ++size;
    }

    // A token bag should only be updated under control of its place.
    // Therefore this method is not made public.
    //
    // If this is supposed to change, a place instance may not
    // expose its token bag any longer.
    synchronized void removeOneOf(Object elem, double time) {
        if (map.containsKey(elem)) {
            TimeSet times = (map.get(elem)).excluding(time);
            if (times.isEmpty()) {
                map.remove(elem);
            } else {
                map.put(elem, times);
            }
            --size;
        } else {
            throw new RuntimeException("Negative number of tokens detected.");
        }
    }

    // A token bag should only be updated under control of its place.
    // Therefore this method is not made public.
    // 
    // This method returns the times stamp of the token that is actually
    // removed so that the token can be put back easily.
    synchronized double removeWithDelay(Object elem, double delay) {
        if (map.containsKey(elem)) {
            TimeSet times = map.get(elem);
            double time = times.latestWithDelay(delay, SearchQueue.getTime());
            times = times.excluding(time);
            if (times.isEmpty()) {
                map.remove(elem);
            } else {
                map.put(elem, times);
            }
            --size;
            return time;
        } else {
            throw new NoSuchElementException();
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TokenBag(size: ");
        buffer.append(size);
        buffer.append("; token@timeset:");
        Iterator<Object> enumeration = map.keySet().iterator();
        while (enumeration.hasNext()) {
            Object token = enumeration.next();
            buffer.append(' ');
            buffer.append(token);
            buffer.append('@');
            buffer.append(map.get(token));
        }
        buffer.append(')');
        return buffer.toString();
    }
}