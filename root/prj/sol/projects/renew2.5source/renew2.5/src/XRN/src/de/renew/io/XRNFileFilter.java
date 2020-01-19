package de.renew.io;

import CH.ifa.draw.io.SimpleFileFilter;


public class XRNFileFilter extends SimpleFileFilter {
    public XRNFileFilter() {
        this.setExtension("xrn");
        setDescription("Renew XML File");
    }
}