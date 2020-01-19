/*
 * Created on Oct 4, 2005
 *
 */
package de.renew.fa.util;

import de.renew.fa.model.State;

import java.text.Collator;

import java.util.Comparator;


/**
 * @author cabac
 *
 */
public class StateNameComparator implements Comparator<State> {

    /**
     *
     */
    public StateNameComparator() {
        super();

    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(State o1, State o2) {
        String s1 = o1.getName();
        String s2 = o2.getName();

        return Collator.getInstance().compare(s1, s2);
    }
}