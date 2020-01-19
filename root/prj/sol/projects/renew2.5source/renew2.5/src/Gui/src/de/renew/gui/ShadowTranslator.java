package de.renew.gui;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.io.RNWFileFilter;

import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowNetLoader;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.SyntaxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.util.Vector;


/**
 * Converts Renew drawings (<code>.rnw</code>) to shadow nets
 * (<code>.sns</code>).
 * <p>
 * </p>
 * ShadowTranslator.java
 * Created: Mon Jan 21  2002
 *
 * @author Michael Duvigneau
 **/
public class ShadowTranslator {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ShadowTranslator.class);
    private static final StatusDisplayer sd = new StatusDisplayer() {
        public void showStatus(String string) {
            System.err.println(string);
        }
    };

    /**
     * Execution entry point from the command line.
     **/
    public static void main(String[] args) {
        //disable gui mode of renew
        System.setProperty("de.renew.noGraphics", "true");

        //minimal logging configuration
        Logger rootLogger = Logger.getRootLogger();
        ConsoleAppender appender = new ConsoleAppender(new SimpleLayout(),
                                                       ConsoleAppender.SYSTEM_OUT);
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.DEBUG);

        if (args.length == 0) {
            logger.error("Usage: java de.renew.gui.ShadowTranslator file.rnw ...");
            logger.info("Converts Renew drawings (.rnw) to shadow nets (.sns).");
            logger.info("No syntax check or compilation is done, just conversion.");
            System.exit(1);
        }

        RNWFileFilter ff = new RNWFileFilter();
        Vector<File> names = new Vector<File>(args.length);
        for (int i = 0; i < args.length; ++i) {
            names.add(DrawingFileHelper.checkAndAddExtension(new File(args[i]),
                                                             ff));
        }
        CPNDrawing[] drawings = readDrawings(names.toArray(new File[names.size()]));
        writeShadows(drawings);
        System.exit(0);
    }

    public static CPNDrawing[] readDrawings(File[] files) {
        Vector<Drawing> drawings = new Vector<Drawing>(files.length);
        for (int i = 0; i < files.length; i++) {
            try {
                Drawing drawing = DrawingFileHelper.loadDrawing(files[i], sd);
                if (drawing == null) {
                    // Probably there already has been an error message...
                } else if (drawing instanceof CPNDrawing) {
                    drawings.add(drawing);
                } else {
                    logger.warn(drawing.getName()
                                + " is not a CPN drawing - ignored.");
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return drawings.toArray(new CPNDrawing[drawings.size()]);
    }

    public static void writeShadows(CPNDrawing[] drawings) {
        try {
            writeShadows(null, false, drawings);
        } catch (SyntaxException e) {
            // THIS SHOULD NOT HAPPEN!
        }
    }

    public static void writeShadows(ShadowCompilerFactory compilerFactory,
                                    boolean compile, CPNDrawing[] drawings)
            throws SyntaxException {
        writeShadows(compilerFactory, compile, drawings, null);
    }

    public static void writeShadows(ShadowCompilerFactory compilerFactory,
                                    boolean compile, CPNDrawing[] drawings,
                                    File[] destsnsfiles)
            throws SyntaxException {
        writeShadows(compilerFactory, compile, drawings, destsnsfiles, null);
    }

    public static void writeShadows(ShadowCompilerFactory compilerFactory,
                                    boolean compile, CPNDrawing[] drawings,
                                    File[] destsnsfiles,
                                    ShadowNetLoader netloader)
            throws SyntaxException {
        if (compile) {
            createGlobalSns(compilerFactory, compile, drawings, netloader);
        }

        int count = 0;

        // Build new shadows of all nets, one net per net system.
        for (int i = 0; i < drawings.length; i++) {
            CPNDrawing currentDrawing = drawings[i];
            ShadowNetSystem netSystem = new ShadowNetSystem(compilerFactory);
            currentDrawing.buildShadow(netSystem);

            String name = currentDrawing.getName();
            File path;
            if (destsnsfiles != null) {
                path = destsnsfiles[i];
            } else {
                path = currentDrawing.getFilename();
                if (path != null) {
                    try {
                        path = path.getCanonicalFile();
                        path = new File(path.getParent(), name + ".sns");
                    } catch (IOException e) {
                        logger.error("Cannot compute sns filename for net "
                                     + name + ".", e);
                        path = null;
                    }
                }
            }
            if (path == null) {
                logger.warn("Skipping net " + name + " (no filename).");
            } else {
                try {
                    logger.debug("Exporting net " + name + " to " + path
                                 + "...");

                    FileOutputStream stream = new FileOutputStream(path);
                    ObjectOutput output = new ObjectOutputStream(stream);
                    output.writeObject(netSystem);
                    output.close();
                    count++;
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            currentDrawing.discardShadow();
        }
        logger.info("Exported " + count + " shadow net"
                    + (count == 1 ? "" : "s") + ".");
    }

    private static ShadowNetSystem createGlobalSns(ShadowCompilerFactory compilerFactory,
                                                   boolean compile,
                                                   CPNDrawing[] drawings,
                                                   ShadowNetLoader netloader)
            throws SyntaxException {
        logger.debug("Creating shadow net system with compiler "
                     + compilerFactory + " and net loader " + netloader + ".");
        ShadowNetSystem netSystem = new ShadowNetSystem(compilerFactory,
                                                        netloader);
        for (int i = 0; i < drawings.length; i++) {
            CPNDrawing currentDrawing = drawings[i];
            currentDrawing.buildShadow(netSystem);
        }

        if (compile) {
            netSystem.compile();
        }
        return netSystem;
    }

    public static void writeSingleShadow(CPNDrawing[] drawings, File fileName) {
        try {
            writeSingleShadow(null, false, drawings, fileName);
        } catch (SyntaxException e) {
            // THIS SHOULD NOT HAPPEN!
        }
    }

    public static void writeSingleShadow(ShadowCompilerFactory compilerFactory,
                                         boolean compile,
                                         CPNDrawing[] drawings, File fileName)
            throws SyntaxException {
        // Build new shadows of all nets, put them into one net system.
        // We do not supply a net loader because the system is meant to
        // be self-contained.
        ShadowNetSystem netSystem = createGlobalSns(compilerFactory, compile,
                                                    drawings, null);

        if (fileName == null) {
            logger.error("Cannot write output sns, no filename given.");
        } else {
            try {
                logger.debug("Exporting all nets to " + fileName + "...");
                FileOutputStream stream = new FileOutputStream(fileName);
                ObjectOutput output = new ObjectOutputStream(stream);
                output.writeObject(netSystem);
                output.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        logger.info("Exported " + drawings.length + " shadow net"
                    + (drawings.length == 1 ? "" : "s") + ".");
    }
}