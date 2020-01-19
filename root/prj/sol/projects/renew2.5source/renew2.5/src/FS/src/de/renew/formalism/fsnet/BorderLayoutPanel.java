package de.renew.formalism.fsnet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;


public class BorderLayoutPanel extends Panel {
    private final static int CENTER = 0;
    private final static int NORTH = 1;
    private final static int SOUTH = 2;
    private final static int EAST = 3;
    private final static int WEST = 4;
    private final static String[] CARDINAL_NAME = new String[] { "Center", "North", "South", "East", "West" };
    private Component[] cardinal = new Component[CARDINAL_NAME.length];

    public BorderLayoutPanel() {
        setLayout(new BorderLayout());
    }

    private Component getCardinal(int i) {
        return cardinal[i];
    }

    private void setCardinal(int i, Component card) {
        if (cardinal[i] != null) {
            remove(cardinal[i]);
        }
        cardinal[i] = card;
        if (card != null) {
            add(card, CARDINAL_NAME[i]);
        }
    }

    public Component getCenter() {
        return getCardinal(CENTER);
    }

    public void setCenter(Component center) {
        setCardinal(CENTER, center);
    }

    public Component getNorth() {
        return getCardinal(NORTH);
    }

    public void setNorth(Component north) {
        setCardinal(NORTH, north);
    }

    public Component getSouth() {
        return getCardinal(SOUTH);
    }

    public void setSouth(Component south) {
        setCardinal(SOUTH, south);
    }

    public Component getEast() {
        return getCardinal(EAST);
    }

    public void setEast(Component east) {
        setCardinal(EAST, east);
    }

    public Component getWest() {
        return getCardinal(WEST);
    }

    public void setWest(Component west) {
        setCardinal(WEST, west);
    }
}