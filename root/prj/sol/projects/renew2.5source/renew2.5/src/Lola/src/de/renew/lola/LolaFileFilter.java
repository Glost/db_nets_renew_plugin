package de.renew.lola;

import CH.ifa.draw.io.SimpleFileFilter;


public class LolaFileFilter extends SimpleFileFilter {
    public LolaFileFilter() {
        this.setExtension("net");
        this.setDescription("Lola File Format (*.net)");
    }
}