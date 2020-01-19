/**
 *
 */
package de.renew.util;



/**
 * Instances of this interface subject themselves to ordered
 * locking.  Each instance should announce a unique lock priority
 * via {@link #lockPriority()}.  To ensure uniqueness, the priority
 * value should be obtained via {@link Orderer#getTicket()}.
 * <p>
 * When locks on multiple OrderedLockable instances are required,
 * requesters must adhere to the lock order imposed by the
 * announced priority tickets.  As support, the class
 * {@link LockComparator} allows natural sorting of lockables
 * in a {@link java.util.SortedSet} or similar classes.
 * </p>
 *
 * @author Olaf Kummer (as part of EarlyExecutable)
 * @author Michael Duvigneau (extracted into OrderedLockable,
 *                            extended documentation)
 **/
public interface OrderedLockable {

    /**
     * Get the priority of locking this executable.
     * Small numbers signal early locking.
     * <p>
     * Lock priority tickets should be obtained from
     * {@link Orderer} to ensure uniqueness.
     * </p>
     *
     * @return the priority as a long integer value
     **/
    long lockPriority();

    /**
     * Lock the resources required to execute this lockable.
     * Multiple locks should be acquired with respect to
     * {@link #lockPriority()}.
     * <p>
     * After calling this method, it becomes mandatory to call
     * {@link #unlock()} later.  The recommended pattern is:
     * </p>
     * <pre>
     *   lockable.lock();
     *   try {
     *     // critical section
     *   } finally {
     *     lockable.unlock();
     *   }
     * </pre>
     **/
    void lock();

    /**
     * Unlock the resources without executing this lockable.
     * It is mandatory to call this method once {@link #lock()}
     * has been called.
     **/
    void unlock();
}