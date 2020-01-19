package de.renew.prompt;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.command.CLCommand;

import de.renew.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.net.URL;

import java.util.Map;


/**
 * This plugin provides an interactive command line user interface.
 *
 * @author Joern Schumacher
 * @author Michael Duvigneau
 **/
public class PromptPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PromptPlugin.class);
    public static final String DONT_PROP_NAME = "de.renew.prompt.dont";
    public static final String ALIVE_PROP_NAME = "de.renew.prompt.keepalive";
    private static final int NULL_INPUT_RETRIES = 3;
    private PromptThread _promptThread;
    private boolean blockingState = false;

    public PromptPlugin(URL location) throws PluginException {
        super(location);
    }

    public PromptPlugin(PluginProperties props) {
        super(props);
    }

    public synchronized void init() {
        // create a command prompt
        boolean startPrompt = !getProperties().getBoolProperty(DONT_PROP_NAME);
        if (startPrompt) {
            prompt();
            setBlockingState(getProperties().getBoolProperty(ALIVE_PROP_NAME));
            PluginManager.getInstance()
                         .addCLCommand("keepalive", new BlockingPromptCommand());
        } else {
            logger.debug("PromptPlugin: " + DONT_PROP_NAME
                         + " is set to true. Not prompting.");
        }
    }

    public synchronized boolean cleanup() {
        if (_promptThread != null) {
            logger.debug("shutting down prompt thread " + _promptThread);
            _promptThread.setStop();
            PluginManager.getInstance().removeCLCommand("keepalive");
            _promptThread = null;
        }
        return true;
    }

    private void prompt() {
        _promptThread = new PromptThread(this);
        _promptThread.start();
    }

    public synchronized void setBlockingState(boolean newState) {
        if (newState != blockingState) {
            blockingState = newState;
            if (blockingState) {
                registerExitBlock();
            } else {
                registerExitOk();
            }
        }
    }

    /**
     * This class represents a thread waiting for input.
     * If a string is entered, the thread compares it
     * to the available commands of the manager; if one is
     * available, its execute method is invoked.
     */
    private class PromptThread extends Thread {
        private boolean _stop = false;
        private BufferedReader _reader;
        private PromptPlugin _myself;

        public PromptThread(PromptPlugin myself) {
            super("Plugin-Prompt-Thread");
            this._myself = myself;
        }

        public void run() {
            logger.debug("Prompt thread running.");
            String command = "";
            int failureCounter = 0;
            InputStreamReader inputreader = new InputStreamReader(System.in);
            _reader = new BufferedReader(inputreader);
            _stop = false;
            try {
                while (!_stop) {
                    System.out.print("Enter command: ");
                    try {
                        command = readCommand();
                        if (command == null) {
                            failureCounter++;
                            if (failureCounter > NULL_INPUT_RETRIES) {
                                logger.error("PromptPlugin: No input. Terminating prompt.");
                                PluginManager.getInstance().stop(_myself);
                                _stop = true;
                            } else {
                                logger.debug("PromptPlugin: No input. ("
                                             + failureCounter + ")");
                            }
                        } else {
                            failureCounter = 0;
                            Map<String, CLCommand> commands = PluginManager.getInstance()
                                                                           .getCLCommands();

                            String[] cmds = command.split(PluginManager.COMMAND_SEPERATOR);
                            for (String cmd : cmds) {
                                String[] cl = StringUtil.splitStringWithEscape(cmd);
                                if (cl.length == 0) {
                                    continue;
                                }
                                CLCommand c = commands.get(cl[0]);
                                if (c == null) {
                                    System.out.println("unknown command.");
                                } else {
                                    String[] nc = new String[cl.length - 1];
                                    for (int i = 0; i < nc.length; i++) {
                                        nc[i] = cl[i + 1];
                                    }
                                    c.execute(nc, System.out);
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        logger.error("PromptThread: an exeption occurred: " + e);
                        logger.error(e.getMessage(), e);
                    }
                }
            } catch (ThreadDeath death) {
                logger.debug("Prompt thread exiting!");
            }

            // We do not try to close the reader because other threads
            // might still want to access the System input stream.
            _reader = null;
            setBlockingState(false);
        }

        protected void setStop() {
            _stop = true;
            interrupt();
        }

        /*
        * Read a line from the command line.
        * If an IOException occurs, an empty String will be returned.
        */
        public String readCommand() {
            String result = "";
            try {
                result = _reader.readLine();
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
            }
            return result;
        }
    }

    private class BlockingPromptCommand implements CLCommand {
        public void execute(String[] args, PrintStream response) {
            if (args.length == 0) {
                if (blockingState) {
                    response.println("Prompt will keep plugin system alive.");
                } else {
                    response.println("Prompt will not prevent plugin system from automatic termination.");
                }
            } else if ("on".equals(args[0])) {
                setBlockingState(true);
                response.println("Prompt will keep plugin system alive.");
            } else if ("off".equals(args[0])) {
                setBlockingState(false);
                response.println("Prompt will not prevent plugin system from automatic termination.");
            } else {
                response.println("Controls the keep-alive feature of the Renew Prompt plugin.\n"
                                 + "Arguments:\n"
                                 + " - \"on\" prevents the plugin system from automatic termination.\n"
                                 + " - \"off\" allows automatic termination as long as no other plugin prevents it.\n"
                                 + " - no argument displays the current keep-alive mode.");
            }
        }

        public String getDescription() {
            return "controls the keep-alive feature of the Renew Prompt plugin.";
        }

        /**
         * @see de.renew.plugin.command.CLCommand#getArguments()
         */
        @Override
        public String getArguments() {
            return null;
        }
    }
}