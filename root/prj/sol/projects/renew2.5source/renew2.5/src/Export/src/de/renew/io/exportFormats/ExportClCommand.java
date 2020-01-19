/**
 *
 */
package de.renew.io.exportFormats;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.ExportHolder;
import CH.ifa.draw.io.ExportHolderImpl;
import CH.ifa.draw.io.SimpleFileFilter;
import CH.ifa.draw.io.StatusDisplayer;
import CH.ifa.draw.io.exportFormats.ExportFormat;
import CH.ifa.draw.io.exportFormats.ExportFormatMultiAbstract;

import CH.ifa.draw.util.Iconkit;

import de.renew.plugin.command.CLCommand;

import java.awt.Frame;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.filechooser.FileFilter;


/**
 * Exports a drawing to an available export format.
 *
 * @author Lawrence Cabac
 *
 */
public class ExportClCommand implements CLCommand, StatusDisplayer {
    private static final String SYNOPSIS = "Exports a drawing. Usage: ex <type> [drawing]+ [options]";
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ExportClCommand.class);

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[], java.io.PrintStream)
     */
    public void execute(String[] args, PrintStream response) {
        CommandLineParser parser = new DefaultParser();
        Options opts = new Options();
        opts.addOption("a", "accumulate", false,
                       "n-to-1 export (only available for some formats, e.g. ShadowNetSystem)");
        opts.addOption("o", "output", true, "output file");

        ExportHolderImpl exporter = (ExportHolderImpl) DrawPlugin.getCurrent()
                                                                 .getExportHolder();
        List<ExportFormat> formats = flattenedExportFormats();

        CommandLine line = null;
        try {
            line = parser.parse(opts, args);
        } catch (ParseException e1) {
            response.append("Could not parse command.\n");
            printHelp(response, formats, opts);
            return;
        }
        List<String> argList = line.getArgList();

        if (argList.size() < 2) {
            response.append("Not enough arguments.\n");
            printHelp(response, formats, opts);
            return;
        }

        String type = argList.remove(0);
        List<String> files = argList;

        boolean accumulate = line.hasOption("a");

        // multiple files and custom output file, but not a n-to-1 export
        if (files.size() > 1 && line.hasOption("o") && !accumulate) {
            response.append("Cannot export multiple drawings with custom output file (except for n-to-1 export).\n");
            printHelp(response, formats, opts);
            return;
        }

        ExportFormat exportFormat = null;
        String extension = type;

        for (ExportFormat format : formats) {
            logger.debug(format);
            if (format.formatName().equalsIgnoreCase(type)) {
                exportFormat = format;
                logger.info(ExportClCommand.class.getSimpleName()
                            + ": format is " + format.formatName());
                // if it's a SimpleFileFilter use the defined extension
                FileFilter fileFilter = format.fileFilter();
                if (fileFilter instanceof SimpleFileFilter) {
                    extension = ((SimpleFileFilter) fileFilter).getExtension();
                }
                break; // we found our format
            }
        }

        if (exportFormat == null) {
            response.append("Export format not found.\n");
            return;
        }

        if (accumulate) {
            if (!exportFormat.canExportNto1()) {
                response.append("Export format does not support n-to-1 export.\n");
                return;
            }


            // do a n-t-1 export, i.e. accumulate all nets in one system (e.g. sns) 
            List<Drawing> drawings = new ArrayList<Drawing>();
            for (String filename : files) {
                logger.info(ExportClCommand.class.getSimpleName()
                            + ": add filename to n-to-1 export " + filename);
                File file = new File(filename);
                if (file.exists()) {
                    drawings.add(DrawingFileHelper.loadDrawing(file, this));
                }
            }
            File exportFile;
            if (line.hasOption("o")) {
                exportFile = new File(line.getOptionValue("o"));
            } else {
                exportFile = new File(drawings.get(0).getName() + ".sns");
            }

            exporter.saveDrawings(Collections.enumeration(drawings),
                                  exportFormat, exportFile, this);
            logger.info(ExportClCommand.class.getSimpleName()
                        + ": exported n-to-1  " + exportFile.getAbsolutePath());

        } else {
            if (line.hasOption("o")) {
                exportSingleDrawing(exporter, exportFormat, extension,
                                    files.get(0), line.getOptionValue("o"));
            } else {
                // export each file one by one
                for (String filename : files) {
                    exportSingleDrawing(exporter, exportFormat, extension,
                                        filename, null);
                }
            }
        }
    }

    public List<ExportFormat> flattenedExportFormats() {
        ExportHolder exporter = (ExportHolderImpl) DrawPlugin.getCurrent()
                                                             .getExportHolder();
        ExportFormat[] formats = exporter.allExportFormats();
        Queue<ExportFormat> exportFormatsQueue = new LinkedList<ExportFormat>(Arrays
                                                                              .asList(formats));
        List<ExportFormat> flattenedExportFormats = new ArrayList<ExportFormat>();
        while (!exportFormatsQueue.isEmpty()) {
            ExportFormat format = exportFormatsQueue.poll();
            if (format instanceof ExportFormatMultiAbstract) {
                ExportFormatMultiAbstract multiFormat = (ExportFormatMultiAbstract) format;
                List<ExportFormat> subformats = multiFormat.formats();
                for (ExportFormat subformat : subformats) {
                    exportFormatsQueue.add(subformat);
                }
            } else {
                flattenedExportFormats.add(format);
            }
        }
        Collections.sort(flattenedExportFormats,
                         new Comparator<ExportFormat>() {
                @Override
                public int compare(ExportFormat o1, ExportFormat o2) {
                    return o1.formatName().compareTo(o2.formatName());
                }
            });
        return flattenedExportFormats;
    }

    private void exportSingleDrawing(ExportHolderImpl exporter,
                                     ExportFormat format, String extension,
                                     String filename, String outputFile) {
        logger.info(ExportClCommand.class.getName() + ": filename is "
                    + filename);
        File file = new File(filename);
        if (file.exists()) {
            Drawing drawing = DrawingFileHelper.loadDrawing(file, this);
            logger.info(ExportClCommand.class.getName() + ": drawing is "
                        + drawing.getName());
            File exportFile;
            if (outputFile == null) {
                exportFile = new File(drawing.getFilename().getAbsoluteFile()
                                             .getParentFile(),
                                      drawing.getName() + "." + extension);
            } else {
                exportFile = new File(outputFile);
            }
            logger.info(ExportClCommand.class.getName() + ": path is "
                        + exportFile);
            exporter.saveDrawing(drawing, format, exportFile, this);
        }
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    public String getDescription() {
        return SYNOPSIS
               + " Type ex for a list of supported formats and options.";
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.io.StatusDisplayer#showStatus(java.lang.String)
     */
    public void showStatus(String message) {
        logger.info(ExportClCommand.class.getName() + " Status: " + message);
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        ExportHolderImpl exporter = (ExportHolderImpl) DrawPlugin.getCurrent()
                                                                 .getExportHolder();
        ExportFormat[] formats = exporter.allExportFormats();
        String arguments = "";
        if (formats.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < formats.length; i++) {
                ExportFormat format = formats[i];
                sb.append(format.formatName());
                if (i < formats.length - 1) {
                    sb.append("|");
                }
            }
            sb.append(")");
            sb.append(" ");
            arguments = sb.toString();
        }
        arguments += "[fileNames|-o|-a]*";
        return arguments;
    }

    private void printHelp(PrintStream response, List<ExportFormat> formats,
                           Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        String header = "Exports a drawing.";
        formatter.printHelp(new PrintWriter(response, true),
                            HelpFormatter.DEFAULT_WIDTH,
                            "ex <type> [drawing]+", header, opts,
                            HelpFormatter.DEFAULT_LEFT_PAD,
                            HelpFormatter.DEFAULT_DESC_PAD, null, true);
//        response.append(SYNOPSIS + "\n");
        response.append("List of available formats:\n");
        for (ExportFormat exportFormat : formats) {
            response.append(ExportClCommand.class.getSimpleName() + ": "
                            + exportFormat + "\n");
            if (exportFormat.canExportNto1()) {
                response.append(ExportClCommand.class.getSimpleName() + ": "
                                + exportFormat
                                + " -a file.rnw [morefiles.rnw] (n-to-1)\n");
            }
        }
    }
}