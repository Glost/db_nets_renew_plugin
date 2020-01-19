package de.renew.util;

import java.util.HashMap;


/**
 * This is a Map where the mapping is bijective. <br>
 *
 * You could use both values as keys and/or as values. <br>
 *
 *
 *
 * @author Benjamin Schleinzer <mailto: 0schlein@informatik.uni-hamburg.de>
 *
 * @version 1.0
 */
public class BijectiveMap<K, V> extends HashMap<K, V> {

    /**
     *
     */
    private static final long serialVersionUID = 1173959856549564757L;

    /**
     * Logger for this class
     */
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(BijectiveMap.class);

    /**
     * this set will looked into of the key is searched for a value
     */
    private HashMap<V, K> valueKeyMap;

    /**
     * generate a new mapping
     *
     */
    public BijectiveMap() {
        valueKeyMap = new HashMap<V, K>();
    }

    /**
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     * @param o1
     *            which referes to o2
     * @param o2
     *            which referes to o1
     * @return <code>null</code>
     */
    public V put(K key, V value) {
        V returnValue = super.put(key, value);
        if (returnValue != null) {
            valueKeyMap.remove(returnValue);
        }
        valueKeyMap.put(value, key);
        return returnValue;
    }

    /**
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     * @param o1
     *            which referes to o2
     * @param o2
     *            which referes to o1
     * @return <code>null</code>
     */
    public K getKey(V value) {
        return valueKeyMap.get(value);
    }

    /**
     * remove the object and the refered object from the map. <br>
     *
     * if a map set is set, this operation fails if the set operation fails
     *
     * @see java.util.Map#remove(java.lang.Object)
     * @param o
     *            the object to remove
     * @return the refered deleted object
     */
    public V remove(Object key) {
        V returnValue = super.remove(key);
        if (returnValue != null) {
            valueKeyMap.remove(returnValue);
        }
        return returnValue;
    }

    public K removeKey(Object value) {
        K returnValue = valueKeyMap.remove(value);
        if (returnValue != null) {
            super.remove(returnValue);
        }
        return returnValue;
    }

    /**
     * removes all elements from this map <br>
     *
     * removes the elements from both list <br>
     *
     * @see java.util.Map#clear()
     */
    public void clear() {
        super.clear();
        valueKeyMap.clear();
    }


    /**
     * returns true if there is the specified value in this map <br>
     *
     * Same as containsKey(...)
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     * @param arg0
     *            the value to be tested
     * @return true if this map contains arg0
     */
    public boolean containsValue(Object arg0) {
        return valueKeyMap.containsKey(arg0);
    }
}