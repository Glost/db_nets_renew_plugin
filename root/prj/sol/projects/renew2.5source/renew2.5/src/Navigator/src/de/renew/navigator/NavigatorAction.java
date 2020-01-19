package de.renew.navigator;

import CH.ifa.draw.util.Iconkit;

import java.awt.Image;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


/**
 * Basic class for all actions that are related to the navigator.
 *
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-26
 */
public abstract class NavigatorAction extends AbstractAction {

    /**
     * Action constructor.
     *
     * @param tooltip text to display when hovering
     * @param iconIdentifier name of the icon to use
     * @param keyStroke key stroke of the action that triggers
     */
    public NavigatorAction(String tooltip, String iconIdentifier,
                           KeyStroke keyStroke) {
        super(null, loadIcon(iconIdentifier));
        putValue("ShortDescription", tooltip);

        if (keyStroke != null) {
            putValue(AbstractAction.ACCELERATOR_KEY, keyStroke);
        }
    }

    /**
     * Loads an icon with Iconkit.
     *
     * @param iconIdentifier name of the icon to load
     * @return scaled icon instance
     */
    private static ImageIcon loadIcon(String iconIdentifier) {
        final Image icon = Iconkit.instance().loadImage(iconIdentifier);
        final Image scaled = icon.getScaledInstance(-1, 16, Image.SCALE_SMOOTH);

        return new ImageIcon(scaled);
    }

    /**
     * @return a human readable name for the action.
     */
    public String getActionName() {
        String simpleName = getClass().getSimpleName();
        simpleName = simpleName.replaceAll("([A-Z])", " $1").trim();
        return simpleName.replace(" Action", "");
    }

    protected void error(String message) {
        JOptionPane.showMessageDialog(null, message,
                                      String.format("%s failed", getActionName()),
                                      JOptionPane.ERROR_MESSAGE);
    }
}