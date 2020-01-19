package de.renew.call;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;

import de.renew.unify.Tuple;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


class SynchronisationOccurrence extends AbstractOccurrence {
    SynchronisationRequest synchronisation;

    SynchronisationOccurrence(SynchronisationRequest synchronisation) {
        super(null);
        this.synchronisation = synchronisation;
    }

    /**
     * As synchronisation occurrences do not relate to binders,
     * an empty enumeration is returned.
     */
    public Collection<Binder> makeBinders(Searcher searcher) {
        return Collections.emptySet();
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        // Remember the current settings of the parameters.
        // The parameters might not yet be complete due to
        // method calls during the action phase, but the
        // searcher decided that the tuple will be complete
        // before the executable gains control.
        Tuple copiedParameters = (Tuple) copier.getCopier()
                                               .copy(synchronisation.parameters);

        return Arrays.asList(new Executable[] { new NotifyExecutable(synchronisation,
                                                                     copiedParameters), new BlockExecutable(synchronisation) });
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        return null;
    }
}