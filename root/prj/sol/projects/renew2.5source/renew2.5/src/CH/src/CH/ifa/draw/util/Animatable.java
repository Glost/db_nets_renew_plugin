/*
 * @(#)Animatable.java 5.1
 *
 */
package CH.ifa.draw.util;



/**
 * Animatable defines a simple animation interface
 */
public interface Animatable {

    /**
     * Initialize the animation.
     */
    public void animationStart();

    /**
     * Perform a step of the animation.
     */
    public void animationStep();

    /**
     * Terminate the animation.
     */
    public void animationStop();
}