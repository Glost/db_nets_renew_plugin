/*
 * @(#)StorableInOut.java 5.1
 *
 */
package CH.ifa.draw.util;

import java.net.URI;

import java.util.Vector;


/**
 * An abstract superclass of storable input and output streams.
 * StorableInOut handles the object identity of the stored objects
 * through a map and preserves the original filename.
 *
 * @see Storable
 * @see StorableInput
 * @see StorableOutput
 */
public abstract class StorableInOut extends Object {
    private Vector<Storable> fMap = new Vector<Storable>();
    private URI location;

    protected StorableInOut() {
        this(null);
    }

    protected StorableInOut(URI location) {
        this.location = location;
    }

    public URI getURI() {
        return location;
    }

    protected boolean mapped(Storable storable) {
        return fMap.contains(storable);
    }

    protected void map(Storable storable) {
        if (!fMap.contains(storable)) {
            fMap.addElement(storable);
        }
    }

    protected Storable retrieve(int ref) {
        return fMap.elementAt(ref);
    }

    protected int getRef(Storable storable) {
        return fMap.indexOf(storable);
    }
}