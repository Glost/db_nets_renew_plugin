package de.renew.formalism.java;



/**
 * This class extends the generated class {@link ParseException} to store
 * additional problem specific information.
 *
 * @author 6hauster
 *
 */
public class ExtendedParseException extends ParseException {
    private Object o;

    /**
     * @param message the message
     * @param o arbitrary problem specific information
     */
    public ExtendedParseException(String message, Object o) {
        super(message);
        this.o = o;
    }

    public Object getProblemSpecificInformation() {
        return this.o;
    }
}