package de.renew.util;

import java.util.Enumeration;


public class ClassCheckEnumeration implements Enumeration<Object> {
    private Enumeration<?extends Object> source;
    private Class<?> clazz;
    private Object next = null;
    private boolean more = true;

    public ClassCheckEnumeration(Enumeration<?extends Object> source,
                                 Class<?> clazz) {
        this.source = source;
        this.clazz = clazz;
        nextElement();
    }

    public boolean hasMoreElements() {
        return more;
    }

    public synchronized Object nextElement() {
        Object result = next;
        more = false;
        while (source.hasMoreElements() && !more) {
            next = source.nextElement();
            more = clazz.isInstance(next);
        }
        return result;
    }
}