package de.renew.formalism.efsnet;

import collections.CollectionEnumeration;
import collections.HashedSet;
import collections.UpdatableSet;

import de.uni_hamburg.fs.FSNode;
import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Node;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.expression.Function;

import de.renew.unify.Impossible;


public class ProcessRuleFunction implements Function, EFSNetConstants {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ProcessRuleFunction.class);
    private boolean valueSem;
    private FeatureStructure processRule;

    public ProcessRuleFunction(boolean valueSem, FeatureStructure processRule) {
        this.valueSem = valueSem;
        this.processRule = processRule;
    }

    public Object function(Object param) throws Impossible {
        if (param instanceof FeatureStructure) {
            scut((FeatureStructure) param);
            return applyRule(valueSem, (FeatureStructure) param, processRule);
        } else {
            throw new Impossible("Argument of ProcessRuleFunction was not a Feature Structure!");
        }
    }

    public static FeatureStructure applyRule(boolean valueSem,
                                             FeatureStructure proc,
                                             FeatureStructure rule)
            throws Impossible {
        FeatureStructure nextProc = null;
        try {
            nextProc = scut(proc).unify(rule); // may fail
        } catch (UnificationFailure uff) {
            throw new Impossible();
        }

        // the following should not fail:
        try {
            if (valueSem) {
                nextProc = nextProc.unify(ValueMarkingFunction.valmark(nextProc
                                                                       .at(PATHpost)),
                                          PATHpostc);
            } else {
                nextProc = nextProc.equate(PATHpost, PATHpostc);
            }
            return nextProc.at(PATHproc);
        } catch (UnificationFailure uff) {
            logger.error("Unexpected unification failure in ProcessRuleFunction.applyRule().");
            return null;
        }
    }

    public static FeatureStructure scut(FeatureStructure P) {
        String netSpace = SingleEFSNetCompiler.getNamespace(P.getType());
        Node scut = null;
        try {
            scut = new FSNode(netSpace + "PEff");
            scut.setFeature(FEATpre, new FSNode(netSpace + "M"));
        } catch (UnificationFailure uff) {
            logger.error("Type " + netSpace + "PEff or M not found!");
            return null;
        }
        scut.setFeature(FEATproc, P.getRoot());
        findTokens(P.getRoot(), scut, new HashedSet());
        return new FeatureStructure(scut);
    }

    private static void findTokens(Node proc, Node scut, UpdatableSet visited) {
        // look for proc nodes which contain a feature m:s
        // but do not contain the feature s (s is not consumed).
        // Then, this proc node is assigned as a value for the
        // feature s in scut.
        if (visited.includes(proc)) {
            return;
        }
        visited.include(proc);
        Node proc_m = proc.delta(FEATm);
        Node scut_pre = scut.delta(FEATpre);
        CollectionEnumeration prodTokens = proc_m.featureNames();

        // get all produced tokens:
        while (prodTokens.hasMoreElements()) {
            Name prodToken = (Name) prodTokens.nextElement();
            if (!proc.hasFeature(prodToken)) {
                // token is not cosumed
                if (scut.hasFeature(prodToken)) {
                    // has been assigned before!
                    throw new RuntimeException("More than one token on place "
                                               + prodToken + "!");
                }
                scut.setFeature(prodToken, proc);
                scut_pre.setFeature(prodToken, proc_m.delta(prodToken));
            }
        }
        CollectionEnumeration consTokens = proc.featureNames();
        while (consTokens.hasMoreElements()) {
            Name consToken = (Name) consTokens.nextElement();
            if (!consToken.equals(FEATm)) {
                findTokens(proc.delta(consToken), scut, visited);
            }
        }
    }
}