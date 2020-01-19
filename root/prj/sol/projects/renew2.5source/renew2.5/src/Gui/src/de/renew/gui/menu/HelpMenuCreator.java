/*
 * Created on 05.02.2004
 *
 * Create the help menu for the Gui Plugin.
 *
 */
package de.renew.gui.menu;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.standard.InfoDialog;

import de.renew.gui.GuiPlugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenuItem;


/**
 * @author 6schumac
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HelpMenuCreator {
    public Collection<JMenuItem> createMenus() {
        Vector<JMenuItem> result = new Vector<JMenuItem>();
        result.add(DrawApplication.createMenuItem("About...",
                                                  new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    InfoDialog about = GuiPlugin.getCurrent().createAboutBox();
                    about.setVisible(true);
                }
            }));
        return result;
    }
}