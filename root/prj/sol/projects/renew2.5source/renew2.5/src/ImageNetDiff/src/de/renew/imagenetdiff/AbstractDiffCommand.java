/**
 *
 */
package de.renew.imagenetdiff;

import CH.ifa.draw.DrawPlugin;
import CH.ifa.draw.IOHelper;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.CombinationFileFilter;
import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import CH.ifa.draw.util.Command;

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * @author Lawrence Cabac
 *
 */
public abstract class AbstractDiffCommand extends Command
        implements DiffExecutor {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(AbstractDiffCommand.class);
    private String imageExtension;

    public void setImageExtension(String imageExtension) {
        this.imageExtension = imageExtension;
    }

    public String getImageExtension() {
        return imageExtension;
    }

    public AbstractDiffCommand(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        //Get the environment. For now we need the gui.
        DrawPlugin guiplugin = DrawPlugin.getCurrent();
        DrawApplication gui = DrawPlugin.getGui();
        JFrame frame = gui.getFrame();
        StatusDisplayer sd = gui;

        // test weather ImageMagick is installed on the system.
        try {
            Runtime.getRuntime().exec("compare --version");
        } catch (IOException e2) {
            logger.error(e2.getMessage());
            logger.debug(e2.getMessage(), e2);
            JOptionPane.showMessageDialog(frame, "ImageMagick not found!",
                                          "ImageMagick not found!",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }


        // If (exactly) two drawings are open in editor ask to make diff 
        // between them
        Enumeration<Drawing> en = gui.drawings();
        int i = 0;
        while (en.hasMoreElements()) {
            en.nextElement();
            i++;
        }
        Drawing[] d = new Drawing[2];
        int check = JOptionPane.NO_OPTION;
        if (i == 2) {
            Enumeration<Drawing> enu = gui.drawings();
            i = 0;
            while (enu.hasMoreElements()) {
                d[i++] = enu.nextElement();
            }
            String message = "Exactly two drawing are opened: "
                             + d[0].getName() + " and " + d[1].getName()
                             + ".\n Would you like to compare these?";
            String title = "Create diff...";
            check = JOptionPane.showOptionDialog(frame, message, title,
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null, null, null);
        }

        // If not (exactly) two drawings are open or if canceled dialogue.
        // Prompt for both comparable files.
        IOHelper io = guiplugin.getIOHelper();
        CombinationFileFilter cff = io.getFileFilter();

        // Do prompt for two files
        Drawing drawing1 = null;
        Drawing drawing2 = null;
        if (check == JOptionPane.YES_OPTION) {
            drawing1 = d[0];
            drawing2 = d[1];
        } else {
            String dir = System.getProperty("user.dir");
            File[] file1 = io.getLoadPath(new File(dir), cff, false);
            File[] file2 = io.getLoadPath(new File(dir), cff, false);
            System.out.println(file1);
            System.out.println(file2);
            if (file1 != null && file2 != null && file1.length > 0
                        && file2.length > 0 && file1[0] != null
                        && file2[0] != null) {
                drawing1 = DrawingFileHelper.loadDrawing(file1[0], gui);
                drawing2 = DrawingFileHelper.loadDrawing(file2[0], gui);

            } else {
                return;
            }
        }
        doDiff(sd, drawing1, drawing2, false);
    }

    protected void exchangeColor(String name, int fuzz, String oldColor,
                                 String newColor) {
        logger.debug("exchangecolor called with: " + fuzz + " " + oldColor
                     + " " + newColor);
        Process process;
        int exit = 0;
        process = null;
        try {
            String cvrtstr = "convert " + name + getImageExtension() + " "
                             + "-fuzz " + fuzz + "% -fill " + newColor
                             + "  -opaque " + oldColor + " " + name
                             + getImageExtension();
            logger.info(Object.class.getName() + ": Convert String:\n"
                        + cvrtstr);
            process = Runtime.getRuntime().exec(cvrtstr);
        } catch (Exception e1) {
            logger.error(e1.getMessage());
            logger.debug(e1.getMessage(), e1);
        }
        if (process != null) {
            try {
                exit = process.waitFor();
                if (logger.isDebugEnabled()) {
                    logger.debug(AbstractDiffClCommand.class.getSimpleName()
                                 + ": process' exit code = " + exit);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}