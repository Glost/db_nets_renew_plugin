package de.renew.navigator.io;

import de.renew.navigator.models.Directory;
import de.renew.navigator.models.DirectoryType;
import de.renew.navigator.models.Leaf;
import de.renew.navigator.models.NavigatorFileTree;
import de.renew.navigator.models.TreeElement;

import java.io.File;
import java.io.FileFilter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-07
 */
public class FilesystemIOLoader implements IOLoader {
    private final FileFilterBuilder fileFilterBuilder;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FilesystemIOLoader.class);

    /**
     * Constructor.
     *
     * @param fileFilterBuilder The filter to select files.
     */
    public FilesystemIOLoader(FileFilterBuilder fileFilterBuilder) {
        this.fileFilterBuilder = fileFilterBuilder;
    }

    @Override
    public TreeElement loadPath(File path, final ProgressListener listener) {
        final TreeElement treeElement = createModel(path);
        if (treeElement instanceof Directory) {
            final FileFilter fileFilter = fileFilterBuilder.buildFileFilter();
            final Directory directory = (Directory) treeElement;
            classifyDirectory(directory, fileFilter);
            directory.setOpened(true);

            // Load internals of the root directory
            loadDirectory(directory, listener, fileFilter);
        }

        return treeElement;
    }

    @Override
    public void refreshPath(TreeElement model, File path,
                            ProgressListener listener) {
        if (model instanceof Directory) {
            if (logger.isDebugEnabled()) {
                logger.debug("refreshing path: " + path);
            }
            final Directory updatedModel = (Directory) loadPath(path, listener);

            compareDirectories((Directory) model, updatedModel, listener);
        }

        // TODO: Compare top level files
    }

    /**
     * Loads a directory into the model.
     *
     * @param parent The parent directory model.
     */
    private void loadDirectory(Directory parent,
                               final ProgressListener listener,
                               FileFilter fileFilter) {
        // Do not take any further actions if worker is cancelled.
        if (listener.isWorkerCancelled()) {
            return;
        }

        final File[] files = listDirectoryContents(parent, fileFilter);

        // Stop if no more sub directories.
        if (files == null) {
            return;
        }

        final int filesCount = files.length;
        int progress = 0;

        // Sort files
        sortFiles(files);

        // Load all subdirectories first.
        for (File file : files) {
            // Load all sub files second.
            if (!file.isDirectory() && file.isFile()) {
                // Add file model child.
                final Leaf fileModel = createFileModel(file);
                parent.add(fileModel);

                // Update the background task.
                listener.progress(++progress, filesCount);

                continue;
            }

            // Load directories recursively
            final Directory directoryModel = createDirectoryModel(file);
            parent.add(directoryModel);
            classifyDirectory(directoryModel, fileFilter);

            // Flatten packages.
            if (directoryModel.getType() == DirectoryType.PACKAGE) {
                File[] sub = file.listFiles(fileFilter);
                while (sub != null && sub.length == 1 && sub[0].isDirectory()) {
                    file = sub[0];
                    directoryModel.setFile(file);
                    final String newName = directoryModel.getName() + "."
                                           + file.getName();
                    directoryModel.setName(newName);
                    sub = file.listFiles(fileFilter);
                }
            }

            final int currentProgress = progress;
            final ProgressListener childListener = new ProgressListener() {
                @Override
                public void progress(float progress, int max) {
                    listener.progress(currentProgress + progress / max,
                                      filesCount);
                }

                @Override
                public boolean isWorkerCancelled() {
                    return listener.isWorkerCancelled();
                }
            };

            // Recursively load its contents.
            loadDirectory(directoryModel, childListener, fileFilter);

            // Update the background task.
            listener.progress(++progress, filesCount);
        }
    }

    private void sortFiles(File[] files) {
        Arrays.sort(files,
                    new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && !o2.isDirectory()) {
                        return -1;
                    }

                    if (!o1.isDirectory() && o2.isDirectory()) {
                        return 1;
                    }

                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
    }

    private void compareDirectories(Directory oldModel, Directory newModel,
                                    ProgressListener listener) {
        // Do not take any further actions if worker is cancelled.
        if (listener.isWorkerCancelled()) {
            return;
        }

        logger.debug("Comparing " + oldModel.getFile().getAbsolutePath());
        LinkedList<TreeElement> childrenToRemove = new LinkedList<TreeElement>();

        for (TreeElement child : oldModel.getChildren()) {
            // Find the corresponding new model for this child.
            final TreeElement newChild = findNewTreeElement(newModel, child);
            if (newChild == null) {
                // Could not find child element? Then remove it
                childrenToRemove.add(child);
                continue;
            }

            // Remove the new child from the queue.
            newModel.remove(newChild);

            // Are both leafs?
            if (child instanceof Leaf) {
                if (!(newChild instanceof Leaf)) {
                    childrenToRemove.add(child);
                    oldModel.add(newChild);
                }

                continue;
            }

            if (child instanceof Directory) {
                // The old and new file are both directories.
                if (newChild instanceof Directory) {
                    compareDirectories((Directory) child, (Directory) newChild,
                                       listener);
                } else {
                    // Remove the old directory child and let the new model stay
                    childrenToRemove.add(child);
                    oldModel.add(newChild);
                }
            }
        }


        // Add new untracked children.
        for (TreeElement child : newModel.getChildren()) {
            logger.debug("New Child: " + child);
            oldModel.add(child);
        }

        // Remove non existing children.
        for (TreeElement child : childrenToRemove) {
            logger.debug("Removed Child: " + child);
            oldModel.remove(child);
        }
    }

    /**
     * Finds the corresponding tree element for a child.
     *
     * @param newModel the new model to search in.
     * @param child the child to reproduce.
     * @return null, if none found.
     */
    private TreeElement findNewTreeElement(Directory newModel, TreeElement child) {
        TreeElement newChild = null;
        for (TreeElement c : newModel.getChildren()) {
            if (child.getFile().equals(c.getFile())) {
                newChild = c;
            }
        }
        return newChild;
    }

    /**
     * Creates a root directory model.
     *
     * @param file The directory to be modeled.
     * @return The model representing the file.
     */
    private TreeElement createModel(File file) {
        if (file.isDirectory()) {
            return createDirectoryModel(file);
        }

        return createFileModel(file);
    }

    /**
     * Creates a directory model.
     *
     * @param dir The directory to be modeled.
     * @return The model representing the dir.
     */
    private Directory createDirectoryModel(File dir) {
        final Directory model = new Directory();

        // Set properties on model.
        model.setOpened(false);
        model.setFile(dir);
        model.setName(dir.getName());
        model.setExcluded(false);

        return model;
    }

    /**
     * Creates a file model.
     *
     * @param file The file to be modeled.
     */
    private Leaf createFileModel(File file) {
        final Leaf model = new Leaf();

        // Set properties on model.
        model.setFile(file);
        model.setName(file.getName());
        model.setExcluded(false);

        return model;
    }

    /**
     * Classifies a directory model.
     *
     * @param directory the directory to classify.
     */
    private void classifyDirectory(Directory directory, FileFilter fileFilter) {
        // Retrieve the parent type.
        final DirectoryType parentType;
        if (directory.getParent() == null
                    || directory.getParent() instanceof NavigatorFileTree) {
            parentType = null;
        } else {
            parentType = ((Directory) directory.getParent()).getType();
        }

        // Packages or source dirs contain packages.
        if (parentType == DirectoryType.SOURCE
                    || parentType == DirectoryType.PACKAGE
                    || parentType == DirectoryType.TEST_SOURCE) {
            directory.setType(DirectoryType.PACKAGE);
            return;
        }

        // Contextualize plugin sub directories.
        if (parentType == DirectoryType.PLUGIN) {
            final String name = directory.getFile().getName();

            if (name.equals("src") || name.equals("resources")) {
                directory.setType(DirectoryType.SOURCE);
                return;
            }
            if (name.equals("test") || name.equals("testsrc")
                        || name.equals("testing")) {
                directory.setType(DirectoryType.TEST_SOURCE);
                return;
            }
            if (name.equals("etc")) {
                directory.setType(DirectoryType.ETC);
                return;
            }

            return;
        }

        // Find plugin.
        File[] files = listDirectoryContents(directory, fileFilter);
        if (parentType == null && containsPluginDirs(files)) {
            directory.setType(DirectoryType.PLUGIN);
        }
    }

    /**
     * Finds out if there are plugin directories existing within given files.
     */
    private boolean containsPluginDirs(File[] files) {
        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.isDirectory() && file.getName().equals("etc")) {
                final File[] etcFiles = file.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.getName().equals("plugin.cfg");
                        }
                    });
                return etcFiles != null && etcFiles.length > 0;
            }
        }

        return false;
    }

    /**
     * @param directory  The directory to load.
     * @param fileFilter The file filter to use.
     * @return null if empty or an array of files.
     */
    private File[] listDirectoryContents(Directory directory,
                                         FileFilter fileFilter) {
        final File dir = directory.getFile();

        // List all contained files.
        return dir.listFiles(fileFilter);
    }
}