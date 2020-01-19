package de.renew.net.arc;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.Putting;
import de.renew.engine.searcher.LateExecutable;
import de.renew.engine.searchqueue.SearchQueue;

import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;
import de.renew.unify.List;
import de.renew.unify.Variable;

import de.renew.util.Value;

import java.lang.reflect.Array;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;


class FlexibleOutArcExecutable implements LateExecutable {
    PlaceInstance pInstance;
    TransitionInstance tInstance;
    Variable tokenVar;
    FlexibleArc arc;

    FlexibleOutArcExecutable(PlaceInstance placeInstance,
                             TransitionInstance tInstance, Variable tokenVar,
                             FlexibleArc arc) {
        this.pInstance = placeInstance;
        this.tInstance = tInstance;
        this.tokenVar = tokenVar;
        this.arc = arc;
    }

    public int phase() {
        return OUTPUT;
    }

    // We can put a token into an output place quickly.
    public boolean isLong() {
        return false;
    }

    private void putSingleToken(StepIdentifier stepIdentifier, Object tok) {
        if (arc.forwardFunction != null) {
            try {
                tok = arc.forwardFunction.function(tok);
            } catch (Impossible e) {
                // This should not happen.
            }
        }


        // We ignore the backward funtion. It is supposed to detect
        // errors for input arcs, but for output arcs it should not be set.
        if (arc.getTrace()) {
            // log activities on net level
            SimulatorEventLogger.log(stepIdentifier,
                                     new Putting(tok, pInstance), pInstance);
        }

        pInstance.insertToken(tok, SearchQueue.getTime());
    }

    public void execute(StepIdentifier stepIdentifier) {
        Object tokens = tokenVar.getValue();

        pInstance.lock.lock();
        try {
            if (tokens == null) {
                // Let's ignore this arc. Probably we are not meant to
                // insert a single null token into the place, but rather 
                // the null is a substitute for an empty array.
                //
                // Actually, who knows? But we cannot complain, because we
                // are in the late phase of firing.
            } else if (tokens instanceof Iterator) {
                // The place must be untyped, or a type error would have
                // occurred earlier on.
                for (Iterator<?> i = (Iterator<?>) tokens; i.hasNext();) {
                    putSingleToken(stepIdentifier, i.next());
                }
            } else if (tokens instanceof Enumeration) {
                // The place must be untyped, or a type error would have
                // occurred earlier on.
                Enumeration<?> enumeration = (Enumeration<?>) tokens;
                while (enumeration.hasMoreElements()) {
                    putSingleToken(stepIdentifier, enumeration.nextElement());
                }
            } else if (tokens.getClass().isArray()) {
                int n = Array.getLength(tokens);

                Class<?> elementType = tokens.getClass().getComponentType();
                for (int i = 0; i < n; i++) {
                    Object tok = Array.get(tokens, i);
                    if (elementType.isPrimitive()) {
                        tok = new Value(tok);
                    }
                    putSingleToken(stepIdentifier, tok);
                }
            } else if (tokens instanceof List) {
                // The place must be untyped, or a type error would have
                // occurred earlier on.
                List current = (List) tokens;
                while (!current.isNull()) {
                    putSingleToken(stepIdentifier, current.head());
                    if (!(current.tail() instanceof List)) { // stop if open/corrupted list
                        break;
                    }
                    current = (List) current.tail();
                }
            } else if (tokens instanceof Collection) {
                // The place must be untyped, or a type error would have
                // occurred earlier on.
                Collection<?> coll = (Collection<?>) tokens;
                for (Iterator<?> i = coll.iterator(); i.hasNext();) {
                    putSingleToken(stepIdentifier, i.next());
                }
            }
            //          Tuples should not be used. See discussion with Olaf

            /*else if (tokens instanceof Tuple) {
                Tuple tuple = (Tuple) tokens;
                for (Iterator i = tuple.iterator(); i.hasNext();) {
                    putSingleToken(stepIdentifier, i.next());
                }
            } */
            else {
                // As a last resort, we insert the value itself into the
                // place. This is barely acceptable, but at least we
                // know that the place is not typed, otherwise a type
                // check would have detected the illegal situation.
                putSingleToken(stepIdentifier, tokens);
            }
        } finally {
            pInstance.lock.unlock();
        }
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
    }
}