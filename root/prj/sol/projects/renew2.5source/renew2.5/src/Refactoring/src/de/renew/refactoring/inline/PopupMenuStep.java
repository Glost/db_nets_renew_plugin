package de.renew.refactoring.inline;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;


/**
 * Inline step that provides a popup menu. The menu appears upon instantiation.
 *
 * @see JPopupMenu
 * @author 2mfriedr
 */
public class PopupMenuStep<T> extends InlineStepWithListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(PopupMenuStep.class);
    private JPopupMenu _menu;
    private Map<JMenuItem, T> _menuItems = new HashMap<JMenuItem, T>();

    /**
     * Creates a PopupMenuStep.
     *
     * @param container the container in which the text field will appear
     * @param origin the point at which the popup menu will appear
     * @param entries the menu entries
     */
    public PopupMenuStep(final Container container, final Point origin,
                         final List<T> entries) {
        _menu = new JPopupMenu();
        addEntries(entries);
        _menu.addPopupMenuListener(popupMenuListener());
        _menu.show(container, origin.x, origin.y);
    }

    /**
     * Adds entries to the menu and calls {@link #titleForEntry(Object)} to
     * determine their title and {@link #actionListenerForEntry(Object)} to
     * determine a custom action listener.
     *
     * @param entries the menu entries
     */
    private void addEntries(final List<T> entries) {
        for (T entry : entries) {
            JMenuItem item = new JMenuItem(titleForEntry(entry));
            ActionListener customListener = actionListenerForEntry(entry);
            item.addActionListener(makeOuterListener(customListener));

            _menu.add(item);
            _menuItems.put(item, entry);
        }
    }

    /**
     * Creates an outer action listener that calls the inner listener's
     * {@link ActionListener#actionPerformed(ActionEvent)} and informs the
     * listeners that the step has finished.
     *
     * @param inner the custom listener
     * @return the outer listener
     */
    private ActionListener makeOuterListener(final ActionListener inner) {
        return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (inner != null) {
                        inner.actionPerformed(e);
                    }
                    informListenersFinished();
                }
            };
    }

    /**
     * Returns the entry that corresponds to a menu item. This is useful within
     * action events that provide the menu item but not the entry.
     *
     * @param item the menu item
     * @return the entry
     */
    public T getEntryForMenuItem(JMenuItem item) {
        return _menuItems.get(item);
    }

    /**
     * Override point for subclasses.
     * This method is called to determine the action listener for the entry's
     * menu item.
     *
     * @param entry the entry
     * @return {@code null}
     */
    public ActionListener actionListenerForEntry(T entry) {
        return null;
    }

    /**
     * Override point for subclasses.
     * This method is called to determine the title for the entry's menu item.
     *
     * @param entry the entry
     * @return {@code entry.toString()}
     */
    public String titleForEntry(T entry) {
        return entry.toString();
    }

    private PopupMenuListener popupMenuListener() {
        return new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    informListenersCancelled();
                }
            };
    }
}