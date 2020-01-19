package de.renew.refactoring;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import de.renew.refactoring.renamechannel.RenameChannelCommand;
import de.renew.refactoring.renamevariable.RenameVariableCommand;

import java.awt.event.KeyEvent;

import java.net.URL;

import java.util.Collection;


/**
 * Refactoring plugin for Renew.
 *
 * @author 2mfriedr
 */
public class RefactoringPlugin extends PluginAdapter {
    CommandMenu _menu;

    public RefactoringPlugin(URL url) throws PluginException {
        super(url);
    }

    public RefactoringPlugin(PluginProperties props) {
        super(props);
    }

    @Override
    public void init() {
        super.init();
        this.createMenu();
    }

    private void createMenu() {
        MenuManager mm = DrawPlugin.getCurrent().getMenuManager();
        _menu = new CommandMenu("Refactor");
        _menu.putClientProperty(MenuManager.ID_PROPERTY, "de.renew.refactoring");
        _menu.add(new RenameChannelCommand(), KeyEvent.VK_PERIOD);
        _menu.add(new RenameVariableCommand(), KeyEvent.VK_COMMA);
        mm.registerMenu(DrawPlugin.TOOLS_MENU, _menu);
    }

    /**
     * Registers a new command for the Refactor menu.
     *
     * @param command the command
     */
    public void registerCommand(Command command) {
        _menu.add(command);
    }

    /**
     * Registers a new command with a keyboard shortcut for the Refactor menu.
     *
     * @param command the command
     * @param shortcut the shortcut
     */
    public void registerCommand(Command command, int shortcut) {
        _menu.add(command, shortcut);
    }

    /**
     * Registers a new command menu for the Refactor menu.
     *
     * @param menu the menu
     */
    public void registerMenu(CommandMenu menu) {
        _menu.add(menu);
    }

    public static RefactoringPlugin getCurrent() {
        RefactoringPlugin result = null;
        Collection<IPlugin> plugins = PluginManager.getInstance()
                                                   .getPluginsProviding("de.renew.refactoring");
        for (IPlugin plugin : plugins) {
            if (plugin instanceof RefactoringPlugin) {
                result = (RefactoringPlugin) plugin;
                break;
            }
        }
        return result;
    }
}