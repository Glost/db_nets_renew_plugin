package de.renew.io;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author 6hauster
 */
public class WoflanFileFilter extends SimpleFileFilter {
    public WoflanFileFilter() {
        this.setExtension("tpn");
        this.setDescription("Woflan");
    }
}