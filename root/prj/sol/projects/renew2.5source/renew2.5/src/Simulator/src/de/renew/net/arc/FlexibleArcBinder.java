package de.renew.net.arc;

import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;

import de.renew.net.TokenReserver;

import de.renew.unify.List;
import de.renew.unify.Unify;

import de.renew.util.Value;

import java.lang.reflect.Array;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


class FlexibleArcBinder implements Binder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FlexibleArcBinder.class);

    /**
     * This is the flexible arc occurrence that created this binder and
     * wants to be informed about the tokens that are actually moved.
     * This information is passed via the field inTokens.
     *
     * @see FlexibleArcOccurrence#inTokens
     */
    FlexibleArcOccurrence occurrence;

    FlexibleArcBinder(FlexibleArcOccurrence occurrence) {
        this.occurrence = occurrence;
    }

    public int bindingBadness(Searcher searcher) {
        if (Unify.isBound(occurrence.tokenVar)) {
            return 1;
        } else {
            // We must not try to bind the variable.
            return BindingBadness.max;
        }
    }

    private void rememberSingleToken(Object tok) {
        occurrence.inTokens.addElement(Unify.copyBoundValue(tok));
    }

    public void bind(Searcher searcher) {
        if (!Unify.isBound(occurrence.tokenVar)) {
            // This should not happen.
            throw new RuntimeException("Flexible arc binder was invoked for an "
                                       + "incomplete value.");
        }

        // The array is analysed locally and need not be copied.
        Object tokens = occurrence.tokenVar.getValue();

        occurrence.inTokens = new Vector<Object>();


        //We can not check for Enumrations and Iterators here as we would use them
        //while checking for possible bindings.
        if (tokens == null) {
            // Let's ignore this arc. Probably the null value is meant as
            // a substitute for an empty array.

            /*} else if (tokens instanceof Iterator) {
                for (Iterator i = (Iterator) tokens; i.hasNext();) {
                                rememberSingleToken(i.next());
                }
            } else if (tokens instanceof Enumeration) {
                Enumeration enumeration = (Enumeration) tokens;
                while (enumeration.hasMoreElements()) {
                                rememberSingleToken(enumeration.nextElement());
                }*/
        } else if (tokens.getClass().isArray()) {
            Class<?> elementType = tokens.getClass().getComponentType();

            int n = Array.getLength(tokens);

            for (int i = 0; i < n; i++) {
                Object tok = null;
                try {
                    // Extract a single token and copy it, so that
                    // backtracking does not hurt.
                    tok = Array.get(tokens, i);

                    if (elementType.isPrimitive()) {
                        tok = new Value(tok);
                    }

                    if (occurrence.arc.forwardFunction != null) {
                        Object convertedToken = occurrence.arc.forwardFunction
                                                    .function(tok);

                        if (occurrence.arc.backwardFunction != null) {
                            Object restoredToken = occurrence.arc.backwardFunction
                                                   .function(convertedToken);
                            Unify.unify(tok, restoredToken, null);
                        }

                        tok = convertedToken;
                    }

                    rememberSingleToken(tok);
                } catch (Exception e) {
                    // Abort this search process.
                    // A conversion did not succeed.
                    // logger.error(e.getMessage() ,e);
                    return;
                }
            }
        } else if (tokens instanceof List) {
            List current = (List) tokens;
            while (!current.isNull()) {
                rememberSingleToken(current.head());
                if (!(current.tail() instanceof List)) { // stop if open/corrupted list
                    break;
                }
                current = (List) current.tail();
            }
        } else if (tokens instanceof Collection) {
            Collection<?> coll = (Collection<?>) tokens;
            for (Iterator<?> i = coll.iterator(); i.hasNext();) {
                rememberSingleToken(i.next());
            }
        }

        // Tuples should not be used. See discussion with Olaf

        /*else if (tokens instanceof Tuple) {
            Tuple tuple = (Tuple) tokens;
            for (Iterator i = tuple.iterator(); i.hasNext();) {
                rememberSingleToken(i.next());
            }
        } else {
            // As a last resort, we insert the value itself into the
            // the list. This is barely acceptable, but at least we
            // know that the place is not typed, otherwise a type
            // check would have detected the illegal situation.
            rememberSingleToken(tokens);
        }*/
        if (!occurrence.arc.isOutputArc() && occurrence.inTokens.size() > 0) {
            // Make sure that the place instance notifies
            // the searchable if its marking changes,
            // because in that case the possible binding would have
            // to be rechecked. This is only required for input
            // arcs, because output arcs cannot disable or enable
            // a transition, and only if a token is actually moved.
            searcher.insertTriggerable(occurrence.placeInstance.triggerables());
        }

        // Now we start computation with side effects.
        boolean success = true;
        int maxOk = 0;
        TokenReserver tokenReserver = TokenReserver.getInstance(searcher);
        while (success && maxOk < occurrence.inTokens.size()) {
            try {
                success = tokenReserver.removeToken(occurrence.placeInstance,
                                                    occurrence.inTokens
                              .elementAt(maxOk), 0);
                if (success) {
                    // Reservation was successful.
                    maxOk++;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                success = false;
            }
        }

        if (success) {
            // All reservations succeeded.
            searcher.search();
        }

        // Undo the successful reservations.
        while (maxOk > 0) {
            maxOk--;
            tokenReserver.unremoveToken(occurrence.placeInstance,
                                        occurrence.inTokens.elementAt(maxOk), 0);
        }


        // The set of tokens has become invalid.
        occurrence.inTokens = null;
    }
}