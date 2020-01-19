package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager.SeparatorFactory;

import CH.ifa.draw.io.ExportHolderImpl;
import CH.ifa.draw.io.ImportHolderImpl;

import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.CommandMenuItem;
import CH.ifa.draw.util.UpdatableCommandMenuItem;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenuItem;


public class FileMenuCreator {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FileMenuCreator.class);
    private UpdatableCommandMenuItem defaultMenuItem = null;

    public Collection<JMenuItem> createMenus(ImportHolderImpl importHolder,
                                             ExportHolderImpl exportHolder,
                                             CommandMenu recentlySavedMenu) {
        Vector<JMenuItem> result = new Vector<JMenuItem>();
        SeparatorFactory sepFac = new SeparatorFactory("ch.ifa.draw");

        if (defaultMenuItem == null) {
            defaultMenuItem = new UpdatableCommandMenuItem(new NewDefaultDrawingCommand(),
                                                           KeyEvent.VK_N);
        }
        result.add(defaultMenuItem);
        result.add(new CommandMenuItem(new NewDrawingCommand()));

        result.add(sepFac.createSeparator());

        result.add(new CommandMenuItem(new OpenURLCommand()));
        result.add(new CommandMenuItem(new OpenDrawingCommand(), KeyEvent.VK_O));
        result.add(new CommandMenuItem(new InsertDrawingCommand()));
        result.add(new CommandMenuItem(new SaveDrawingCommand(), KeyEvent.VK_S));
        result.add(new CommandMenuItem(new SaveDrawingAsCommand(),
                                       KeyEvent.VK_S,
                                       Toolkit.getDefaultToolkit()
                                              .getMenuShortcutKeyMask()
                                       + KeyEvent.SHIFT_DOWN_MASK));
        result.add(new CommandMenuItem(new SaveAllDrawingsCommand()));
        result.add(new CommandMenuItem(new CloseDrawingCommand(), KeyEvent.VK_W));
        result.add(new CommandMenuItem(new CloseAllDrawingsCommand(),
                                       KeyEvent.VK_W,
                                       Toolkit.getDefaultToolkit()
                                              .getMenuShortcutKeyMask()
                                       + KeyEvent.SHIFT_DOWN_MASK));

        result.add(recentlySavedMenu);

        result.add(sepFac.createSeparator());

        result.add(importHolder.getImportMenu());
        result.add(exportHolder.getExportMenu());

        result.add(sepFac.createSeparator());

        result.add(new CommandMenuItem(new PrintDrawingCommand()));

        result.add(sepFac.createSeparator());

        result.add(DrawApplication.createMenuItem("Exit",
                                                  new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    DrawApplication app = DrawPlugin.getGui();
                    app.requestClose();
                }
            }));

        return result;
    }
}