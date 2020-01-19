/**
 *
 */
package de.renew.imagenetdiff;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.plugin.command.CLCommand;

import java.io.File;
import java.io.PrintStream;


/**
 * @author Lawrence Cabac
 *
 */
abstract public class AbstractDiffClCommand implements CLCommand,
                                                       StatusDisplayer {
    String COMMAND = "no name";
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(AbstractDiffClCommand.class);
    DiffExecutor diffCommand;
    String[] EXT = { "", ".rnw", ".aip", ".draw", ".arm", ".mad" };

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[], java.io.PrintStream)
     */
    public void execute(String[] args, PrintStream response) {
        int index = 0;
        boolean quiteMode = false;
        if (args.length > 0 && args[index].equals("-q")) {
            quiteMode = true;
            index++;
        }
        if (args.length - index == 0) {
            showStatus("No args gives. Synopsys: " + getName()
                       + " [-q] file1 [file2]");
        }
        while (args.length - index >= 2) {
            String filename1 = args[index++];
            String filename2 = args[index++];
            for (String ext : EXT) {
                showStatus("Diff: trying files: " + filename1 + ext + " and "
                           + filename2 + ext);
                File file1 = new File(filename1 + ext);
                File file2 = new File(filename2 + ext);
                if (file1.exists() && file2.exists()) {
                    Drawing drawing1 = DrawingFileHelper.loadDrawing(file1, this);
                    Drawing drawing2 = DrawingFileHelper.loadDrawing(file2, this);

                    if (logger.isDebugEnabled()) {
                        logger.debug(AbstractDiffClCommand.class.getSimpleName()
                                     + ": first drawing to diff: "
                                     + drawing1.getFilename().getPath());
                        logger.debug(AbstractDiffClCommand.class.getSimpleName()
                                     + ": second drawing to diff: "
                                     + drawing2.getFilename().getPath());
                    }
                    diffCommand.doDiff(this, drawing1, drawing2, quiteMode);
                    break;
                } else {
                    showStatus("One or more files do not exist: " + filename1
                               + ", " + filename2);
                }
            }
        }
        if (args.length - index == 1) {
            String filename1 = args[index];
            for (String ext : EXT) {
                showStatus("Diff (svn base): trying file: " + filename1 + ext);
                File file1 = new File(filename1 + ext);
                if (file1.exists()) {
                    String path = file1.getParent();
                    String name = file1.getName();
                    String pathprefix = "";
                    if (path != null) {
                        pathprefix = path + File.separator;
                    }
                    String filename2 = pathprefix + ".svn" + File.separator
                                       + "text-base" + File.separator + name
                                       + ".svn-base";
                    File file2 = new File(filename2);
                    if (file2.exists()) {
                        Drawing drawing1 = DrawingFileHelper.loadDrawing(file1,
                                                                         this);
                        Drawing drawing2 = DrawingFileHelper.loadDrawing(file2,
                                                                         this);
                        diffCommand.doDiff(this, drawing1, drawing2, quiteMode);
                    } else {
                        showStatus("Could not find file: " + filename2);
                    }
                    break;
                } else {
                    showStatus("File " + args[index] + ext + " does not exist.");
                }
            }
        }
    }

    protected String getName() {
        return COMMAND;
    }

    public void showStatus(String message) {
        logger.info(message);
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "[-q] fileNames fileNames";
    }
}