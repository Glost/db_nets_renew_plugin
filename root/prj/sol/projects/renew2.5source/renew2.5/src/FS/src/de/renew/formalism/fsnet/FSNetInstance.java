package de.renew.formalism.fsnet;

import de.renew.application.SimulatorPlugin;

import de.renew.net.Net;
import de.renew.net.NetInstanceImpl;
import de.renew.net.NetNotFoundException;

import de.renew.unify.Impossible;


public class FSNetInstance extends NetInstanceImpl {
    public FSNetInstance() {
        super();
    }

    public String getOf() {
        return getNet().getName();
    }

    public void setOf(String netName) {
        try {
            Net net = Net.forName(netName);
            initNet(net, true);
            createConfirmation(SimulatorPlugin.getCurrent()
                                              .getCurrentEnvironment()
                                              .getSimulator()
                                              .currentStepIdentifier());
        } catch (NetNotFoundException e) {
            throw new RuntimeException("Could not make an FSNet instance: " + e);
        } catch (Impossible e) {
            throw new RuntimeException("Could not make an FSNet instance.");
        }
    }
}