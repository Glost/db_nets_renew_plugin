/*
 */
package de.renew.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * This is a simple utility class containing static method that convert Collections (as from java.util.Collection)
 * into a rendered string.
 * The rendering of this string can be configured by a delimiter that will be
 * inserted between every element of the collection.
 */
public class CollectionLister {

    /**
     * Creates a String from the given collection.
     * The elements of the collections will be separated by a comma.
     */
    public static String toString(Collection<?> c) {
        return toString(c, ", ");
    }

    /**
     * Creates a String from the given collection,
     * separating the elements of the collection with the given delimiter string.
     */
    public static String toString(Collection<?> c, String delim) {
        return toString(c.iterator(), delim);
    }

    /**
     * Creates a String from the given iterator.
     * The elements of the iterator will be separated by a comma.
     */
    public static String toString(Iterator<?> c) {
        return toString(c, ", ");
    }

    /**
     * Creates a String from the given iterator,
     * separating the elements of the iterator with the given delimiter string.
     */
    public static String toString(Iterator<?> c, String delimiter) {
        String result = "";
        while (c.hasNext()) {
            result += c.next();
            if (c.hasNext()) {
                result += delimiter;
            }
        }
        return result;
    }

    /**
     * Creates a String from the given Array,
     * separating elements by a comma.
     */
    public static String toString(Object[] array) {
        return toString(array, ",");
    }

    /**
     * Creates a String from the given Array,
     * with the elements separated by the given delimiter.
     */
    public static String toString(Object[] array, String delimiter) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                result.append(delimiter);
            }
            result.append(array[i]);
        }
        return result.toString();
    }

    public static ArrayList<String> toArrayList(Iterator<String> it) {
        ArrayList<String> result = new ArrayList<String>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        return result;
    }
}