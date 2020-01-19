package de.renew.ant;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.SimpleFileFilter;
import CH.ifa.draw.io.StatusDisplayer;
import CH.ifa.draw.io.exportFormats.ExportFormat;

import CH.ifa.draw.util.Iconkit;

import java.awt.Frame;

import java.io.File;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;


public abstract class AbstractExportTask extends Task {
    private final ExportFormat exportFormat;
    private final String defaultExtension;
    private File destdir = null;
    private Vector<FileSet> filesets = new Vector<FileSet>();

    protected AbstractExportTask(ExportFormat exportFormat) {
        super();
        this.exportFormat = exportFormat;
        FileFilter fileFilter = exportFormat.fileFilter();
        if (fileFilter instanceof SimpleFileFilter) {
            this.defaultExtension = ((SimpleFileFilter) fileFilter).getExtension();
        } else {
            // FIXME this is an ugly fallback (may contain capitalization and whitespace).
            this.defaultExtension = exportFormat.formatName();
        }
    }

    /**
     * Configures the root directory of the directory tree where all
     * <code>.eps</code> files are put into.
     *
     * @param dest a directory location
     **/
    public void setDestdir(File dest) {
        this.destdir = dest;
    }

    /**
     * Is invoked by the ant task and exports all the given
     * <code>.rnw<code/> to <code>.eps<code/>
     * into the same directory as the original file is in.
     **/
    public void execute() throws BuildException {
        super.execute();

        // minimal Renew logging configuration that forwards messages
        // to the Ant logging system
        Logger logger = Logger.getLogger("CH.ifa.draw");
        AntTaskLogAppender appender = AntTaskLogAppender.getInstance(this);
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
        logger = Logger.getLogger("de.renew");
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        // get all files - out of the vector full of filesets -> 2 loops
        Iterator<FileSet> filesetIterator = filesets.iterator();
        while (filesetIterator.hasNext()) {
            FileSet elementFileSet = filesetIterator.next();
            DirectoryScanner dirScan = elementFileSet.getDirectoryScanner(getProject());
            String[] fileNames = dirScan.getIncludedFiles();
            log("filenames: " + fileNames.length, Project.MSG_VERBOSE);

            for (int i = 0; i < fileNames.length; i++) {
                String fileName = fileNames[i];
                File inFile = new File(dirScan.getBasedir() + "/" + fileName);
                log("File: " + inFile.getAbsolutePath(), Project.MSG_VERBOSE);

                // get drawing from file
                Drawing netDrawing = DrawingFileHelper.loadDrawing(inFile,
                                                                   new StatusDisplayer() {
                        public void showStatus(String message) {
                            log(message);
                        }
                    }); //rnw-Object
                log("Drawing: " + netDrawing, Project.MSG_VERBOSE);

                if (netDrawing == null) {
                    throw new BuildException("Could not read drawing file: "
                                             + inFile);
                }

                // Export netDrawing
                try {
                    File helpFile = new File(destdir + "/" + fileName); // helps to get the right directory path
                    File outFile = new File(helpFile.getParent() + "/"
                                            + netDrawing.getName() + "."
                                            + defaultExtension);

                    //create directories
                    outFile.getParentFile().mkdirs();
                    exportFormat.export(netDrawing, outFile);
                } catch (Exception e) {
                    throw new BuildException(e);
                }
            }
        }
    }

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
}