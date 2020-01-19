package de.renew.unify;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import java.lang.reflect.Array;

import java.util.Iterator;
import java.util.Set;


// This is the abstract superclass of tuple and list.
// It captures the common properties of both classes.
// If further subtypes are added to this class, it is
// necessary to update the tuple index class.
public abstract class Aggregate implements Unifiable, Referable, Referer,
                                           Serializable {
    // Serialization Notes:
    // Only complete aggregates are allowed to be serialized.
    // As a consequence, deserialized tuples do not need
    // a recorderChecker: the field is transient and does
    // not get a value on deserialization.
    private BacklinkSet backlinkSet;
    final transient RecorderChecker recorderChecker;
    final Reference[] references;
    private boolean complete; // Are no unknowns part of this object?
    private boolean bound; // Is this object bound to a real value?

    Aggregate(int arity) {
        recorderChecker = new RecorderChecker(null);
        references = Unify.makeUnknownReferenceArray(arity, this);

        complete = (arity == 0);
        bound = (arity == 0);
        if (!complete) {
            backlinkSet = new BacklinkSet();
        }
    }

    Aggregate(Object[] initValues, StateRecorder recorder) {
        recorderChecker = new RecorderChecker(recorder);
        references = Unify.cleanupReferenceArray(initValues, this, recorder);

        complete = calculateComplete();
        bound = calculateBound();
        if (!complete) {
            backlinkSet = new BacklinkSet();
        }
    }

    public Object[] asArray(Class<?> destType) {
        return copyInto((Object[]) Array.newInstance(destType, references.length));
    }

    public Object[] asArray() {
        return copyInto(new Object[references.length]);
    }

    public Object[] copyInto(Object[] result) {
        for (int i = 0; i < references.length; i++) {
            result[i] = references[i].value;
        }
        return result;
    }

    private boolean calculateComplete() {
        for (int i = 0; i < references.length; i++) {
            if (!references[i].complete) {
                return false;
            }
        }
        return true;
    }

    private boolean calculateBound() {
        for (int i = 0; i < references.length; i++) {
            if (!references[i].bound) {
                return false;
            }
        }
        return true;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isBound() {
        return bound;
    }

    public void possiblyCompleted(Set<Notifiable> listeners,
                                  StateRecorder recorder)
            throws Impossible {
        if (references.length == 0) {
            // This should not happen.
            throw new RuntimeException("An empty aggregate became completed.");
        } else {
            if (!complete && calculateComplete()) {
                if (recorder != null) {
                    recorder.record(new StateRestorer() {
                            public void restore() {
                                complete = false;
                            }
                        });
                }
                complete = true;

                // Maybe I am even bound?
                if (calculateBound()) {
                    if (recorder != null) {
                        recorder.record(new StateRestorer() {
                                public void restore() {
                                    bound = false;
                                }
                            });
                    }
                    bound = true;
                }


                // I any case, I have a notification to send to my backlinked objects.
                // I am notifying with myself as the new value.
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
    }

    private void checkBacklinkSet() {
        if (backlinkSet == null) {
            if (!complete) {
                throw new RuntimeException("A complete object "
                                           + "received a backlink. Strange");
            } else {
                throw new RuntimeException("An incomplete object lacks "
                                           + "a backlink set. Strange");
            }
        }
    }

    public void addBacklink(Reference reference, StateRecorder recorder) {
        recorderChecker.checkRecorder(recorder);
        checkBacklinkSet();
        backlinkSet.addBacklink(reference, recorder);
    }

    // Only call this method when it is clear that the concrete subtypes of
    // this and that allow a unification.
    void unifySilently(Aggregate that, StateRecorder recorder,
                       Set<Notifiable> listeners) throws Impossible {
        if (that.references.length != references.length) {
            throw new Impossible();
        }
        for (int i = 0; i < references.length; i++) {
            Unify.unifySilently(references[i].value, that.references[i].value,
                                recorder, listeners);
        }
    }

    public void occursCheck(Unknown that, Set<IdentityWrapper> visited)
            throws Impossible {
        // Am I complete? If yes, no unknown can possibly
        // be contained within me.
        if (complete) {
            return;
        }

        // Did I check myself earlier?
        IdentityWrapper thisWrapper = new IdentityWrapper(this);
        if (visited.contains(thisWrapper)) {
            return;
        }


        // I do not want to check me again.
        visited.add(thisWrapper);

        // Now let's test all the referenced objects.
        for (int i = 0; i < references.length; i++) {
            references[i].occursCheck(that, visited);
        }
    }

    boolean matches(Aggregate that) {
        if (references.length != that.references.length) {
            return false;
        }

        for (int i = 0; i < references.length; i++) {
            if (references[i].value == null) {
                if (that.references[i].value != null) {
                    return false;
                }
            } else {
                if (!references[i].value.equals(that.references[i].value)) {
                    return false;
                }
            }
        }

        return true;
    }

    protected int refHash(int i) {
        if (references[i].value == null) {
            return 1;
        } else {
            return references[i].value.hashCode();
        }
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except checking additional error conditions.
     * Throws NotSerializableException if not complete.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        if (isComplete()) {
            out.defaultWriteObject();
        } else {
            throw new NotSerializableException("de.renew.unify.Aggregate: "
                                               + this + " is not complete.");
        }
    }

    public abstract Iterator<Object> iterator();
}