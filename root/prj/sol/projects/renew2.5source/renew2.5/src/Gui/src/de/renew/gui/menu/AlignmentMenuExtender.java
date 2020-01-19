/*
 * Created on 28.01.2004
 *
 */
package de.renew.gui.menu;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.standard.ChangeAttributeCommand;

import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.CommandMenuItem;

import de.renew.gui.ChangeSizeCommand;
import de.renew.gui.LayoutCommand;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenuItem;


/**
 * This class is used by the Gui plugin to add items to the Layout menu.
 *
 * @author J&ouml;rn Schumacher
 */
public class AlignmentMenuExtender {
    public Collection<JMenuItem> createMenus() {
        Collection<JMenuItem> result = new Vector<JMenuItem>();
        CommandMenu sizeMenu = DrawApplication.createCommandMenu("Figure Size");

        sizeMenu.add(new CommandMenuItem(new ChangeSizeCommand("copy within selection",
                                                               ChangeSizeCommand.FIRST_FIGURE)));
        sizeMenu.add(new CommandMenuItem(new ChangeSizeCommand("reset to default",
                                                               ChangeSizeCommand.DEFAULT_SIZE)));
        result.add(sizeMenu);

        result.add(MenuManager.createSeparator("sep"));
        LayoutCommand lc = new LayoutCommand("Automatic Layout...");
        result.add(new CommandMenuItem(lc));

        CommandMenu fixMenu = DrawApplication.createCommandMenu("Location");
        fixMenu.add(new ChangeAttributeCommand("dynamic", "Location",
                                               Boolean.FALSE));
        fixMenu.add(new ChangeAttributeCommand("fixed", "Location", Boolean.TRUE));
        result.add(fixMenu);

        return result;
    }
}