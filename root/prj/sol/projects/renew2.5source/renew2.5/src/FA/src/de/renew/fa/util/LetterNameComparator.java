/*
 * Created on Oct 4, 2005
 *
 */
package de.renew.fa.util;

import de.renew.fa.model.Letter;

import java.text.Collator;

import java.util.Comparator;


/**
 * @author cabac
 *
 */
public class LetterNameComparator implements Comparator<Letter> {

    /**
     *
     */
    public LetterNameComparator() {
        super();

    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Letter o1, Letter o2) {
        String s1 = o1.getName();
        String s2 = o2.getName();

        return Collator.getInstance().compare(s1, s2);
    }
}