/*
 * Created on September 15. 2005
 */
package de.renew.fa;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author Lawrence Cabac
 */
public class JflapFileFilter extends SimpleFileFilter {
    public JflapFileFilter() {
        this.setExtension("jff");
        this.setDescription("JFLAP File (v4) (*." + this.getExtension() + ")");
    }
}