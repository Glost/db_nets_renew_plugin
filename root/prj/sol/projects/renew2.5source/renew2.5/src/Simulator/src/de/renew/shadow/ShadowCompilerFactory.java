package de.renew.shadow;

import java.io.Serializable;


/**
 * An interface for classes that create ShadowCompilers.
 * <p>
 * A shadow compiler factory must be serializable in order
 * to be able to serialize the shadow net system.
 * </p>
 * @author Joern Schumacher
 **/
public interface ShadowCompilerFactory extends Serializable {
    public ShadowCompiler createCompiler();
}