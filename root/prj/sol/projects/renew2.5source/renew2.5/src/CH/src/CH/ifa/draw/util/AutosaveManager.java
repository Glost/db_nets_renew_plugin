package CH.ifa.draw.util;

import CH.ifa.draw.framework.Drawing;

import java.util.Hashtable;


public class AutosaveManager {
    private Hashtable<Drawing, AutosaveTask> autosaveTasks = new Hashtable<Drawing, AutosaveTask>();
    private long interval;
    private AutosaveSaver saver;

    public AutosaveManager(AutosaveSaver saver, long interval) {
        this.saver = saver;
        this.interval = interval;
    }

    /**
     * Include a drawing in the list of drawings to be autosaved.
     * If the drawing is already included in the list, reset
     * its last save time to now.
     *
     * @param drawing the <code>Drawing</code> to be autosaved
     */
    public synchronized void addDrawing(Drawing drawing) {
        AutosaveTask task = autosaveTasks.get(drawing);
        if (task == null) {
            task = new AutosaveTask(saver, drawing, interval);
            autosaveTasks.put(drawing, task);
        }
        task.reset();
    }

    /**
     * Exclude a drawing from the list of drawings to be autosaved.
     * Remove its autosave file, if any.
     *
     * @param drawing the <code>Drawing</code> not to be autosaved
     */
    public synchronized void removeDrawing(Drawing drawing) {
        AutosaveTask task = autosaveTasks.get(drawing);
        if (task != null) {
            task.terminate();
            autosaveTasks.remove(drawing);
        }
    }

    /**
     * Update the name of the autosave file associated to a
     * drawing. This method should be called after a drawing has
     * been renamed. If the name of a drawing changes without
     * calling this method, the old autosave file will still be
     * used, which is allowed.
     *
     * @param drawing the <code>Drawing</code> to be renamed
     */
    public synchronized void renameDrawing(Drawing drawing) {
        removeDrawing(drawing);
        addDrawing(drawing);
    }
}