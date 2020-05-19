package de.renew.unify;

public class UnifyUtils {

    private UnifyUtils() {
        throw new AssertionError();
    }

    public static boolean isInstanceOfUnknown(Object object) {
        return object instanceof Unknown;
    }
}
