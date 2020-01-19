package de.renew.navigator;

import de.renew.navigator.models.NavigatorFileTree;

import java.util.Observable;
import java.util.Observer;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-25
 */
abstract class NavigatorController implements Observer {
    protected final NavigatorPlugin plugin;
    protected final NavigatorFileTree model;

    /**
     * @param plugin the plugin containing the controller
     */
    public NavigatorController(final NavigatorPlugin plugin) {
        this.plugin = plugin;
        this.model = plugin.getModel();
    }

    /**
     * Gets executed each time the model changes.
     *
     * @param target Event target.
     */
    protected void onModelChanged(Object target) {
    }

    @Override
    public void update(Observable observable, Object o) {
        // Do not execute events if executor was the object itself
        if (o != null && o.equals(this)) {
            return;
        }

        if (observable == model) {
            onModelChanged(o);
            return;
        }

        throw new RuntimeException(String.format("An unsuspected observable: %s",
                                                 observable.getClass()));
    }
}