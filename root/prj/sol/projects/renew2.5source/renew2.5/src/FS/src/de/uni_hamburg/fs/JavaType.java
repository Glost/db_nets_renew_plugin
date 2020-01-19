package de.uni_hamburg.fs;

public interface JavaType extends Type {

    /** Returns the java object wrapped by this Type. */
    public Object getJavaObject();
}