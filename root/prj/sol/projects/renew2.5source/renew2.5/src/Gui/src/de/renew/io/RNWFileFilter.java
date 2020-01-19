/*
 * Created on Apr 13, 2003
 */
package de.renew.io;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author Lawrence Cabac
 */
public class RNWFileFilter extends SimpleFileFilter {
    public RNWFileFilter() {
        this.setExtension("rnw");
        setDescription("Net Drawing (*.rnw)");
    }
}