package de.renew.util;

import java.util.Comparator;


/**
 * This comparator can be used to order {@link OrderedLockable}
 * instances in a {@link java.util.SortedSet} or similar classes
 * by their lock priority.
 *
 * @author Olaf Kummer (as part of de.renew.engine.simulator)
 * @author Michael Duvigneau (moved to de.renew.util, added documentation)
 **/
public class LockComparator implements Comparator<OrderedLockable> {

    /**
     * {@inheritDoc}
     * @param fst  the first OrderedLockable to compare
     * @param snd  the second OrderedLockabel to compare
     * @return -1 if <code>fst</code> should be locked first (has a lower
     *            {@link OrderedLockable#lockPriority()} value. <br />
     *         +1 if <code>snd</code> should be locked first.
     *         0  if both share the same {@link OrderedLockable#lockPriority()}
     *            value
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
    public int compare(OrderedLockable fst, OrderedLockable snd) {
        long fstPhase = fst.lockPriority();
        long sndPhase = snd.lockPriority();
        if (fstPhase < sndPhase) {
            return -1;
        }
        if (fstPhase > sndPhase) {
            return 1;
        }
        return 0;
    }
}