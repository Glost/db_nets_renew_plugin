package de.renew.net.arc;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;

import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.PlaceInstance;

import de.renew.unify.Copier;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import de.renew.util.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;


public class ArcOccurrence extends AbstractOccurrence {
    // This is a complete and immutable variable that is used
    // whenever not time specification is explicitly given.
    static Variable theNullTimeVar = new Variable(new Value(new Double(0)), null);
    PlaceInstance placeInstance;
    VariableMapper mapper;
    Variable tokenVar;
    Variable delayVar;
    Arc arc;

    protected ArcOccurrence(Arc arc, VariableMapper mapper,
                            NetInstance netInstance) {
        super(netInstance.getInstance(arc.transition));
        this.arc = arc;
        this.mapper = mapper;
        placeInstance = netInstance.getInstance(arc.place);
    }

    protected InhibitorArcBinder getInhibitorArcBinder(Variable tokenVar,
                                                       PlaceInstance placeInstance) {
        return new InhibitorArcBinder(tokenVar, placeInstance);
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        // For the moment, arc expressions are always early.
        // We might make them late for output arcs, but this
        // might cause significant confusion. By introducing a
        // new variable and an action statement a late evaluation
        // can always be enforced within the model.
        tokenVar = new Variable(arc.tokenExpr.startEvaluation(mapper,
                                                              searcher.recorder,
                                                              searcher.calcChecker),
                                searcher.recorder);

        if (!arc.isUntimedArc()) {
            delayVar = new Variable(arc.timeExpr.startEvaluation(mapper,
                                                                 searcher.recorder,
                                                                 searcher.calcChecker),
                                    searcher.recorder);
        }


        // Prepare a sequence of binders for this arc.
        List<Binder> binders = new ArrayList<Binder>();

        if (arc.arcType == Arc.out) {
            // We must make sure that the variables associated with
            // an output arc are going to be computed before we declare
            // a transition enabled.
            searcher.calcChecker.addLateVariable(tokenVar, searcher.recorder);
            searcher.calcChecker.addLateVariable(delayVar, searcher.recorder);
        } else if (arc.arcType == Arc.inhibitor) {
            binders.add(getInhibitorArcBinder(tokenVar, placeInstance));
        } else {
            // If the arc expression is invertable, there is a good
            // reason to try to assign values to it.
            if (arc.tokenExpr.isInvertible()) {
                binders.add(new ArcAssignBinder(tokenVar, placeInstance,
                                                arc.isTestArc()));
            }


            // Input arcs and double arcs simply check the
            // existence of tokens during binding.
            if (arc.isTestArc()) {
                binders.add(new TestArcBinder(tokenVar, placeInstance));
            } else if (arc.arcType == Arc.bothOT) {
                binders.add(new InputArcBinder(tokenVar,
                                               new Variable(new Integer(0), null),
                                               placeInstance));
                searcher.calcChecker.addLateVariable(delayVar, searcher.recorder);
            } else {
                binders.add(new InputArcBinder(tokenVar, delayVar, placeInstance));
            }
        }

        return binders;
    }

    private double getDelay() {
        Object timeObj = delayVar.getValue();
        if (timeObj instanceof Value) {
            timeObj = ((Value) timeObj).value;
        }
        return ((Number) timeObj).doubleValue();
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
        Copier copier = variableMapperCopier.getCopier();


        // There will be no undo past this point, so we need
        // not supply a state recorder. However, we must always
        // make a copy, because the current bindings will be
        // completely rolled back. Even when we make a getValue()
        // call to the old variable, that variable might contain
        // a tuple that is taken apart later on.
        Variable copiedToken = (Variable) copier.copy(tokenVar);

        Collection<Executable> coll = new Vector<Executable>();

        // Depending on the type of the arc we must create different
        // executables.
        switch (arc.arcType) {
        case Arc.in:
            coll.add(new InputArcExecutable(placeInstance, getTransition(),
                                            copiedToken.getValue(), getDelay(),
                                            false, arc.getTrace()));
            return coll;
        case Arc.test:
            TestArcExecutable tester = new TestArcExecutable(placeInstance,
                                                             getTransition(),
                                                             copiedToken
                                           .getValue(), false, arc.getTrace());
            return Arrays.asList(new Executable[] { tester, new UntestArcExecutable(tester) });
        case Arc.out:
            // For an output arc, the time might be calculated later.
            Variable copiedDelay = (Variable) copier.copy(delayVar);
            coll.add(new OutputArcExecutable(placeInstance, getTransition(),
                                             copiedToken, copiedDelay,
                                             arc.getTrace()));
            return coll;
        case Arc.both:
            return Arrays.asList(new Executable[] { new InputArcExecutable(placeInstance,
                                                                           getTransition(),
                                                                           copiedToken
                                                                           .getValue(),
                                                                           getDelay(),
                                                                           false,
                                                                           arc
                       .getTrace()), new OutputArcExecutable(placeInstance,
                                                             getTransition(),
                                                             copiedToken, null,
                                                             arc.getTrace()) });
        case Arc.fastBoth:
            coll.add(new InputArcExecutable(placeInstance, getTransition(),
                                            copiedToken.getValue(), getDelay(),
                                            true, arc.getTrace()));
            return coll;
        case Arc.fastTest:
            coll.add(new TestArcExecutable(placeInstance, getTransition(),
                                           copiedToken.getValue(), true,
                                           arc.getTrace()));
            return coll;
        case Arc.inhibitor:
            coll.add(new InhibitorExecutable(placeInstance, getTransition(),
                                             copiedToken.getValue(),
                                             arc.getTrace()));
            return coll;
        case Arc.bothOT:
            // For an output arc, the time might be calculated later.
            copiedDelay = (Variable) copier.copy(delayVar);
            return Arrays.asList(new Executable[] { new InputArcExecutable(placeInstance,
                                                                           getTransition(),
                                                                           copiedToken
                                                                           .getValue(),
                                                                           0,
                                                                           false,
                                                                           arc
                       .getTrace()), new OutputArcExecutable(placeInstance,
                                                             getTransition(),
                                                             copiedToken,
                                                             copiedDelay,
                                                             arc.getTrace()) });
        default:
            throw new RuntimeException("Bad arc type.");
        }
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        return null;
    }
}