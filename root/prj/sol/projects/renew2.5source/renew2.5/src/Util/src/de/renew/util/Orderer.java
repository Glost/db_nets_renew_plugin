package de.renew.util;



/**
 * I am responsible for creating
 * unique numbers that order the accesses to
 * lock objects. This is useful in deadlock prevention
 * schemes. Every order ticket that I create will be smaller
 * than every prior order ticket.
 **/
public class Orderer {

    /**
     * The next order ticket that will be made public.
     **/
    private static long nextOrder = -1;

    /**
     * Get an order ticket.
     *
     * @return long negative integer that is guaranteed to be unique
     *   for all calls to this method.
     **/
    public static synchronized long getTicket() {
        // Only I may access nextOrder. This allows me
        // to uniquely allocate an order ticket.
        //
        // If a process locks place instances, it locks
        // them in the order of their creation, so that
        // no deadlock situations can occur.
        return nextOrder--;
    }
}