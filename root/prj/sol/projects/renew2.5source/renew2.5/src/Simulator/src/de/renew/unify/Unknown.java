package de.renew.unify;

import java.util.HashSet;
import java.util.Set;


final class Unknown implements Unifiable, Referable {
    final private BacklinkSet backlinkSet = new BacklinkSet();
    final private RecorderChecker recorderChecker;

    Unknown() {
        recorderChecker = new RecorderChecker(null);
    }

    public boolean isComplete() {
        return false;
    }

    public boolean isBound() {
        return false;
    }

    public void addBacklink(Reference reference, StateRecorder recorder) {
        recorderChecker.checkRecorder(recorder);
        backlinkSet.addBacklink(reference, recorder);
    }

    // Moved here so that only an unknown needs to know about its
    // unification strategy.
    void unifySilently(Object obj, StateRecorder recorder,
                       Set<Notifiable> listeners) throws Impossible {
        if (obj instanceof Unknown) {
            Unknown that = (Unknown) obj;
            if (that.backlinkSet.size() < backlinkSet.size()) {
                // The other unknown posses fewer backlinks to be updated.
                // It should be updated, so that we can safe some work.
                that.unifySilently(this, recorder, listeners);
                // Nothing more to do here.
                return;
            }

            // I'll do it myself, it's cheaper this way.
        } else if (obj instanceof Referable) {
            ((Referable) obj).occursCheck(this, new HashSet<IdentityWrapper>());
        }


        // Make the update.
        backlinkSet.updateBacklinked(this, obj, listeners, recorder);
    }

    public void occursCheck(Unknown that, Set<IdentityWrapper> visited)
            throws Impossible {
        // Do I cause a cycle?
        if (this == that) {
            throw new Impossible();
        }
    }

    /**
     * Disable the normal <code>equals</code> and <code>hashCode</code>
     * methods.  Because unknowns can change their values, it would be
     * desastrous to insert them into a hashtable.
     *
     * @throws RuntimeException always.
     **/
    public boolean equals(Object that) {
        throw new RuntimeException("Somebody compared an unknown.");
    }

    /**
     * Disable the normal <code>equals</code> and <code>hashCode</code>
     * methods.  Because unknowns can change their values, it would be
     * desastrous to insert them into a hashtable.
     *
     * @throws RuntimeException always.
     **/
    public int hashCode() {
        throw new RuntimeException("Somebody took the hash code of an unknown.");
    }

    public String toString() {
        return "<UNKNOWN!>";
    }
}