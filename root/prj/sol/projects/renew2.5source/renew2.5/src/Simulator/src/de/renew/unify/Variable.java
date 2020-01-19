package de.renew.unify;

import java.util.HashSet;
import java.util.Set;


public final class Variable implements Unifiable, Referer {
    final private Set<Notifiable> myListeners = new HashSet<Notifiable>();
    final private Reference reference;
    final private RecorderChecker recorderChecker;

    public Variable() {
        recorderChecker = new RecorderChecker(null);
        reference = new Reference(new Unknown(), this, null);
    }

    public Variable(Object initValue, StateRecorder recorder) {
        recorderChecker = new RecorderChecker(recorder);
        reference = new Reference(initValue, this, recorder);
    }

    public boolean isComplete() {
        return reference.complete;
    }

    public boolean isBound() {
        return reference.bound;
    }

    public void possiblyCompleted(Set<Notifiable> listeners,
                                  StateRecorder recorder)
            throws Impossible {
        // I own only one reference, therefore I am guaranteed to be completed.
        // Am I bound, too?
        if (isBound()) {
            listeners.addAll(myListeners);
        }
    }

    public Object getValue() {
        return reference.value;
    }

    public void addListener(final Notifiable listener, StateRecorder recorder)
            throws Impossible {
        recorderChecker.checkRecorder(recorder);

        if (!myListeners.contains(listener)) {
            myListeners.add(listener);
            if (recorder != null) {
                recorder.record(new StateRestorer() {
                        public void restore() {
                            myListeners.remove(listener);
                        }
                    });
            }


            // If I am already bound, i will not have another opportunity to
            // send a notification. I'll do it now, before it's too late.
            if (isBound()) {
                listener.boundNotify(recorder);
            }
        }
    }

    /**
     * @author Friedrich Delgado Friedrichs <friedel@nomaden.org>
     *
     * Since this class is final and Reference etc. are also opaque, non-Java
     * languages need a chance to get the reference of a local variable for
     * adding a backlink to it.
     *
     * @return the reference
     */
    public Reference getReference() {
        return reference;
    }
}