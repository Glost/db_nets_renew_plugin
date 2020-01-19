package de.renew.net;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.VariableMapper;

import de.renew.unify.Impossible;

import java.io.ObjectStreamException;

import java.util.Collections;


/**
 * The <code>ManualInscription</code> tags a transition as non-spontaneous.
 * The class uses the singleton design pattern.
 * <p>
 * The <code>ManualInscription</code> has no occurrences because it has no
 * effect when a transition fires.
 * The only effect of this inscription is that the transition property
 * {@link de.renew.net.Transition#isSpontaneous} is <code>false</code>
 * whenever the <code>ManualInscription</code> singleton is detected among
 * the transition's inscriptions
 * </p>
 *
 * @author Olaf Kummer
 * @author Michael Duvigneau
 **/
public class ManualInscription implements TransitionInscription {

    /**
     * Stores the singleton instance of this class.
     **/
    private static ManualInscription singleton;

    /**
     * This class cannot be instantiated except for the the singleton
     * instance which can be obtained by a call to {@link #getInstance}.
     **/
    private ManualInscription() {
    }

    /**
     * Obtains a reference to the singleton instance of this class.
     * The instance is created by the first call of this method.
     *
     * @return  the one and only <code>ManualInscription</code> instance.
     **/
    public static synchronized ManualInscription getInstance() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (singleton == null) {
            singleton = new ManualInscription();
        }

        return singleton;
    }

    /**
     * The <code>ManualInscription</code> has no effect when a transition
     * fires, therefore the returned set of occurrences is empty.
     *
     * @param mapper          {@inheritDoc}
     * @param netInstance     {@inheritDoc}
     * @param searcher        {@inheritDoc}
     * @return                an empty set.
     * @exception Impossible  never.
     **/
    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher)
            throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return Collections.emptySet();
    }

    /**
     * This method is called automatically by the Java deserialization
     * mechanism, and it ensures the singleton property of this class.
     *
     * @return  the singleton instance of <code>ManualInscription</code>.
     * @exception ObjectStreamException  if an error occurs
     **/
    private Object readResolve() throws ObjectStreamException {
        return getInstance();
    }
}