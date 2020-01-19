package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


/**
 * A DropTargetListener that only reacts on dropped files. The files are
 * filtered by a given file filter. The file filter can be changed in a
 * subclass. The handling of the files has to be implemented in a subclass.
 *
 * @author 6hauster
 */
public abstract class AbstractFileDragDropListener implements DropTargetListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(AbstractFileDragDropListener.class);

    /**
     * Get the file filter which is used to filter the dropped files.
     *
     * @return the file filter
     */
    protected FileFilter getFileFilter() {
        return DrawPlugin.getCurrent().getIOHelper()
                         .getFileFilterWithImportFormats();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
     */
    @Override
    public void drop(DropTargetDropEvent event) {
        // Accept copy drops
        event.acceptDrop(DnDConstants.ACTION_COPY);

        // Get the transfer which can provide the dropped item data
        Transferable transferable = event.getTransferable();

        // Get the data formats of the dropped item
        DataFlavor[] flavors = transferable.getTransferDataFlavors();

        // Loop through the flavors
        for (DataFlavor flavor : flavors) {
            try {
                // If the drop items are files
                if (flavor.isFlavorJavaFileListType()) {
                    // Get all of the dropped files
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) transferable.getTransferData(flavor);

                    // do an empty loop to have the class cast exception close to the cast
                    for (@SuppressWarnings("unused")
                    File f : files) {
                        ;
                    }


                    // filter the files
                    List<File> filteredFiles = new ArrayList<File>();
                    FileFilter fileFilter = getFileFilter();

                    for (File file : files) {
                        if (fileFilter.accept(file)) {
                            filteredFiles.add(file);
                        }
                    }

                    // handle the filtered files at once
                    File[] filesArray = filteredFiles.toArray(new File[filteredFiles
                                                                       .size()]);
                    handleFiles(filesArray, event.getLocation());
                }
            } catch (IOException e) {
                // Print out the error stack
                logger.error(e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(AbstractFileDragDropListener.class
                        .getSimpleName() + ": " + e);
                }
            } catch (UnsupportedFlavorException e) {
                // Print out the error stack
                logger.error(e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(AbstractFileDragDropListener.class
                        .getSimpleName() + ": " + e);
                }
            }
        }

        // Inform that the drop is complete
        event.dropComplete(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent
     * )
     */
    @Override
    public void dragEnter(DropTargetDragEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
     */
    @Override
    public void dragExit(DropTargetEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent
     * )
     */
    @Override
    public void dragOver(DropTargetDragEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.
     * DropTargetDragEvent)
     */
    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
    }

    /**
     * Handle the dropped files.
     *
     * @param files
     *            the files as array
     */
    protected abstract void handleFiles(File[] files, Point loc);
}