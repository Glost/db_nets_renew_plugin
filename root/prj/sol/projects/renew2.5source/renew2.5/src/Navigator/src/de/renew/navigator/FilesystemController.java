package de.renew.navigator;

import de.renew.navigator.io.FileFilterBuilder;
import de.renew.navigator.io.FilesystemIOLoader;
import de.renew.navigator.io.IOLoader;
import de.renew.navigator.io.ProgressListener;
import de.renew.navigator.models.BackgroundTask;
import de.renew.navigator.models.TreeElement;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import de.renew.util.PathEntry;
import de.renew.util.StringUtil;

import java.io.File;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-25
 */
public class FilesystemController extends NavigatorController {
    public static final String FILES_AT_STARTUP = "de.renew.navigator"
                                                  + ".filesAtStartup";
    public static final String WORKSPACE_LOCATION = "de.renew.navigator"
                                                    + ".workspace";
    public static final String NET_PATH = "de.renew.netPath";
    private final IOLoader ioLoader;
    private final FileFilterBuilder fileFilterBuilder;
    private final HashSet<SwingWorker<TreeElement, Void>> activeWorkers;

    /**
     * @param plugin the plugin containing the controller
     */
    public FilesystemController(NavigatorPlugin plugin,
                                FileFilterBuilder fileFilterBuilder) {
        super(plugin);
        this.fileFilterBuilder = fileFilterBuilder;
        this.ioLoader = new FilesystemIOLoader(fileFilterBuilder);
        activeWorkers = new HashSet<SwingWorker<TreeElement, Void>>();
    }

    /**
     * Finds out, if a file should be opened externally.
     *
     * @param file the file to check
     * @return <code>true</code>, if the file should be opened externally
     */
    public boolean isExternallyOpenedFile(File file) {
        return fileFilterBuilder.isExternallyOpenedFile(file);
    }

    /**
     * Loads a root directory into a model by giving a filesystem directory.
     *
     * @param rootDirectories filesystem directories to scan
     */
    public void loadRootDirectories(Collection<File> rootDirectories) {
        for (final File rootDir : rootDirectories) {
            addLoadingTask(new LoadingTask() {
                    @Override
                    public String getName() {
                        return String.format("Loading %s ...", rootDir.getName());
                    }

                    @Override
                    public TreeElement performAction(ProgressListener listener) {
                        return ioLoader.loadPath(rootDir, listener);
                    }
                });
        }
    }

    /**
     * This method loads predefined directories from plugin properties,
     * filtered by the CombinationFileFilter from CH.ifa.draw.IOHelper.
     * Each file and directory gets added by the addFile()
     * method. The retrieved CombinationFileFilter will applied to each of these
     * files.
     */
    public void loadFromProperties() {
        cancelAllLoadingTasks();
        model.clear();

        String filesAtStartup;
        String workspaceLocation;

        // Load pro
        final PluginProperties properties = plugin.getProperties();
        filesAtStartup = properties.getProperty(FILES_AT_STARTUP);
        workspaceLocation = properties.getProperty(WORKSPACE_LOCATION);

        // If no workspace is set in the properties, take it from system props.
        if (workspaceLocation == null) {
            workspaceLocation = System.getProperty("user.dir");
        }

        // If no startup files are set in the properties, take workspace dir.
        if (filesAtStartup == null) {
            filesAtStartup = workspaceLocation;
        }


        // Collect all directories to add.
        LinkedList<File> files = new LinkedList<File>();
        for (String fileName : filesAtStartup.split(";")) {
            // Prepend workspace location if relative path.
            if (!fileName.startsWith("/")) {
                fileName = workspaceLocation + "/" + fileName;
            }

            File dir = new File(fileName);
            if (dir.exists() && dir.isDirectory()) {
                files.add(dir);
            }
        }

        loadRootDirectories(files);
    }

    /**
     * Open the NetPaths, set by simulator plugin properties.
     */
    public void loadFromNetPaths() {
        final IPlugin simulator = PluginManager.getInstance()
                                               .getPluginByName("Renew Simulator");

        if (simulator == null) {
            throw new RuntimeException("Could not find Simulator plugin.");
        }

        final String property = simulator.getProperties().getProperty(NET_PATH);

        if (property == null) {
            throw new RuntimeException("Net path property is not set.");
        }

        final String[] paths = StringUtil.splitPaths(property);
        final PathEntry[] entries = StringUtil.canonizePaths(paths);

        final List<File> files = new LinkedList<File>();
        for (PathEntry entry : entries) {
            if (entry.isClasspathRelative) {
                continue;
            }

            files.add(new File(entry.path));
        }

        loadRootDirectories(files);
    }

    /**
     * Takes the model and refreshes all
     */
    public void refreshPaths() {
        cancelAllLoadingTasks();

        for (final TreeElement treeRoot : model.getTreeRoots()) {
            addLoadingTask(new LoadingTask() {
                    @Override
                    public String getName() {
                        return String.format("Refreshing %s ...",
                                             treeRoot.getName());
                    }

                    @Override
                    public TreeElement performAction(ProgressListener listener) {
                        ioLoader.refreshPath(treeRoot, treeRoot.getFile(),
                                             listener);
                        return treeRoot;
                    }
                });
        }
    }

    public javax.swing.filechooser.FileFilter getFileFilter() {
        return fileFilterBuilder.buildFileFilter();
    }

    /**
     * Creates a swing worker executing the
     * @param loadingTask
     */
    private void addLoadingTask(final LoadingTask loadingTask) {
        final FilesystemController ctrl = this;
        final BackgroundTask backgroundTask = new BackgroundTask(loadingTask
                                                  .getName());
        final SwingWorker<TreeElement, Void> worker;

        worker = new SwingWorker<TreeElement, Void>() {
                @Override
                protected TreeElement doInBackground()
                        throws Exception {
                    // Update the background task while progressing.
                    final ProgressListener listener = new ProgressListener() {
                        @Override
                        public void progress(float progress, int max) {
                            backgroundTask.setCurrent(progress / max);
                            backgroundTask.notifyObservers();
                        }

                        @Override
                        public boolean isWorkerCancelled() {
                            return isCancelled();
                        }
                    };

                    return loadingTask.performAction(listener);
                }

                @Override
                protected void done() {
                    model.removeBackgroundTask(backgroundTask);
                    if (!isCancelled()) {
                        activeWorkers.remove(this);
                        try {
                            final TreeElement treeElement = get();
                            if (!model.contains(treeElement)) {
                                model.add(treeElement);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    model.notifyObservers(ctrl);
                }
            };
        activeWorkers.add(worker);
        worker.execute();

        // Set the cancel action on the background task.
        backgroundTask.setCancelAction(new Runnable() {
                @Override
                public void run() {
                    activeWorkers.remove(worker);
                    worker.cancel(true);
                }
            });

        model.addBackgroundTask(backgroundTask);
        model.notifyObservers(this);
    }

    private void cancelAllLoadingTasks() {
        for (Iterator<SwingWorker<TreeElement, Void>> i = activeWorkers.iterator();
                     i.hasNext();) {
            SwingWorker<TreeElement, Void> worker = i.next();
            i.remove();
            worker.cancel(true);
        }
    }

    interface LoadingTask {
        String getName();

        TreeElement performAction(final ProgressListener listener);
    }
}