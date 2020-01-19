/*
 * Created on May 25 2005
 */
package de.renew.fa;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * A file filter for finite automaton drawings, which are
 * characterized by the <i>.fa</i> extension and colled
 * <i>Finite Automata Drawing</i>
 *
 * @author Lawrence Cabac
 */
public class FAFileFilter extends SimpleFileFilter {
    public FAFileFilter() {
        this.setExtension("fa");
        this.setDescription("Finite Automata Drawing (*." + this.getExtension()
                            + ")");
    }
}