package de.renew.gui;

import de.renew.plugin.command.CLCommand;

import java.io.PrintStream;


/**
 * Opens the gui and passes command line arguments.
 * @author Joern Schumacher
 * @author Michael Duvigneau
 **/
public class StartGuiCommand implements CLCommand {
    private GuiPlugin _starter;

    StartGuiCommand(GuiPlugin starter) {
        _starter = starter;
    }

    public void execute(String[] args, final PrintStream response) {
        synchronized (_starter) {
            if (_starter.isGuiPresent()) {
                response.println("Gui already running.");
            } else {
                response.println("Opening gui...");
                _starter.openGui();
            }
            if (args.length > 0) {
                response.println("Passing args to gui...");
                _starter.getGui().loadAndOpenCommandLineDrawings(args);
            }
        }
    }

    public String getDescription() {
        return "start the renew gui";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "(fileNames)*";
    }
}