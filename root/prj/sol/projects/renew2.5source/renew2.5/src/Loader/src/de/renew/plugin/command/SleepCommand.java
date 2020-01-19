/**
 *
 */
package de.renew.plugin.command;

import java.io.PrintStream;


/**
 * @author Lawrence Cabac (cabac@inf...)
 *
 */
public class SleepCommand implements CLCommand {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SleepCommand.class);

    /**
     * The name for this command.
     */
    public final static String CMD = "sleep";

    public SleepCommand() {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[], java.io.PrintStream)
     */
    @Override
    public void execute(String[] args, PrintStream response) {
        int waitingTime = 5; // in seconds
        if (args.length > 0) {
            waitingTime = new Integer(args[0]).intValue();
        }
        try {
            response.println("SCRIPT: sleeping for " + waitingTime
                             + " seconds ...");
            Thread.sleep(waitingTime * 1000);
            response.println("SCRIPT: ...done! Continuing with script execution.");
        } catch (InterruptedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(SleepCommand.class.getSimpleName()
                             + ": interrupted. This should not happen!");
            }
        }
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "This command waits for a given time until the next command is executed.";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "3";
    }
}