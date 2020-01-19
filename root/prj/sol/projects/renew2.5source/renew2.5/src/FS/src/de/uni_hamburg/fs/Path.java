package de.uni_hamburg.fs;

import collections.ArrayEnumeration;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


public class Path implements java.io.Serializable {
    public static final Path EPSILON = new Path(new Name[0]);
    private Name[] feature;

    /** Constructs a Path from a String.
     *  A path is a sequence of Features (meant to be starting at the root node).
     *  The syntax of a path is feature_1:feature_2:...feature_n.
     *  The empty String returns the empty Path (length zero).
     */
    public Path(String path) {
        StringTokenizer featenumeration = new StringTokenizer(path, ":");
        feature = new Name[featenumeration.countTokens()];
        for (int i = 0; featenumeration.hasMoreTokens(); ++i) {
            feature[i] = new Name(featenumeration.nextToken());
        }
    }

    public Path(Name feature) {
        this.feature = new Name[] { feature };
    }

    public Path(Name[] feature) {
        this.feature = feature;
    }

    public static Path nth(int n) {
        Name[] feature = new Name[n + 1];
        for (int i = 0; i < n; ++i) {
            feature[i] = ListType.TAIL;
        }
        feature[n] = ListType.HEAD;
        return new Path(feature);
    }

    public Enumeration<?> features() {
        return new ArrayEnumeration(feature);
    }

    public int length() {
        return feature.length;
    }

    public boolean isEmpty() {
        return feature.length == 0;
    }

    public Name at(int i) {
        return feature[i]; // may throw ArrayIndexOutOfBoundsException
    }

    public Path append(Name feat) {
        int size = feature.length;
        Name[] appfeature = new Name[size + 1];
        System.arraycopy(feature, 0, appfeature, 0, size);
        appfeature[size] = feat;
        return new Path(appfeature);
    }

    public Path append(String feat) {
        return append(new Name(feat));
    }

    public Path prepend(Name feat) {
        int size = feature.length;
        Name[] prefeature = new Name[size + 1];
        System.arraycopy(feature, 0, prefeature, 1, size);
        prefeature[0] = feat;
        return new Path(prefeature);
    }

    public Path prepend(String feat) {
        return prepend(new Name(feat));
    }

    private Path butOne(int shift) {
        int bosize = feature.length - 1;
        if (bosize < 0) {
            throw new NoSuchElementException();
        }
        Name[] bofeature = new Name[bosize];
        System.arraycopy(feature, shift, bofeature, 0, bosize);
        return new Path(bofeature);
    }

    public Path butLast() {
        return butOne(0);
    }

    public Name last() {
        return at(feature.length - 1);
    }

    public Path butFirst() {
        return butOne(1);
    }

    public Name first() {
        return at(0);
    }

    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuffer out = new StringBuffer(feature[0].toString());
        for (int i = 1; i < feature.length; ++i) {
            out.append(':').append(feature[i]);
        }
        return out.toString();
    }
}