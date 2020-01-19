package de.renew.engine.simulator;

import de.renew.engine.searcher.EarlyExecutable;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.LateExecutable;

import java.util.Comparator;


/**
 * This comparator can be used to order {@link Executable}
 * instances in a {@link java.util.SortedSet} or similar classes
 * by their phase.
 *
 * @see EarlyExecutable#phase()
 * @see LateExecutable#phase()
 * @author Olaf Kummer
 * @author Michael Duvigneau (added documentation)
 **/
class PhaseComparator implements Comparator<Executable> {

    /**
     * {@inheritDoc}
     * @param fst  the first Executable to compare
     * @param snd  the second Executable to compare
     * @return -1 if <code>fst</code> should be executed first (has a lower
     *            {@link Executable#phase()} value. <br />
     *         +1 if <code>snd</code> should be executed first.
     *         0  if both share the same {@link Executable#phase()}
     *            value
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    **/
    public int compare(Executable fst, Executable snd) {
        int fstPhase = fst.phase();
        int sndPhase = snd.phase();
        if (fstPhase < sndPhase) {
            return -1;
        }
        if (fstPhase > sndPhase) {
            return 1;
        }
        return 0;
    }
}