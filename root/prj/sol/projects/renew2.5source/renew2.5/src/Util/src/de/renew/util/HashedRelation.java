package de.renew.util;

import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class HashedRelation<K, V> implements Serializable {
    private Map<K, Set<V>> map;

    public HashedRelation() {
        map = new HashMap<K, Set<V>>();
    }

    public synchronized void put(K key, V elem) {
        Set<V> set;
        if (map.containsKey(key)) {
            set = map.get(key);
        } else {
            set = new HashSet<V>();
            map.put(key, set);
        }
        set.add(elem);
    }

    public synchronized void remove(K key, V elem) {
        Set<V> set = map.get(key);
        set.remove(elem);
        if (set.isEmpty()) {
            map.remove(key);
        }
    }

    public synchronized Set<K> keys() {
        return map.keySet();
    }

    public synchronized Set<V> elementsAt(K key) {
        if (map.containsKey(key)) {
            Set<V> set = map.get(key);
            return set;
        } else {
            return Collections.emptySet();
        }
    }

    public synchronized int sizeAt(Object key) {
        if (map.containsKey(key)) {
            Set<V> set = map.get(key);
            return set.size();
        } else {
            return 0;
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("de.renew.util.HashedRelation( ");
        for (K key : map.keySet()) {
            buffer.append(key);
            buffer.append(" -> (");
            boolean first = true;
            for (V value : map.get(key)) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(", ");
                }
                buffer.append(value);
            }
            buffer.append(") ");
        }
        buffer.append(")");
        return buffer.toString();
    }
}