package de.renew.gui;

import CH.ifa.draw.util.Command;


/**
 * Connects to another simulation server.
 *
 * @author Thomas Jacob
 */
public class OpenServerFrameCommand extends Command {
    // private CPNApplication editor;
    private RemoteServerController _controller = null;

    public OpenServerFrameCommand(String name) {
        super(name);
        // this.editor = editor;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return true;
    }

    public void execute() {
        if (_controller == null) {
            CPNApplication editor = GuiPlugin.getCurrent().getGui();
            _controller = new RemoteServerController(editor);
        }
        _controller.show();
    }
}