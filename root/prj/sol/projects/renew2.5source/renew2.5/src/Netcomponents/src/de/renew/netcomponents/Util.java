package de.renew.netcomponents;

import CH.ifa.draw.framework.FigureEnumeration;

import java.util.Iterator;
import java.util.Vector;


/**
 * tools.Util.java
 *
 *
 * @author Lawrence Cabac
 * @version 0.1,  June 2002
 *
 */
public class Util {
    public static Object[] it2a(Iterator<Object> it) {
        return iterator2array(it);
    }

    public static Object[] iterator2array(Iterator<Object> it) {
        Vector<Object> v = new Vector<Object>();

        while (it.hasNext()) {
            v.add(it.next());
        }

        return v.toArray();
    }

    public static Object[] f2a(FigureEnumeration en) {
        return figureEnumeration2array(en);
    }

    public static Object[] figureEnumeration2array(FigureEnumeration en) {
        Vector<Object> v = new Vector<Object>();

        while (en.hasMoreElements()) {
            v.add(en.nextElement());
        }

        return v.toArray();
    }
} //class
