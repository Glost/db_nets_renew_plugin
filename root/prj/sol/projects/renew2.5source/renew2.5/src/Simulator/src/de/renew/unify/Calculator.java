package de.renew.unify;

import java.util.Set;


public final class Calculator implements Unifiable, Referable, Referer {
    private BacklinkSet backlinkSet;
    final private Reference reference;
    final private RecorderChecker recorderChecker;
    final private Class<?> type;

    Calculator(Class<?> type, Object object, StateRecorder recorder) {
        recorderChecker = new RecorderChecker(recorder);
        reference = new Reference(object, this, recorder);
        this.type = type;
        if (!isComplete()) {
            backlinkSet = new BacklinkSet();
        }
    }

    public boolean isComplete() {
        return reference.complete;
    }

    public boolean isBound() {
        return false;
    }

    public Class<?> getType() {
        return type;
    }

    private void checkBacklinkSet() {
        if (backlinkSet == null) {
            if (isComplete()) {
                throw new RuntimeException("A complete tuple "
                                           + "received a backlink. Strange");
            } else {
                throw new RuntimeException("An incomplete tuple lacks "
                                           + "a backlink set. Strange");
            }
        }
    }

    public void addBacklink(Reference reference, StateRecorder recorder) {
        recorderChecker.checkRecorder(recorder);
        checkBacklinkSet();
        backlinkSet.addBacklink(reference, recorder);
    }

    public void possiblyCompleted(Set<Notifiable> listeners,
                                  StateRecorder recorder)
            throws Impossible {
        if (isComplete()) {
            checkBacklinkSet();
            backlinkSet.updateBacklinked(this, this, listeners, recorder);


            // I have now done the update, so the backlinked objects should be
            // forgotten. In fact they have to be dumped into the state
            // recorder for future backtracking.
            final BacklinkSet oldBacklinkSet = backlinkSet;
            if (recorder != null) {
                recorder.record(new StateRestorer() {
                        public void restore() {
                            backlinkSet = oldBacklinkSet;
                        }
                    });
            }
            backlinkSet = null;
        }
    }

    public void occursCheck(Unknown that, Set<IdentityWrapper> visited)
            throws Impossible {
        // Am I complete? If yes, no unknown can possibly
        // be contained within me.
        if (reference.complete) {
            return;
        }

        // Did I check myself earlier?
        IdentityWrapper thisWrapper = new IdentityWrapper(this);
        if (visited.contains(thisWrapper)) {
            return;
        }


        // I do not want to check me again.
        visited.add(thisWrapper);

        reference.occursCheck(that, visited);
    }

    /**
     * Disable the normal <code>equals</code> and <code>hashCode</code>
     * methods.  Because calculators are not values, it would be desastrous
     * to insert them into a hashtable.
     *
     * @throws RuntimeException always.
     **/
    public boolean equals(Object that) {
        throw new RuntimeException("Somebody compared a calculator.");
    }

    /**
     * Disable the normal <code>equals</code> and <code>hashCode</code>
     * methods.  Because calculators are not values, it would be desastrous
     * to insert them into a hashtable.
     *
     * @throws RuntimeException always.
     **/
    public int hashCode() {
        throw new RuntimeException("Somebody took the hash code of a calculator.");
    }

    public String toString() {
        return "<pending: " + getType() + ">";
    }
}