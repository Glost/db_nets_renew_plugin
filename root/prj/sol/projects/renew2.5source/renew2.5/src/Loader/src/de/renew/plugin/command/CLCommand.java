package de.renew.plugin.command;

import java.io.PrintStream;


/**
 * This interface represents commands that can be given to the PluginManager
 * to enable user interaction.
 * They are registered via the AddCLCommand() method of the PluginManager.
 * The String given when registering is used to identify the command.
 *
 * @author J&ouml;rn Schumacher
 */
public interface CLCommand {

    /**
     * Trigger the functionality represented by this command.
     * @param args      The command line that was typed by the user.
     * @param response  The <code>PrintStream</code> for user feedback.
     */
    public void execute(String[] args, PrintStream response);

    /**
     * Returns a human-readable description of the functionality of this command.
     * @return The description of this Command.
     */
    public String getDescription();

    /**
     * Specifies special arguments as completions for command.
     *
     * The arguments have to be separated by whitespaces. A special syntax is
     * used to denote optional, alternative or recurring arguments. [p]
     * represents an optional argument p. [p|q|r] represents an alternative
     * for an optional argument. (p|q|r) stands for an alternative for a
     * mandatory argument. The brackets (square brackets and parentheses) may
     * be used in combination with the *, which represents a recurring
     * argument. The * may only appear outside of brackets  (e.g. (fileNames)*).
     * Some keywords can be used to represent arguments of a special type.
     * These keywords will be transformed to special <code>Completer</code>s.
     *
     *  Known keywords are:
     *  fileNames,
     *  pluginNames,
     *  locationNames,
     *  propertyNames,
     *  drawingNames
     *
     * @return A String representing the arguments for this command
     */
    public String getArguments();
}