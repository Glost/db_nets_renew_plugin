package de.renew.formalism.fs;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.JavaObject;
import de.uni_hamburg.fs.Path;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.unify.Impossible;
import de.renew.unify.Notifiable;
import de.renew.unify.StateRecorder;
import de.renew.unify.StateRestorer;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.util.Vector;


class FSUnifier {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FSUnifier.class);
    Variable result;
    private Vector<Variable> variables = new Vector<Variable>();
    private FeatureStructure current;
    private int open = 0;

    //  private boolean resultSet=false;
    //private boolean debug=false;
    //  FSUnifier(FeatureStructure template, Vector paths, StateRecorder recorder,
    //	    boolean debug) throws Impossible {
    //      this(template,paths,recorder);
    //      this.debug=debug;
    //  }
    FSUnifier(FeatureStructure template, Vector<Path> paths,
              StateRecorder recorder) throws Impossible {
        result = new Variable();
        current = template;

        // Create listeners.
        for (int i = 0; i < paths.size(); i++) {
            Variable variable = new Variable();
            variables.addElement(variable);
            setupListener(variable, paths.elementAt(i), recorder);
            open++;
        }
        setupResultListener(paths, recorder);
    }

    Variable getVariable(int i) {
        return variables.elementAt(i);
    }

    private void setupListener(final Variable variable, final Path path,
                               StateRecorder recorder)
            throws Impossible {
        variable.addListener(new Notifiable() {
                public void boundNotify(StateRecorder irecorder)
                        throws Impossible {
                    //  	if (resultSet) {
                    //  	    return;
                    //  	}
                    Object val = variable.getValue();
                    final FeatureStructure oldCurrent = current;
                    if (irecorder != null) {
                        irecorder.record(new StateRestorer() {
                                public void restore() {
                                    current = oldCurrent;
                                    open++;
                                }
                            });
                    }
                    open--;
                    FeatureStructure fs;
                    if (val instanceof FeatureStructure) {
                        fs = (FeatureStructure) val;
                    } else {
                        fs = new FeatureStructure(JavaObject.getJavaType(val));
                    }


                    //if (debug)
                    //	    logger.debug("Trying to unify "+fs+" into "+current+" at "+path);
                    try {
                        current = current.unify(fs, path);
                    } catch (UnificationFailure uff) {
                        // logger.debug(" failed!");
                        throw new Impossible();
                    }


                    // logger.debug("succeeded with result "+current);
                    if (open == 0) {
                        //	logger.debug("all expressions evaluated:"+current);
                        Unify.unify(result, current, irecorder);
                    }
                }
            }, recorder);
    }

    private void setupResultListener(final Vector<Path> paths,
                                     StateRecorder recorder)
            throws Impossible {
        result.addListener(new Notifiable() {
                public void boundNotify(StateRecorder irecorder)
                        throws Impossible {
                    Object val = result.getValue();
                    if (!(val instanceof FeatureStructure)) {
                        throw new Impossible();
                    }
                    FeatureStructure resultfs = (FeatureStructure) val;
                    if (!current.subsumes(resultfs)) {
                        // they can never become equal!
                        logger.debug("Current: " + current + "resultfs:"
                                     + resultfs);
                        throw new Impossible();
                    }


                    //  	  final FeatureStructure oldCurrent=current;
                    //  	  irecorder.record(new StateRestorer() {
                    //  	      public void restore() {
                    //  		  current=oldCurrent;
                    //  		  resultSet=false;
                    //  	      }
                    //  	  });
                    //  	  resultSet=true;
                    //  	  try {
                    //  	      current=((FeatureStructure)val).unify(current);
                    //  	  } catch (UnificationFailure uff) {
                    //  	      throw new Impossible();
                    //  	  }
                    for (int i = 0; i < paths.size(); i++) {
                        Variable variable = variables.elementAt(i);
                        if (!Unify.isBound(variable)) {
                            Path path = paths.elementAt(i);


                            //		  Unify.unify(variable,current.unpackingAt(path),irecorder);
                            Unify.unify(variable, resultfs.unpackingAt(path),
                                        irecorder);
                        }
                    }
                }
            }, recorder);
    }
}