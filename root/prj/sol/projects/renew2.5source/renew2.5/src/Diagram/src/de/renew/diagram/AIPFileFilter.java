/*
 * Created on Apr 13, 2003
 */
package de.renew.diagram;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author Lawrence Cabac
 */
public class AIPFileFilter extends SimpleFileFilter {
    public AIPFileFilter() {
        this.setExtension("aip");
        this.setDescription("Agent Interaction Protocol (*.aip)");
    }
}