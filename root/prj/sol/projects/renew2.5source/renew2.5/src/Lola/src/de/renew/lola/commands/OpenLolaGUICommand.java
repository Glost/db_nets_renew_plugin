/**
 *
 */
package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;

import de.renew.lola.LolaGUI;

import javax.swing.JFrame;


/**
 * Opens the GUI.
 *
 * @author hewelt, wagner
 *
 */
public class OpenLolaGUICommand extends Command {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(OpenLolaGUICommand.class);
    private LolaGUI gui = null;
    private String lolaPath;

    /**
     * @param name
     */
    public OpenLolaGUICommand(String name, String path) {
        super(name);
        lolaPath = path;
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        DrawApplication app = DrawPlugin.getGui();

        if (app.drawing() instanceof CPNDrawing) {
            if (gui != null) {
                gui.setVisible(true);
                gui.toFront();
                // GUI checks itself on init
                //gui.checkNow();
            } else {
                gui = new LolaGUI("Lola for Renew ", lolaPath);
                gui.setSize(400, 300);
                gui.setVisible(true);
                gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                //gui.checkNow();
            }
        } else {
            DrawPlugin.getCurrent()
                      .showStatus("[Lola GUI] Could not start, current drawing not a CPNDrawing.");
            logger.error("[Lola GUI] Could not open GUI, no open drawing or not CPNDrawing");
        }

        // TODO Auto-generated method stub
    }
}