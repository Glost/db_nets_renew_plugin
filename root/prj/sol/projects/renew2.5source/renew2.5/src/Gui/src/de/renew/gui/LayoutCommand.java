/*
 * @(#)LayoutCommand.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;


/**
 * Command to start/stop animating a drawing.
 */
public class LayoutCommand extends Command {
    // protected LayoutFrame fFrame;

    /**
     * Constructs an animate command.
     * @param name the command name
     */
    public LayoutCommand(String name) {
        super(name);

        //        fFrame = new LayoutFrame(editor);
    }

    public void execute() {
        LayoutFrame frame = new LayoutFrame(DrawPlugin.getCurrent()
                                                      .getDrawingEditor());
        frame.startAnimation();
    }
}