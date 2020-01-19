package de.renew.plugin.command;

import java.io.PrintStream;


/**
 * This command does nothing.
 *
 * @author J&ouml;rn Schumacher
 */
public class NoOpCommand implements CLCommand {
    public void execute(String[] args, PrintStream response) {
    }

    public String getDescription() {
        return "";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return null;
    }
}