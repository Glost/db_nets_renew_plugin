/*
 * Created on Feb 3, 2005
 *
 */
package de.renew.fa;

import CH.ifa.draw.util.Command;

import de.renew.plugin.command.CLCommand;

import java.util.Vector;


/**
 * @author cabac
 *
 */
public interface FAPluginExtender {
    /**
     * Returns a list of Commands - made available by the plugin - that can be
     * added to the Menu of the plugee.
     */
    Vector<Command> getMenuCommands();

    /**
     * Returns Prompt (Command Line) Commands that offer functionality of the
     * plugin to the plugee. The plugee can offer this functionality to the user
     * on the prompt.
     *
     */
    Vector<CLCommand> getPromptCommands();
}