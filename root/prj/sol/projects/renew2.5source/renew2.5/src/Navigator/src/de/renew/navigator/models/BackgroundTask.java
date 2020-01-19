package de.renew.navigator.models;

import java.util.Observable;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-08
 */
public class BackgroundTask extends Observable {

    /**
     * Name of the background task.
     */
    protected String name;

    /**
     * The current value of the background task.
     */
    protected float current;

    /**
     * The Action to execute when the task gets canceled.
     */
    protected Runnable cancelAction;

    /**
     * Constructor.
     */
    public BackgroundTask(String name) {
        this.name = name;
        this.current = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setChanged();
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
        setChanged();
    }

    /**
     * @return if this task is in indeterminate state.
     */
    public boolean isIndeterminate() {
        return current == -1;
    }

    /**
     * @return if this task can be cancelled.
     */
    public boolean isCancelable() {
        return this.cancelAction != null;
    }

    public void setCancelAction(Runnable cancelAction) {
        this.cancelAction = cancelAction;
        setChanged();
    }

    /**
     * Cancels the background task.
     */
    public void cancel() {
        if (this.cancelAction != null) {
            this.cancelAction.run();
        }
    }

    public void increaseCurrent(float amount) {
        current += amount;
        setChanged();
    }
}