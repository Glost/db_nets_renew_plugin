package CH.ifa.draw.util;

import CH.ifa.draw.framework.Drawing;

import de.renew.util.Scheduler;
import de.renew.util.StringUtil;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class AutosaveTask implements Runnable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(AutosaveTask.class);
    private Drawing drawing;
    private long lastSaved;
    private long interval;

    /**
     * <code>autosaveFilename</code> is null, until the first
     * time that the drawing was saved. Afterwards, it keeps track
     * of the name of the autosave file.
     */
    private File autosaveFilename = null;
    private AutosaveSaver saver;

    public AutosaveTask(AutosaveSaver saver, Drawing drawing, long interval) {
        this.saver = saver;
        this.drawing = drawing;
        this.interval = interval;
        reset();

        Scheduler.instance().executeIn(this, interval);
    }

    private void touch(File filename) {
        try {
            FileOutputStream stream = new FileOutputStream(filename);
            stream.close();
        } catch (IOException e) {
            // Never try to save again.
            interval = 0;
            logger.debug("Autosave of drawing " + drawing.getName()
                         + " cancelled: " + e);
        }
    }

    public synchronized void reset() {
        lastSaved = System.currentTimeMillis();
    }

    /**
     * Terminate the autosave process, removing the autosave file,
     * if it exists.
     */
    public synchronized void terminate() {
        interval = 0;
        Scheduler.instance().cancel(this);
        if (autosaveFilename != null) {
            if (autosaveFilename.exists()) {
                logger.debug("Deleting autosave copy " + autosaveFilename
                             + "...");
                autosaveFilename.delete();
            }
        }
    }

    public synchronized void run() {
        if (interval > 0) {
            // Ok, this autosave task is still associated to an
            // open drawing.
            long now = System.currentTimeMillis();
            long remainingDelay = lastSaved + interval - now;
            if (remainingDelay <= 0) {
                if (drawing.isModified()) {
                    if (autosaveFilename == null) {
                        File filename = drawing.getFilename();
                        if (filename != null) {
                            autosaveFilename = new File(StringUtil
                                                   .stripFilenameExtension(filename
                                                                           .getPath())
                                                        + ".aut");
                        }
                        if (autosaveFilename != null
                                    && !autosaveFilename.exists()) {
                            touch(autosaveFilename);
                        } else {
                            autosaveFilename = null;
                            for (int i = 0;
                                         i < 25 && autosaveFilename == null
                                         && interval > 0; i++) {
                                int autosaveNumber = (int) Math.floor(Math
                                                         .random() * 100000);
                                File testFilename = new File("rnw"
                                                             + autosaveNumber
                                                             + ".aut");
                                try {
                                    if (testFilename.createNewFile()) {
                                        // Remember the filename.
                                        autosaveFilename = testFilename;
                                    }
                                } catch (IOException e) {
                                    // An error occurred. Do not save again.
                                    interval = 0;
                                    logger.debug("Autosave of drawing "
                                                 + drawing.getName()
                                                 + " cancelled: " + e);
                                }
                            }
                        }
                    }

                    if ((interval > 0) && autosaveFilename != null) {
                        Rectangle rect = drawing.displayBox();
                        Dimension size = new Dimension(rect.width + rect.x
                                                       + 200,
                                                       rect.height + rect.y
                                                       + 200);
                        try {
                            logger.debug("Autosaving drawing "
                                         + drawing.getName() + " to "
                                         + autosaveFilename + "...");
                            saver.saveAutosaveFile(drawing, autosaveFilename,
                                                   new Point(), size);
                        } catch (IOException e) {
                            // An error occurred. Do not save again.
                            interval = 0;
                            logger.debug("Autosave of drawing "
                                         + drawing.getName() + " cancelled: "
                                         + e);
                        }
                    }
                }


                // We just tried to save. Reset the clock.
                reset();
            }
            if (interval > 0) {
                // Another save is desired.
                Scheduler.instance().executeAt(this, lastSaved + interval);
            }
        }
    }
}