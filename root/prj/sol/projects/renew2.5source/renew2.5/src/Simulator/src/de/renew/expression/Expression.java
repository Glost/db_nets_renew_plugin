package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;

import java.io.Serializable;


/** An expression might be:
  *   - an assignment
  *   - a unification (invertible)
  *   - a method
  *   - a constructor
  *   - an operator
  *   - a field read
  *   - a field write (only late!)
  *   - an array read
  *   - an array write (only late!)
  *   - a constant
  *   - a tuple (possibly invertible)
  *   - a local variable (invertible, l-value)
  */
public interface Expression extends Serializable {

    /** Can the variable provided by startEvaluation be unified
      * with other values to propagate information to the arguments?
      */
    boolean isInvertible();

    /** Start the evaluation. The resulting object might be an unknown.
      * It will usually be stored in a variable.
      *
      * If recorder is null, the calculations cannot be undone.
      * If checker is null, no informations on desired bindings
      * will be provided. Usually both recorder and checker will
      * be null during late evaluations.
      */
    Object startEvaluation(VariableMapper mapper, StateRecorder recorder,
                           CalculationChecker checker)
            throws Impossible;

    /** Inform the calculation checker how an evaluation of this
      * expression would proceed.
      */
    Object registerCalculation(VariableMapper mapper, StateRecorder recorder,
                               CalculationChecker checker)
            throws Impossible;

    /** Return the type of the value that this expression will compute,
      * or Types. UNTYPED if no type information can be given.
      */
    Class<?> getType();
}