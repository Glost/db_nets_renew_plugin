/*
 * Created on Apr 13, 2003
 */
package de.renew.io;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * @author Lawrence Cabac
 */
public class SimulationStateFileFilter extends SimpleFileFilter {
    public SimulationStateFileFilter() {
        this.setExtension("rst");
        this.setDescription("Renew Simulation State");
    }
}