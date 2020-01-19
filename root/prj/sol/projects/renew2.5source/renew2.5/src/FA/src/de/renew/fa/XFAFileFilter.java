/*
 * Created on September 15. 2005
 */
package de.renew.fa;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author Lawrence Cabac
 */
public class XFAFileFilter extends SimpleFileFilter {
    public XFAFileFilter() {
        this.setExtension("xfa");
        this.setDescription("Finite Automata Text Export/Import (*."
                            + this.getExtension() + ")");
    }
}