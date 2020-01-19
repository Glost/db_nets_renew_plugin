/*
 * Created on Apr 13, 2003
 */
package de.renew.io;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author Lawrence Cabac
 */
public class PSFileFilter extends SimpleFileFilter {
    public PSFileFilter() {
        this.setExtension("ps");
        this.setDescription("Post Script File");
    }
}