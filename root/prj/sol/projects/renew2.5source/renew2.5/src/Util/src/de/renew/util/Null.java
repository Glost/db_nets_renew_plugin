package de.renew.util;

public class Null {
    public static boolean nullAwareEquals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static int nullAwareHashCode(Object object) {
        return object == null ? 0 : object.hashCode();
    }
}