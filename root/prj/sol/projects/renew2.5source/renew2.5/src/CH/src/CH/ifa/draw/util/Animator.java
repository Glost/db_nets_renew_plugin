/*
 * @(#)Animator.java 5.1
 *
 */
package CH.ifa.draw.util;

import CH.ifa.draw.framework.DrawingView;

import java.awt.EventQueue;

import java.lang.reflect.InvocationTargetException;


public class Animator implements Runnable {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(Animator.class);
    private static final int MAX_DELAY = 1000 / 16;
    private static final int MIN_DELAY = MAX_DELAY / 4;
    private DrawingView fView;
    private Animatable fAnimatable;
    private boolean fIsRunning;

    public Animator(Animatable animatable, DrawingView view) {
        //super("Animator");
        //setPriority(MIN_PRIORITY);
        fView = view;
        fAnimatable = animatable;
    }

    //Belonged to thread implementaion moved code tun run() method
    /*
    public void start() {
        fIsRunning = true;
        fAnimatable.animationStart();
        super.start();

    }*/
    public void end() {
        fAnimatable.animationStop();
        fIsRunning = false;
    }

    public void run() {
        fIsRunning = true;
        fAnimatable.animationStart();
        Runnable nextStep = new Runnable() {
            public void run() {
                fAnimatable.animationStep();
                fView.checkDamage();
            }
        };
        while (fIsRunning) {
            long tm = System.currentTimeMillis();
            try {
                EventQueue.invokeAndWait(nextStep);
            } catch (InterruptedException e) {
                break;
            } catch (InvocationTargetException e) {
                logger.warn(e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(Animator.class.getSimpleName() + ": " + e);
                }
            }

            // Delay for a while
            try {
                tm += MAX_DELAY;
                Thread.sleep(Math.max(MIN_DELAY, tm
                                      - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}