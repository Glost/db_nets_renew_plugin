package de.renew.formalism.efsnet;

import collections.CollectionEnumeration;
import collections.HashedSet;
import collections.UpdatableSet;

import de.uni_hamburg.fs.EquivRelation;
import de.uni_hamburg.fs.FSNode;
import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Node;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.expression.Function;

import de.renew.unify.Impossible;


public class SCutFunction implements Function, EFSNetConstants {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SCutFunction.class);
    public static SCutFunction VALUE_SEMANTICS = new SCutFunction(true);
    public static SCutFunction REFERENCE_SEMANTICS = new SCutFunction(false);
    private boolean valueSem;

    private SCutFunction(boolean valueSem) {
        this.valueSem = valueSem;
    }

    public static SCutFunction instance(boolean valueSem) {
        return valueSem ? VALUE_SEMANTICS : REFERENCE_SEMANTICS;
    }

    public Object function(Object param) throws Impossible {
        if (param instanceof FeatureStructure) {
            return scut((FeatureStructure) param, valueSem);
        } else {
            throw new Impossible("Argument of SCutFunction was not a Feature Structure!");
        }
    }

    public static FeatureStructure scut(FeatureStructure P, boolean valueSem) {
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
        findTokens(P.getRoot(), scut, valueSem, new HashedSet());
        return new FeatureStructure(scut);
    }

    private static void findTokens(Node proc, Node scut, boolean valueSem,
                                   UpdatableSet visited) {
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
                Node token = proc_m.delta(prodToken);
                if (valueSem) {
                    // copy token:
                    token = EquivRelation.deepCopy(token);
                }
                scut_pre.setFeature(prodToken, token);
            }
        }
        CollectionEnumeration consTokens = proc.featureNames();
        while (consTokens.hasMoreElements()) {
            Name consToken = (Name) consTokens.nextElement();
            if (!consToken.equals(FEATm)) {
                findTokens(proc.delta(consToken), scut, valueSem, visited);
            }
        }
    }
}