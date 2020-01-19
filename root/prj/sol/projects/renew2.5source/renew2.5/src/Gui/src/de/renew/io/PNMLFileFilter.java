/*
 * Created on Apr 13, 2003
 */
package de.renew.io;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author Lawrence Cabac
 */
public class PNMLFileFilter extends SimpleFileFilter {
    public PNMLFileFilter() {
        this.setExtension("pnml");
        setDescription("Renew PNML File");
    }
}