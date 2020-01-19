package de.renew.unify;

import java.io.Serializable;

import java.util.Set;


/* Tried to delay the field <code>value</code>
 * to cut down recursion depth on serialization.
 * But that did cause a problem with the
 * TupleIndexes of PlaceInstances: at the time
 * a tuple was included into the index, its
 * components were still missing, so the index
 * was incomplete.
 */
public class Reference implements Serializable {
    Object value;
    boolean bound;
    boolean complete;
    Referer referer;

    public Reference(Object value, Referer referer, StateRecorder recorder) {
        // Get rid of variables.
        if (value instanceof Variable) {
            value = ((Variable) value).getValue();
        }


        // Initialize.
        this.value = value;
        complete = Unify.isComplete(value);
        bound = Unify.isBound(value);

        this.referer = referer;


        // If I reference a referable that is not complete,
        // I have to register as a backlink. However, if the referable
        // is complete, I need not expect a notification, so why bother?
        // Adding a backlink would only make garbage collection
        // more difficult.
        //
        // Note that the special case of complete referenced objects
        // occurs when complete unifiable objects are send though
        // a copier.
        if (!complete && value instanceof Referable) {
            ((Referable) value).addBacklink(this, recorder);
        }
    }

    public void occursCheck(Unknown that, Set<IdentityWrapper> visited)
            throws Impossible {
        // Now let's test the referenced object.
        if (value instanceof Referable) {
            ((Referable) value).occursCheck(that, visited);
        }
    }

    public void update(final Referable oldValue, final Object newValue,
                       Set<Notifiable> listeners, StateRecorder recorder)
            throws Impossible {
        // A strange phenomenon: The completion of a subobject might
        // be indicated multiple times. Scenario: A=[B,C], B=[C]
        // and C becomes complete. Now C notifies B, which becomes
        // complete and notifies A in turn, which is complete now, too.
        // Now the notification from C to A (C occurs in A)
        // remains to be done, but A is already complete. Therefore
        // I must be prepared to be notified although I am already
        // complete. I simply discard any late notifications.
        if (!complete) {
            if (value == oldValue && oldValue != newValue) {
                if (recorder != null) {
                    recorder.record(new StateRestorer() {
                            public void restore() {
                                value = oldValue;
                            }
                        });
                }
                value = newValue;
            }

            if (Unify.isComplete(value)) {
                if (recorder != null) {
                    recorder.record(new StateRestorer() {
                            public void restore() {
                                complete = false;
                            }
                        });
                }
                complete = true;

                if (Unify.isBound(value)) {
                    if (recorder != null) {
                        recorder.record(new StateRestorer() {
                                public void restore() {
                                    bound = false;
                                }
                            });
                    }
                    bound = true;
                }


                // Notify the referer about the change of completeness.
                referer.possiblyCompleted(listeners, recorder);
            } else {
                if (value instanceof Referable) {
                    Referable referable = (Referable) value;
                    referable.addBacklink(this, recorder);
                } else {
                    throw new RuntimeException("An incomplete value that is "
                                               + "referred to was not referable. Strange");
                }
            }
        }
    }

    public Referer getReferer() {
        return referer;
    }

    public Object getValue() {
        return value;
    }
}