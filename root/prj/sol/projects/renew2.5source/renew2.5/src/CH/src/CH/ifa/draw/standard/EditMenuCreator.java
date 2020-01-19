/*
 * Created on 06.02.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.application.MenuManager.SeparatorFactory;

import CH.ifa.draw.figures.GroupCommand;
import CH.ifa.draw.figures.SelectCommand;
import CH.ifa.draw.figures.UngroupCommand;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenuItem;

import java.awt.event.KeyEvent;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenuItem;


public class EditMenuCreator {
    public Collection<JMenuItem> createMenus() {
        SeparatorFactory sepFac = new SeparatorFactory("ch.ifa.draw");
        Vector<JMenuItem> result = new Vector<JMenuItem>();
        result.add(createCommandMenuItem(new UndoRedoCommand("Undo",
                                                             UndoRedoCommand.UNDO),
                                         KeyEvent.VK_Z));
        result.add(createCommandMenuItem(new UndoRedoCommand("Redo",
                                                             UndoRedoCommand.REDO),
                                         KeyEvent.VK_Y));
        result.add(sepFac.createSeparator());

        result.add(createCommandMenuItem(new CutCommand("Cut"), KeyEvent.VK_X));
        result.add(createCommandMenuItem(new CopyCommand("Copy"), KeyEvent.VK_C));
        result.add(createCommandMenuItem(new PasteCommand("Paste"),
                                         KeyEvent.VK_V));
        result.add(sepFac.createSeparator());
        result.add(createCommandMenuItem(new DuplicateCommand("Duplicate"),
                                         KeyEvent.VK_D));
        result.add(createCommandMenuItem(new DeleteCommand("Delete")));
        result.add(sepFac.createSeparator());

        result.add(createCommandMenuItem(new SearchCommand("Search..."),
                                         KeyEvent.VK_F));
        result.add(createCommandMenuItem(new ReplaceCommand("Search & Replace..."),
                                         KeyEvent.VK_G));
        result.add(sepFac.createSeparator());

        result.add(createCommandMenuItem(new GroupCommand("Group")));
        result.add(createCommandMenuItem(new UngroupCommand("Ungroup")));
        result.add(sepFac.createSeparator());

        // The menues "Send To Back" and "Bring To Front" were
        // moved to the "Layout" (formerly "Align") menu.
        result.add(createCommandMenuItem(new SelectCommand("Select All"),
                                         KeyEvent.VK_A));
        result.add(createCommandMenuItem(new SelectCommand("Invert Selection",
                                                           Figure.class,
                                                           SelectCommand.INVERT)));
        return result;
    }

    private CommandMenuItem createCommandMenuItem(Command command) {
        return new CommandMenuItem(command);
    }

    private CommandMenuItem createCommandMenuItem(Command command, int shortcut) {
        return new CommandMenuItem(command, shortcut);
    }

//    private CommandMenuItem createCommandMenuItem(Command command,
//                                                  int shortcut, int modifier) {
//        return new CommandMenuItem(command, shortcut, modifier);
//    }
}