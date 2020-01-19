package CH.ifa.draw.util;

import java.awt.EventQueue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A <code>Command</code> that can update its associated
 * <code>UpdatableCommandMenuItem</code>s.
 * <p>
 * </p>
 * Created: Mon Jun 20  2005
 *
 * @author Michael Duvigneau
 **/
public abstract class UpdatingCommand extends Command {

    /**
     * The set of {@link UpdatableCommandMenuItem} instances that are
     * listening for updates.
     **/
    protected Set<UpdatableCommandMenuItem> listeners = new HashSet<UpdatableCommandMenuItem>();

    /**
     * Creates a new <code>UpdatingCommand</code> with the given name.
     * The name may change later.
     *
     * @param name  the initial name of the command.
     **/
    public UpdatingCommand(String name) {
        super(name);
    }

    /**
     * Changes the command name.
     *
     * @param name  the new name of the command.
     **/
    public synchronized void setName(final String name) {
        this.fName = name;
        fireUpdateText();
    }

    /**
     * Updates the associated <code>UpdatableCommandMenuItem</code>s
     * asynchronously, but in synchronization with the AWT event queue.
     **/
    protected synchronized void fireUpdateText() {
        final String newName = name();
        for (Iterator<UpdatableCommandMenuItem> listenerIterator = listeners
                 .iterator(); listenerIterator.hasNext();) {
            final UpdatableCommandMenuItem item = listenerIterator.next();
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        item.updateName(newName);
                    }
                });
        }
    }

    /**
     * Registers the given menu item as listener for updates.
     * This method is intended to be called only by the registring
     * <code>UpdatableCommandMenuItem</code> internally.
     *
     * @param item  the <code>UpdatableCommandMenuItem</code> to register as
     *              update listener.
     **/
    protected synchronized void addMenuItem(UpdatableCommandMenuItem item) {
        listeners.add(item);
    }

    /**
     * Deregisters the given menu item as listener for updates.
     * This method is intended to be called only by the registered
     * <code>UpdatableCommandMenuItem</code> internally.
     *
     * @param item  the <code>UpdatableCommandMenuItem</code> to deregister as
     *              update listener.
     **/
    protected synchronized void removeMenuItem(UpdatableCommandMenuItem item) {
        listeners.remove(item);
    }
}