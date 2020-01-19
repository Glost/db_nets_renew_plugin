package de.renew.util;

public class Dist {
    public static double negexp(double avg) {
        return -avg * Math.log(1 - Math.random());
    }
}