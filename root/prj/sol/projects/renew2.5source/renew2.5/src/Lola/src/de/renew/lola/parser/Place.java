package de.renew.lola.parser;

public class Place extends Node {
    private int initialMarking = 0;

    public Place(String name) {
        super(name);
    }

    public Place(String name, int x, int y) {
        super(name, x, y);
    }

    public boolean equals(Object o) {
        if (o instanceof Place) {
            return getName().equals(((Place) o).getName());
        } else {
            return false;
        }
    }

    public boolean initiallyMarked() {
        return initialMarking > 0;
    }

    /**
     * @param initialMarking the initialMarking to set
     */
    protected void setInitialMarking(int initialMarking) {
        this.initialMarking = initialMarking;
    }

    public int getInitialMarking() {
        return initialMarking;
    }
}