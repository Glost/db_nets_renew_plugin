package de.renew.formalism.fsnet;

import java.util.Vector;


public class BeanVector extends Vector<Object> {
    // rename some methods to standard names:
    public Object getElement(int index) {
        return elementAt(index);
    }

    public int getElementCount() {
        return size();
    }
}