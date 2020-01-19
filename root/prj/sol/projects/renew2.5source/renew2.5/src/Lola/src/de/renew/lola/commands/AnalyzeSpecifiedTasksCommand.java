package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.standard.NullDrawing;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;
import de.renew.gui.GuiPlugin;

import de.renew.lola.LolaFileCreator;
import de.renew.lola.LolaHelper;

import de.renew.plugin.command.CLCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 * This Command is executed when clicking "Analyze specified tasks" from the "Lola integration" menu.
 * It works on the current drawing (that needs to be a CPNDrawing) or let the user choose a net file
 * if there is no current drawing.
 * It exports the drawing into a lola net file and extracts task specifications from the file and writes
 * them to task files. Then for each task specification the appropriate lola binary is called to verify
 * the specification. Results are presented on the console.
 *
 * @author Marcin Hewelt, Thomas Wagner
 *
 * */
public class AnalyzeSpecifiedTasksCommand extends Command implements CLCommand {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(AnalyzeSpecifiedTasksCommand.class);

    /**
     * Points to the location, where the lola binaries reside
     */
    private File lolaPath;

    /**
     * Net file and task files are created in this location.
     * It is either the "official" tmp directory (e.g. /tmp on linux)
     * or user defined by setting the property de.renew.lola.tmpdir
     */
    private File tmpDir;

    /**
     * Constructs AnalyzeSpecifiedTasksCommand, sets the name, lolaPath and tmpDir.
     * @param name The Name of this command like it appears in the "Lola Integration" menu
     * @param path Location of the lola binaries
     */
    public AnalyzeSpecifiedTasksCommand(String name, String path) {
        super(name);
        lolaPath = new File(path);
        // determine tmp directory
        tmpDir = LolaHelper.findTmpDir();
    }


    /*
     * (non-Javadoc)
     *
     * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[],
     * java.io.PrintStream)
     */
    public void execute(String[] args, PrintStream response) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    public String getDescription() {
        return "Analyze file in lola format.";
    }

    /**
     * Executing this command causes the current drawing to be
     * exported into a lola net file and any verification tasks
     * specified in the drawing to be exported in a lola task file each.
     * Then for each of the tasks the appropriate lola binary is executed
     * and the verification results are printed to the console.
     *
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        DrawingEditor drawingEditor = GuiPlugin.getCurrent().getDrawingEditor();
        Drawing drawing = drawingEditor.drawing();
        LolaFileCreator creator = new LolaFileCreator();
        File tmpFile = null;

        String netNameChoosen = "";
        File netTmpDirChoosen = null;
        /* No current drawing, let the user select one lola net file.
        * Unfortunatly in this case we don't have the rnw net, just
        * the net file which doesn't contain task specifications
        * so we later on just use those task files we find in the dir of
        * the choosen net file.
        */
        if (drawing instanceof NullDrawing) {
            tmpFile = chooseLolaFile();
            if (tmpFile == null) {
                logger.info("[Lola] Neither open drawing, nor file selected");
                return;
            } else {
                netTmpDirChoosen = new File(tmpFile.getParent());
                netNameChoosen = tmpFile.getName()
                                        .substring(0,
                                                   tmpFile.getName()
                                                          .indexOf(".net"));
                logger.info("[Lola] Choosen file: " + tmpFile.toString());
                logger.info("[Lola] Using task files in: "
                            + netTmpDirChoosen.toString());
            }
        }
        final String netName = (drawing instanceof NullDrawing)
                               ? netNameChoosen : drawing.getName();
        final File netTmpDir = (drawing instanceof NullDrawing)
                               ? netTmpDirChoosen : new File(tmpDir, netName);
        if (drawing instanceof CPNDrawing) {
            // create the temporary directory to store net and task files
            if (netTmpDir.exists()) {
                // preemptively delete it
                for (File f : netTmpDir.listFiles()) {
                    f.delete();
                }
                netTmpDir.delete();
            }
            if (netTmpDir.mkdir()) {
                logger.info("[Lola] Created temporary directory " + netTmpDir);
                tmpFile = new File(netTmpDir, netName + ".net");
            } else {
                logger.info("[Lola] Could not create directory " + netTmpDir);
                logger.info("[Lola] Instead using " + tmpDir);
                tmpFile = new File(netTmpDir, netName + ".net");
            }

            // delete previous versions of net file
            if (tmpFile.exists()) {
                tmpFile.delete();
            }

            // create tmp file and export drawing to it
            try {
                tmpFile.createNewFile();
                // open file output stream
                FileOutputStream stream = new FileOutputStream(tmpFile);
                // write drawing to net file and specified tasks to task files
                creator.writeLolaFile(stream, (CPNDrawing) drawing);
                creator.createTasks((CPNDrawing) drawing, tmpFile);
                stream.flush();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("[Lola] tmpFile in " + tmpDir.toString()
                             + "couldn't be created or written to");
            }
        }
        logger.info("[Lola] Analyzing net " + tmpFile.toString());
        Runtime myrun = Runtime.getRuntime();

        // get task files (those are all fils in netTmpDir that start with netName and end with ".task")
        File[] taskFiles = netTmpDir.listFiles(new java.io.FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().startsWith(netName)
                           && pathname.toString().endsWith(".task") ? true : false;
                }
            });
        if (taskFiles.length == 0) {
            logger.info("[Lola] No task files found for net " + netName);
        }

        // now iterate
        for (File taskFile : taskFiles) {
            // extract the task type from the task filename 
            String taskType = filename2taskType(netName, taskFile.getName());

            // now get appropriate lola binary name
            String lolaCommand = LolaHelper.taskCommandMap.get(taskType);

            // construct lola call 
            File lolaBin = new File(lolaPath, lolaCommand);
            String[] execCommand = { lolaBin.toString(), tmpFile.toString(), "-a", taskFile
                                                                                   .toString() };
            logger.info("[Lola] Executing " + execCommand);
            try {
                Process lolproc = myrun.exec(execCommand);
                BufferedReader input = new BufferedReader(new InputStreamReader(lolproc
                                                                                .getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
                input.close();
            } catch (IOException e) {
                logger.error("[Lola] Execution failed");
                e.printStackTrace();
            }
        }
    }

    /**
     * Extracts the taskType (e.g. Marking) from the filename of the task file (e.g. netnameMarking2.task)
     * @param netName
     * @param taskFileName
     * @return the type of a task file
     */
    private String filename2taskType(final String netName, String taskFileName) {
        String taskType = taskFileName.substring(netName.length(),
                                                 taskFileName.indexOf(".task"));
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola] taskType before trimming: " + taskType);
        }

        // strip number at end (task counter)
        while (Character.isDigit(taskType.charAt(taskType.length() - 1))) {
            taskType = taskType.substring(0, taskType.length() - 1);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola] taskType trimmed: " + taskType);
        }
        return taskType;
    }

    /**
     * Let user select a file in lola net file format that should be analyzed.
     * @return the selected file or null if aborted by user
     */
    private File chooseLolaFile() {
        JFrame editor = DrawPlugin.getGui().getFrame();

        // make file chooser
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        FileFilter filter = new FileNameExtensionFilter("lola net format", "net");
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
        chooser.setDialogTitle("Select a file (in lola net format) to analyze.");

        int returnValue = chooser.showOpenDialog(editor);
        File selectedFile;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            logger.info(selectedFile.toString() + " was selected");
        } else {
            logger.info("User abort.");
            return null;
        }
        return selectedFile;
    }
}