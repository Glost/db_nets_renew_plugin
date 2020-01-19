package de.renew.unify;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


// On the fly:
//   Check that no calculated unknown is bound.
//   Check that no calculated unknown is registered twice.
//   Check that no two calculated unknowns depend on each other.
// During the consistency check:
//   Check that all early variables have become bound.
//   Check that all late variables are either bound or calculated.
final public class CalculationChecker {
    private Set<Variable> lateVariables;
    private Set<Variable> earlyVariables;

    public CalculationChecker() {
        lateVariables = new HashSet<Variable>();
        earlyVariables = new HashSet<Variable>();
    }

    public void reset() {
        lateVariables.clear();
        earlyVariables.clear();
    }

    public void addEarlyVariable(final Variable var, StateRecorder recorder)
            throws Impossible { //NOTICEthrows
        if (!earlyVariables.contains(var)) {
            if (recorder != null) {
                recorder.record(new StateRestorer() {
                        public void restore() {
                            earlyVariables.remove(var);
                        }
                    });
            }

            earlyVariables.add(var);
        }
    }

    public void addCalculated(Class<?> targetType, Object target,
                              Object source, StateRecorder recorder)
            throws Impossible {
        Unify.unify(target, new Calculator(targetType, source, recorder),
                    recorder);
    }

    public void addLateVariable(final Variable var, StateRecorder recorder)
            throws Impossible { //NOTICEthrows
        if (!lateVariables.contains(var)) {
            if (recorder != null) {
                recorder.record(new StateRestorer() {
                        public void restore() {
                            lateVariables.remove(var);
                        }
                    });
            }

            lateVariables.add(var);
        }
    }

    private boolean checkLateVariables() {
        // All late variables must be bound or calculated.
        Iterator<Variable> iterator = lateVariables.iterator();
        while (iterator.hasNext()) {
            Variable variable = iterator.next();
            if (!variable.isComplete()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkEarlyVariables() {
        // All early variables must be bound.
        Iterator<Variable> enumeration = earlyVariables.iterator();
        while (enumeration.hasNext()) {
            Variable variable = enumeration.next();
            if (!variable.isBound()) {
                return false;
            }
        }
        return true;
    }

    public boolean isConsistent() {
        return checkLateVariables() && checkEarlyVariables();
    }
}