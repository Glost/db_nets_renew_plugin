package de.renew.unify;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public final class Unify {
    // Provide a copier that can be used to copy bound values.
    private static Copier tempCopier = new Copier();

    private Unify() {
    }

    static Reference[] cleanupReferenceArray(Object[] orgArray,
                                             Referer referer,
                                             StateRecorder recorder) {
        Reference[] referencers = new Reference[orgArray.length];
        for (int i = 0; i < orgArray.length; i++) {
            referencers[i] = new Reference(orgArray[i], referer, recorder);
        }
        return referencers;
    }

    static Reference[] makeReferenceArray(Object initValue, Referer referer,
                                          StateRecorder recorder) {
        Reference[] referencers = new Reference[1];
        referencers[0] = new Reference(initValue, referer, recorder);
        return referencers;
    }

    static Reference[] makeUnknownReferenceArray(int arity, Referer referer) {
        Reference[] referencers = new Reference[arity];
        for (int i = 0; i < arity; i++) {
            referencers[i] = new Reference(new Unknown(), referer, null);
        }
        return referencers;
    }

    // The next two methods probe the status of a unifiable object.
    // An object is complete when no further unification
    // within its scope can change its state, i.e. it is
    // fully calculatable.
    // An object is bound if it is fully calculated.
    //
    // An object may be complete and not bound, if it depends on
    // calculations registered with the checker.
    // A bound object is always complete.
    static boolean isComplete(Object o) {
        if (o instanceof Unifiable) {
            // Let's ask the object.
            return ((Unifiable) o).isComplete();
        } else {
            // Ordinary objects are always complete.
            return true;
        }
    }

    public static boolean isBound(Object o) {
        if (o instanceof Unifiable) {
            // Let's ask the object.
            return ((Unifiable) o).isBound();
        } else {
            // Ordinary objects are always bound.
            return true;
        }
    }

    // This is the internal unification method that records
    // all required notifications in a given updatable set.
    // It is assumed that all listeners will be notified later
    // on.
    static void unifySilently(Object left, Object right,
                              StateRecorder recorder, Set<Notifiable> listeners)
            throws Impossible {
        // Remove variables, because they are only wrappers
        // for the real values.
        if (left instanceof Variable) {
            left = ((Variable) left).getValue();
        }
        if (right instanceof Variable) {
            right = ((Variable) right).getValue();
        }

        // Now do the unification.
        if (left == right) {
            return;
        } else if (left instanceof SilentlyUnifiable) {
            ((SilentlyUnifiable) left).unifySilently(right, recorder, listeners);
        } else if (left instanceof Unknown) {
            ((Unknown) left).unifySilently(right, recorder, listeners);
        } else if (right instanceof Unknown) {
            ((Unknown) right).unifySilently(left, recorder, listeners);
        } else if (left instanceof Tuple) {
            if (right instanceof Tuple) {
                ((Tuple) left).unifySilently((Tuple) right, recorder, listeners);
            } else {
                throw new Impossible();
            }
        } else if (left instanceof List) {
            if (right instanceof List) {
                ((List) left).unifySilently((List) right, recorder, listeners);
            } else {
                throw new Impossible();
            }
        } else if (left == null || right == null || left instanceof Calculator
                           || right instanceof Calculator) {
            throw new Impossible(); // We already checked for identity.
        } else {
            if (!left.equals(right)) {
                throw new Impossible();
            }
        }
    }

    static public void unify(Object left, Object right, StateRecorder recorder)
            throws Impossible {
        // Prepare a set of listeners that will hold the
        // listeners to invoke.
        Set<Notifiable> listeners = new HashSet<Notifiable>();


        // Make the unification.
        unifySilently(left, right, recorder, listeners);


        // Notify all listeners.
        Iterator<Notifiable> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().boundNotify(recorder);
        }
    }

    static public Object copyBoundValue(Object val) {
        if (!isBound(val)) {
            throw new RuntimeException("To copy unbound values, use a copier.");
        }
        return tempCopier.copy(val);
    }
}