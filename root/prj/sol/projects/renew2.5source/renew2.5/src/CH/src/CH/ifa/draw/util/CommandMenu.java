/*
 * @(#)CommandMenu.java 5.1
 *
 */
package CH.ifa.draw.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;


/**
 * A {@link Command} enabled menu.  Selecting a <code>Command</code> menu
 * item executes the corresponding command.  <code>Command</code> menu
 * items are checked automatically for enabledness.
 *
 * @see Command
 */
public class CommandMenu extends JMenu implements ActionListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CommandMenu.class);

    /**
     * Maps from commands to their associated CommandMenuItems. This
     * information is needed to be able to remove those items later on
     * request.
     **/
    private Map<Command, CommandMenuItem> commandItems = new HashMap<Command, CommandMenuItem>();

    public CommandMenu(String name) {
        super(name);
        checkEnabled();
    }

    /**
     * Adds an item to the menu.  If the item is a {@link CommandMenuItem},
     * the command magic is enabled (see class <code>CommandMenu</code>
     * documentation).  It is not allowed to add the same command twice.
     *
     * @param item the <code>JMenuItem</code> to add to the menu.
     * @return the added <code>JMenuItem</code>.
     * @throws IllegalArgumentException
     *   if an item for the same command has already been added to the menu.
     **/
    public synchronized JMenuItem add(JMenuItem item) {
        if (item instanceof CommandMenuItem) {
            CommandMenuItem citem = (CommandMenuItem) item;
            Command command = citem.getCommand();
            if (commandItems.containsKey(command)) {
                throw new IllegalArgumentException("Cannot add command "
                                                   + command.name()
                                                   + ": it already exists in the menu.");
            }
            commandItems.put(command, citem);
            citem.addActionListener(this);
            JMenuItem result = super.add(citem);
            checkEnabled();
            return result;
        } else {
            return super.add(item);
        }
    }

    /**
     * Adds an item to the menu at the specified position.
     * If the item is a {@link CommandMenuItem}, the command magic is
     * enabled (see class <code>CommandMenu</code> documentation).
     * It is not allowed to add the same command twice.
     *
     * The super method is defined for {@link Component}s, therefore
     * this method has to accept them and also return them.
     *
     * @param item the <code>Component</code> to add to the menu.
     * @return the added <code>Component</code>.
     * @throws IllegalArgumentException
     *   if an item for the same command has already been added to the menu.
     */
    @Override
    public synchronized Component add(Component item, int position) {
        if (item instanceof CommandMenuItem) {
            CommandMenuItem citem = (CommandMenuItem) item;
            Command command = citem.getCommand();
            if (commandItems.containsKey(command)) {
                throw new IllegalArgumentException("Cannot add command "
                                                   + command.name()
                                                   + ": it already exists in the menu.");
            }
            commandItems.put(command, citem);
            citem.addActionListener(this);
            Component result = super.add(item, position);
            checkEnabled();
            return result;
        } else {
            return super.add(item, position);
        }
    }

    /**
     * Adds a command to the menu.  The item's label is the command's
     * name.  It is not allowed to add the same command twice.
     */
    public synchronized void add(Command command) {
        add(new CommandMenuItem(command));
    }

    /**
     * Adds a command with the given short cut to the menu. The item's
     * label is the command's name. It is not allowed to add the same
     * command twice.
     */
    public synchronized void add(Command command, int shortcut) {
        add(new CommandMenuItem(command, shortcut));
    }

    /**
     * Adds a command with the given short cut and a modifier to the menu.
     * The item's label is the command's name. It is not allowed to
     * add the same command twice.
     * This method is added to allow other modifier keys then the usual (Ctrl.)
     */
    public synchronized void add(Command command, int shortcut, int modifier) {
        add(new CommandMenuItem(command, shortcut, modifier));
    }

    /**
     * Removes a command from the menu.
     *
     * @param command the <code>Command</code> to remove.
     * @throws NoSuchElementException
     *   if the given command that does not exist in the menu.
     **/
    public synchronized void remove(Command command) {
        CommandMenuItem citem = commandItems.get(command);
        if (citem == null) {
            throw new IllegalArgumentException("Cannot remove command "
                                               + command.name()
                                               + ": it does not exist in the menu.");
        } else {
            remove(citem);
        }
    }

    /**
     * Removes an item from the menu.
     *
     * If the item is a {@link CommandMenuItem}, the the command magic is
     * removed, too.
     *
     * @param item the <code>JMenuItem</code> to remove.
     **/
    public synchronized void remove(JMenuItem item) {
        if (item instanceof CommandMenuItem) {
            CommandMenuItem citem = (CommandMenuItem) item;
            Command command = citem.getCommand();
            if (commandItems.get(command) != item) {
                throw new IllegalArgumentException("Cannot remove item for command "
                                                   + command.name()
                                                   + ": the item is insconsistent with the menu.");
            }
            commandItems.remove(command);
            citem.removeActionListener(this);
        }
        super.remove(item);
    }

    public synchronized boolean checkEnabled() {
        boolean menuEnabled = false;
        boolean enabled;
        for (int i = 0; i < getMenuComponentCount(); i++) {
            Component item = getMenuComponent(i);
            if (item == null) {
                return false;
            }
            if (item instanceof CommandMenuItem) {
                enabled = ((CommandMenuItem) item).getCommand().isExecutable();
                item.setEnabled(enabled);
                menuEnabled = menuEnabled | enabled;
            } else if (item instanceof CommandMenu) {
                enabled = ((CommandMenu) item).checkEnabled();
                item.setEnabled(enabled);
                menuEnabled = menuEnabled | enabled;
            } else if (item instanceof JSeparator) {
                // Ignore separators.
            } else {
                menuEnabled = menuEnabled | item.isEnabled();
            }
        }
        return menuEnabled;
    }

    public synchronized void updateCommandText() {
        String commandText;
        for (int i = 0; i < getMenuComponentCount(); i++) {
            Component item = getMenuComponent(i);
            if (item == null) {
                return;
            }
            if (item instanceof CommandMenuItem) {
                Command command = ((CommandMenuItem) item).getCommand();
                commandText = command.name();
                String text = ((AbstractButton) item).getText();
                if (text != null && commandText != null
                            && !text.equals(commandText)) {
                    ((AbstractButton) item).setText(commandText);
                }
            } else if (item instanceof CommandMenu) {
                ((CommandMenu) item).updateCommandText();
            }
        }
    }

    /**
     * Executes the command.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof CommandMenuItem) {
            Command cmd = ((CommandMenuItem) source).getCommand();
            cmd.execute();
        }
    }
}