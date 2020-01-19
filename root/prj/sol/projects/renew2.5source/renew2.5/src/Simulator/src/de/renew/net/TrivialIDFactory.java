package de.renew.net;



/**
 * This ID factory creates unique IDs by counting from
 * 0 or a given value upwards. Using the start value,
 * it is applicable for database backing.
 * The factory will generate IDs that are unique within
 * a JVM.
 */
public class TrivialIDFactory implements IDFactory {
    private long count;

    public TrivialIDFactory() {
        count = 0;
    }

    public TrivialIDFactory(int startCount) {
        count = startCount;
    }

    /**
    * Create another ID. Each call will return the string
    * representation of a number that advances by one on
    * each call.
    *
    * @return a unique string
    */
    public String createID() {
        return String.valueOf(count++);
    }
}