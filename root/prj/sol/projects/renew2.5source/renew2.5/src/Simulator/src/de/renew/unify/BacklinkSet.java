package de.renew.unify;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


class BacklinkSet implements Serializable {

    /**
     * Up to six backlinked objects can be handled without
     * a special data structure.
     **/
    private int cnt = 0;
    private Reference[] backlinks = new Reference[6];
    private Set<IdentityWrapper> externalSet = null;

    private boolean includes(Reference reference) {
        if (externalSet != null) {
            return externalSet.contains(new IdentityWrapper(reference));
        } else {
            switch (cnt) {
            case 6:
                return reference == backlinks[0] || reference == backlinks[1]
                       || reference == backlinks[2]
                       || reference == backlinks[3]
                       || reference == backlinks[4]
                       || reference == backlinks[5];
            case 5:
                return reference == backlinks[0] || reference == backlinks[1]
                       || reference == backlinks[2]
                       || reference == backlinks[3]
                       || reference == backlinks[4];
            case 4:
                return reference == backlinks[0] || reference == backlinks[1]
                       || reference == backlinks[2]
                       || reference == backlinks[3];
            case 3:
                return reference == backlinks[0] || reference == backlinks[1]
                       || reference == backlinks[2];
            case 2:
                return reference == backlinks[0] || reference == backlinks[1];
            case 1:
                return reference == backlinks[0];
            }
            return false;
        }
    }

    /**
     * Include an element. This element must not be
     * included in the set so far.
     **/
    private void includeNonElement(Reference reference) {
        if (externalSet == null && cnt == 6) {
            externalSet = new HashSet<IdentityWrapper>();
            for (int i = 0; i < 6; i++) {
                externalSet.add(new IdentityWrapper(backlinks[i]));
            }
        }
        if (externalSet != null) {
            externalSet.add(new IdentityWrapper(reference));
        } else {
            backlinks[cnt++] = reference;
        }
    }

    /**
     * Exclude an element. This element must be
     * included in the set.
     **/
    private void exclude(Reference reference) {
        if (externalSet != null) {
            // I do not switch back to array once I have created
            // the hashed set. It is probable that the number
            // of links will grow again in this case.
            externalSet.remove(new IdentityWrapper(reference));
        } else {
            cnt--;
            if (backlinks[cnt] != reference) {
                if (backlinks[0] == reference) {
                    backlinks[0] = backlinks[cnt];
                } else if (backlinks[1] == reference) {
                    backlinks[1] = backlinks[cnt];
                } else if (backlinks[2] == reference) {
                    backlinks[2] = backlinks[cnt];
                } else {
                    throw new RuntimeException("Backlink not found. Strange. ");
                }
            }


            // Enable garbage collection.
            backlinks[cnt] = null;
        }
    }

    // Return the number of references known to this object.
    // Knowing this number is useful to optimize reference
    // redirection by minimizing the number of references changed.
    int size() {
        if (externalSet != null) {
            return externalSet.size();
        } else {
            return cnt;
        }
    }

    void addBacklink(final Reference reference, StateRecorder recorder) {
        if (!includes(reference)) {
            includeNonElement(reference);
            if (recorder != null) {
                recorder.record(new StateRestorer() {
                        public void restore() {
                            exclude(reference);
                        }
                    });
            }
        }
    }

    void updateBacklinked(Referable oldValue, Object newValue,
                          Set<Notifiable> listeners, StateRecorder recorder)
            throws Impossible {
        // I will tell all my backlinked objects that the given
        // value is now relevant instead of myself. (Maybe I *am* the
        // new object, but who knows.)
        //
        // I assume that no new backlinks will be added to myself
        // during this procedure.
        if (externalSet != null) {
            Iterator<IdentityWrapper> enumeration = externalSet
                                                        .iterator();
            while (enumeration.hasNext()) {
                IdentityWrapper wrapper = enumeration.next();
                Reference reference = (Reference) wrapper.getObject();
                reference.update(oldValue, newValue, listeners, recorder);
            }
        } else {
            for (int i = 0; i < cnt; i++) {
                backlinks[i].update(oldValue, newValue, listeners, recorder);
            }
        }
    }
}