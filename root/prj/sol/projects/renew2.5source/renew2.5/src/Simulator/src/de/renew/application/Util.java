package de.renew.application;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.simulator.ExecuteFinder;
import de.renew.engine.simulator.SimulatorHelper;

import de.renew.net.NetInstance;
import de.renew.net.NetInstanceList;
import de.renew.net.Transition;
import de.renew.net.TransitionInstance;

import java.util.Collection;


/**
 * Utility to access transition instances. Transitions marked "manual"
 * can be fired by providing the net instance id and the transition
 * name - usually given by a name annotation.
 * (Code has been taken from
 * <code>de.renew.agent.interactiontest.NetTestFramework</code>)
 *
 * @author cabac,stosch
 */
public class Util {
    public Util() {
    }

    /**
     * Fires the given transition if found and fireable. Returns whether
     * transition was found.
     *
     * @param instance
     *            {@link NetInstance} where the given transition is located
     * @param name
     *            the {@link Transition}'s name. Must exactly match the correct
     *            name.
     * @return <code>true</code> if found, else <code>false</code>
     */
    public boolean fireTransition(NetInstance instance, String name) {
        boolean found = false;
        Transition transition = findTransition(instance, name);
        if (transition != null) {
            found = true;
            TransitionInstance transitionInstance = instance.getInstance(transition);
            ExecuteFinder finder = new ExecuteFinder();
            Searcher searcher = new Searcher();
            SimulatorHelper.searchOnce(searcher, finder, transitionInstance,
                                       transitionInstance);
            SimulatorPlugin sp = SimulatorPlugin.getCurrent();
            StepIdentifier step = sp.getCurrentEnvironment().getSimulator()
                                    .nextStepIdentifier();

            if (finder.isCompleted()) {
                finder.execute(step, true);
            }
        }
        return found;
    }

    /**
     * Returns a transition with the given name in the given NetInstance.
     *
     * @param instance
     *            {@link NetInstance} where the given transition is located
     * @param name
     *            the {@link Transition}'s name. Must exactly match the correct
     *            name.
     * @return transition if found, else <code>null</code>
     */
    private Transition findTransition(NetInstance instance, String name) {
        Collection<Transition> transitions = instance.getNet().transitions();
        Transition selection = null;
        for (Transition transition : transitions) {
            if (transition.getName().equals((name))) {
                selection = transition;
                break;
            }
        }
        return selection;
    }

    public NetInstance findInstance(String netInstanceName) {
        return NetInstanceList.getNetInstance(netInstanceName);

    }
}