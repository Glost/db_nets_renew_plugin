package de.renew.plugin.command;

import java.io.PrintStream;


/**
 * This plugin manager command allows to trigger Java's garbage
 * collector.
 *
 * @author Michael Duvigneau
 **/
public class GCCommand implements CLCommand {
    public void execute(String[] args, PrintStream response) {
        System.gc();
    }

    public String getDescription() {
        return "triggers the Java garbage collector.";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return null;
    }
}