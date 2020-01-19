package de.renew.net;

class IDCounter implements java.io.Serializable {
    private String id;
    private int reserves;

    IDCounter(String id) {
        this.id = id;
        reserves = 0;
    }

    String getID() {
        return id;
    }

    void reserve() {
        reserves++;
    }

    void unreserve() {
        reserves--;
    }

    boolean isDiscardable() {
        return reserves == 0;
    }
}