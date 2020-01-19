package de.renew.unify;

import de.renew.util.Types;
import de.renew.util.Value;

import java.util.Set;


public class TypeConstrainer implements Referer {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TypeConstrainer.class);
    final Class<?> type;
    final private Reference reference;

    private TypeConstrainer(Class<?> type, Object value, StateRecorder recorder)
            throws Impossible { //NOTICEthrows
        this.type = type;
        reference = new Reference(value, this, recorder);
    }

    public static void constrain(Class<?> type, Object value,
                                 StateRecorder recorder)
            throws Impossible {
        if (type == Types.UNTYPED) {
            // Nothing to do.
            return;
        } else if (Unify.isComplete(value)) {
            // Check value now, no need to create a type constrainer.
            check(type, value);
        } else {
            new TypeConstrainer(type, value, recorder);
        }
    }

    private static void check(Class<?> type, Object value)
            throws Impossible {
        if (value instanceof Calculator) {
            Class<?> valueType = ((Calculator) value).getType();

            if (type != valueType) {
                if (type.isPrimitive()
                            || !Types.allowsReferenceWidening(valueType, type)) {
                    try {
                        String message = "Type mismatch: Primitive " + type
                                         + ", calculation result: " + valueType;
                        logger.debug(message);
                        throw new Impossible(message);
                    } catch (RuntimeException e) {
                        String message = "Type mismatch (diagnostics not available).";
                        logger.debug(message, e);
                        throw new Impossible(message, e);
                    }
                }
            }
        } else if (value instanceof Value) {
            if (!type.isPrimitive()
                        || !Types.objectify(type)
                                         .isInstance(((Value) value).value)) {
                try {
                    String message = "Type mismatch: Class " + type
                                     + ", primitive value: " + value;
                    logger.debug(message);
                    throw new Impossible(message);
                } catch (RuntimeException e) {
                    String message = "Type mismatch (diagnostics not available).";
                    logger.debug(message, e);
                    throw new Impossible(message, e);
                }
            }
        } else if (value == null) {
            if (type.isPrimitive()) {
                try {
                    String message = "Type mismatch: Primitive " + type
                                     + ", null reference";
                    logger.debug(message);
                    throw new Impossible(message);
                } catch (RuntimeException e) {
                    String message = "Type mismatch (diagnostics not available).";
                    logger.debug(message, e);
                    throw new Impossible(message, e);
                }
            }
        } else if (!type.isInstance(value)) {
            try {
                String message = "Type mismatch: Class " + type + ", object: "
                                 + value;
                logger.debug(message);
                throw new Impossible(message);
            } catch (RuntimeException e) {
                String message = "Type mismatch (diagnostics not available).";
                logger.debug(message, e);
                throw new Impossible(message, e);
            }
        }
    }

    public void possiblyCompleted(Set<Notifiable> listeners,
                                  StateRecorder recorder)
            throws Impossible {
        // I own only one reference, therefore that reference must have become
        // completed.
        check(type, reference.value);
    }
}