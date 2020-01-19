/**
 *
 */
package de.renew.fa.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.util.Command;

import de.renew.fa.service.JflapFileCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * @author Cabac
 *
 */
public class ExportJflapCommand extends Command {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ExportJflapCommand.class);

    public ExportJflapCommand(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        Drawing drawing = DrawPlugin.getGui().drawing();
        String name = drawing.getName();
        File path = drawing.getFilename().getParentFile();
        File file = new File(path, name + ".jff");
        OutputStream stream;
        try {
            stream = new FileOutputStream(file);
            JflapFileCreator.export(stream, drawing);
            stream.close();
        } catch (FileNotFoundException e) {
            logger.error(ExportJflapCommand.class.getName() + ": ", e);
        } catch (IOException e) {
            logger.error(ExportJflapCommand.class.getName() + ": ", e);
        }
    }
}