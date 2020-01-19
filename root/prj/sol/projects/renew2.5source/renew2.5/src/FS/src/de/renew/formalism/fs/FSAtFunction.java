package de.renew.formalism.fs;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.JavaObject;
import de.uni_hamburg.fs.NoSuchFeatureException;
import de.uni_hamburg.fs.Path;

import de.renew.expression.Function;

import de.renew.unify.Impossible;


public class FSAtFunction implements Function {
    Path path;
    boolean unpacking = true;

    public FSAtFunction(String feat) {
        path = new Path(feat);
    }

    public FSAtFunction(Path path, boolean unpacking) {
        this.path = path;
        this.unpacking = unpacking;
    }

    public Object function(Object param) throws Impossible {
        FeatureStructure fs;
        if (param instanceof FeatureStructure) {
            fs = (FeatureStructure) param;
        } else {
            fs = new FeatureStructure(JavaObject.getJavaType(param));
        }
        try {
            if (unpacking) {
                return fs.unpackingAt(path);
            } else {
                return fs.at(path);
            }
        } catch (NoSuchFeatureException nsf) {
            throw new Impossible(nsf.getMessage());
        }
    }
}