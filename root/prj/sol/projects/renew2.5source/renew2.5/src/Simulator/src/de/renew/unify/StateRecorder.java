package de.renew.unify;

import java.util.Vector;


public class StateRecorder {
    Vector<StateRestorer> savedObjects;

    public StateRecorder() {
        savedObjects = new Vector<StateRestorer>();
    }

    public void record(StateRestorer obj) {
        savedObjects.addElement(obj);
    }

    public int checkpoint() {
        return savedObjects.size();
    }

    public void restore(int checkpoint) {
        if (checkpoint < savedObjects.size()) {
            for (int i = savedObjects.size() - 1; i >= checkpoint; i--) {
                savedObjects.elementAt(i).restore();
            }
            savedObjects.setSize(checkpoint);
        }
    }

    public void restore() {
        restore(0);
    }
}