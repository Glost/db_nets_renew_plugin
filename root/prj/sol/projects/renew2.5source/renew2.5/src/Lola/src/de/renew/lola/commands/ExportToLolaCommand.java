/**
 *
 */
package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;
import de.renew.gui.GuiPlugin;

import de.renew.lola.LolaFileCreator;

import de.renew.plugin.command.CLCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;


/**
 * @author Lawrence Cabac
 *
 */
public class ExportToLolaCommand extends Command implements CLCommand {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ExportToLolaCommand.class);

    public ExportToLolaCommand(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[], java.io.PrintStream)
     */
    public void execute(String[] args, PrintStream response) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    public String getDescription() {
        return "Export to lola format.";
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        DrawingEditor drawingEditor = GuiPlugin.getCurrent().getDrawingEditor();
        Drawing drawing = drawingEditor.drawing();
        JFrame editor = DrawPlugin.getGui().getFrame();

        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setDialogTitle("Select a file to save exported net.");
        chooser.setToolTipText("Good Luck.");
        int returnValue = chooser.showSaveDialog(editor);
        File selectedFile;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
        } else {
            logger.info("User abort.");
            return;
        }
        try {
            OutputStream stream = new FileOutputStream(selectedFile);
            LolaFileCreator creator = new LolaFileCreator();
            creator.writeLolaFile(stream, (CPNDrawing) drawing);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}