package de.renew.engine.searcher;

public class BindingBadness {
    /*
     * Binding badness:
     *  10000  I cannot contribute any information.
     *   9999  I have lots of possibilities to check.
     * 2-9998  I know there are at most so many possibilities
     *      1  I can contribute information without the need
     *         to branch.
     *      0  Sorry, but I know we'll never succeed.
     */
    public static final int max = 10000;

    public static int clip(int bad) {
        return (bad < max) ? bad : (max - 1);
    }
}