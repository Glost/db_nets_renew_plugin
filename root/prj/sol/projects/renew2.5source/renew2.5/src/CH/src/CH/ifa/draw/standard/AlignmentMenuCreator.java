/*
 * Created on 06.02.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager.SeparatorFactory;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.CommandMenuItem;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenuItem;


public class AlignmentMenuCreator {
    public Collection<JMenuItem> createMenus() {
        SeparatorFactory sepFac = new SeparatorFactory("ch.ifa.draw");
        Vector<JMenuItem> result = new Vector<JMenuItem>();
        result.add(createCommandMenu(new ToggleGridCommand("Toggle Snap to Grid")));
        result.add(createCommandMenu(new SnapToGridCommand("Snap to Grid now")));
        result.add(sepFac.createSeparator());

        int ctrlDownMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        CommandMenu alignMenu = DrawApplication.createCommandMenu("Align");
        alignMenu.add(new AlignCommand("Lefts", AlignCommand.LEFTS));
        alignMenu.add(new AlignCommand("Centers", AlignCommand.CENTERS),
                      KeyEvent.VK_BACK_SLASH,
                      ctrlDownMask //+ KeyEvent.SHIFT_DOWN_MASK
        );
        alignMenu.add(new AlignCommand("Rights", AlignCommand.RIGHTS));
        alignMenu.addSeparator();
        alignMenu.add(new AlignCommand("Tops", AlignCommand.TOPS));
        alignMenu.add(new AlignCommand("Middles", AlignCommand.MIDDLES),
                      KeyEvent.VK_MINUS,
                      ctrlDownMask //+ KeyEvent.SHIFT_DOWN_MASK
        );
        alignMenu.add(new AlignCommand("Bottoms", AlignCommand.BOTTOMS));
        result.add(alignMenu);
        CommandMenu spreadMenu = DrawApplication.createCommandMenu("Spread");
        spreadMenu.add(new SpreadCommand("Lefts", SpreadCommand.LEFTS));
        spreadMenu.add(new SpreadCommand("Centers", SpreadCommand.CENTERS),
                       KeyEvent.VK_EQUALS,
                       ctrlDownMask //+ KeyEvent.SHIFT_DOWN_MASK
        );
        spreadMenu.add(new SpreadCommand("Rights", SpreadCommand.RIGHTS));
        spreadMenu.add(new SpreadCommand("Distances",
                                         SpreadCommand.HORIZONTAL_DISTANCE));
        spreadMenu.addSeparator();
        spreadMenu.add(new SpreadCommand("Tops", SpreadCommand.TOPS));
        spreadMenu.add(new SpreadCommand("Middles", SpreadCommand.MIDDLES),
                       KeyEvent.VK_SEMICOLON,
                       ctrlDownMask //+ KeyEvent.SHIFT_DOWN_MASK
        );
        spreadMenu.add(new SpreadCommand("Bottoms", SpreadCommand.BOTTOMS));
        spreadMenu.add(new SpreadCommand("Distances",
                                         SpreadCommand.VERTICAL_DISTANCE));

        spreadMenu.addSeparator();
        spreadMenu.add(new SpreadCommand("Diagonal",
                                         SpreadCommand.DIAGONAL_CENTERS),
                       KeyEvent.VK_SLASH, ctrlDownMask);

        //spreadMenu.add(new SpreadCommand("Distances", SpreadCommand.DIAGONAL_DISTANCE));
        result.add(spreadMenu);
        result.add(sepFac.createSeparator());

        // the following menus used to be in "Edit":
        result.add(createCommandMenu(new SendToBackCommand("Send to Back"),
                                     KeyEvent.VK_B));
        result.add(createCommandMenu(new BringToFrontCommand("Bring to Front"),
                                     KeyEvent.VK_B,
                                     ctrlDownMask + KeyEvent.SHIFT_DOWN_MASK));
        return result;
    }

    private CommandMenuItem createCommandMenu(Command command) {
        return new CommandMenuItem(command);
    }

    private CommandMenuItem createCommandMenu(Command command, int shortcut) {
        return new CommandMenuItem(command, shortcut);
    }

    private CommandMenuItem createCommandMenu(Command command, int shortcut,
                                              int modifier) {
        return new CommandMenuItem(command, shortcut, modifier);
    }
}