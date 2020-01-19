/*
 * Created on 06.02.2004 by Joern Schumacher
 *
 */
package CH.ifa.draw;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingTypeManager;

import CH.ifa.draw.io.CombinationFileFilter;
import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.ImportHolder;
import CH.ifa.draw.io.PositionedDrawing;
import CH.ifa.draw.io.SimpleFileFilter;
import CH.ifa.draw.io.importFormats.ImportFormat;
import CH.ifa.draw.io.importFormats.ImportFormatMulti;

import de.renew.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


/**
 * This class was designed to extract load/save operations from the
 * DrawPlugin class. This was meant to do two things:
 * <ul>
 * <li>to slim down the DrawPlugin class by providing a delegation target.</li>
 * <li>to facilitate the creation of a I/O plugin.</li>
 * </ul>
 *
 * @author Joern Schumacher
 *
 */
public class IOHelper {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(IOHelper.class);
    private static IOHelper _instance;
    private File lastPath = new File(System.getProperty("user.dir"));
    private SimpleFileFilter lastSelectedFileFilter;

    private IOHelper() {
        lastSelectedFileFilter = DrawingTypeManager.getInstance()
                                                   .getDefaultFileFilter();
    }

    public static IOHelper getInstance() {
        if (_instance == null) {
            _instance = new IOHelper();
        }
        return _instance;
    }

    /**
     * @return last path
     */
    public File getLastPath() {
        return lastPath;
    }

    /**
     * Opens the JFileChooser dialog to get the savePath
     *
     * @param file initial file name/path where to start the dialog, if it is null first tries to get the filename of the current drawing. If that does not work the lastpath is used.
     * @param ff file filter for the JFileChooser. if null, null is returned
     * @return returns the chosen file or null
     */
    public File getSavePath(File file, SimpleFileFilter ff) {
        if (DrawPlugin.getGui() == null) {
            logger.debug("could not open save dialog: no gui!");
        }
        JFileChooser dialog = new JFileChooser();
        if (ff == null) {
            return null;
        }
        if (file == null) {
            file = DrawPlugin.getGui().drawing().getFilename();
        }
        if (file == null) {
            file = lastPath;
        }


        // prepare the dialog
        dialog.setAcceptAllFileFilterUsed(false);
        dialog.setFileFilter(ff);


        // change this
        dialog.setCurrentDirectory(getCurrentDirectory(file));
        file = new File(StringUtil.extendFileNameBy(file.getPath(),
                                                    ff.getExtension()));
        dialog.setSelectedFile(file);
        int check = dialog.showSaveDialog(DrawPlugin.getGui().getFrame());

        if (check == JFileChooser.APPROVE_OPTION) {
            lastPath = dialog.getCurrentDirectory();
            return dialog.getSelectedFile();
        }
        return null;
    }

    private Collection<SimpleFileFilter> getInputFileFilters() {
        List<SimpleFileFilter> fileFilters = new LinkedList<SimpleFileFilter>();
        Enumeration<String> enumeration = getDrawingTypeManager()
                                              .getDrawingTypes().keys();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            fileFilters.add(getDrawingTypeManager().getFilter(name));
        }
        return fileFilters;
    }

    /**
     * @return file filter for all known input file types
     */
    public CombinationFileFilter getFileFilter() {
        CombinationFileFilter result = new CombinationFileFilter("All known input file types");
        result.addAll(getInputFileFilters());
        result.setPreferedFileFilter(lastSelectedFileFilter);
        return result;
    }

    /**
     * @return file filter for all known input file types including import formats
     */
    public CombinationFileFilter getFileFilterWithImportFormats() {
        CombinationFileFilter result = new CombinationFileFilter("All known input file types including import formats");

        // add input file types
        result.addAll(getInputFileFilters());

        // add import format file types
        ImportHolder importHolder = DrawPlugin.getCurrent().getImportHolder();
        Queue<ImportFormat> importFormats = new LinkedList<ImportFormat>();
        importFormats.addAll(Arrays.asList(importHolder.allImportFormats()));
        while (!importFormats.isEmpty()) {
            ImportFormat format = importFormats.remove();
            if (format instanceof ImportFormatMulti) {
                ImportFormatMulti multiFormat = (ImportFormatMulti) format;
                importFormats.addAll(Arrays.asList(multiFormat.allImportFormats()));
            } else {
                final javax.swing.filechooser.FileFilter filter = format
                                                                      .fileFilter();
                if (filter != null && filter instanceof SimpleFileFilter) {
                    result.add((SimpleFileFilter) filter);
                }
            }
        }

        result.setPreferedFileFilter(lastSelectedFileFilter);
        return result;
    }

    protected DrawingTypeManager getDrawingTypeManager() {
        return DrawingTypeManager.getInstance();
    }


    /**
     * Opens a file dialog according to the parameters and returns the
     * selected/entered files.
     *
     * @param file     The default file name of the drawing to be proposed
     *                 to the user. The file name is checked and completed
     *                 to end with one of the default Extensions before.
     *                 If the file name is prefixed with a directory, the
     *                 dialog initially switches its actual directory to
     *                 this prefix. In the other case, the actual directory
     *                 is set to the last directory used for files with the
     *                 file's extension.
     * @param ff       The array of FileFilter that determins the selectable file
     *                 types in the JFileChooser.
     * @return         The file name that was selected or entered by the
     *                 user. Returns <code>null</code>, if the dialog was
     *                 cancelled.
     **/
    public File getSavePath(File file, SimpleFileFilter[] ff) {
        if (DrawPlugin.getGui() == null) {
            logger.debug("could not open save dialog: no gui!");
        }
        JFileChooser dialog = new JFileChooser();
        if (ff == null) {
            return null;
        }
        if (file == null) {
            file = DrawPlugin.getGui().drawing().getFilename();
        }
        if (file == null) {
            file = lastPath;
        }


        // prepare the dialog
        dialog.setAcceptAllFileFilterUsed(false);
        for (int pos = 0; pos < ff.length; pos++) {
            dialog.addChoosableFileFilter(ff[pos]);
        }
        dialog.setFileFilter(ff[0]);

        // change this
        dialog.setCurrentDirectory(getCurrentDirectory(file));

        file = new File(StringUtil.extendFileNameBy(file.getPath(),
                                                    ff[0].getExtension()));

        dialog.setSelectedFile(file);
        int check = dialog.showSaveDialog(DrawPlugin.getGui().getFrame());

        if (check == JFileChooser.APPROVE_OPTION) {
            lastPath = dialog.getCurrentDirectory();
            return DrawingFileHelper.checkAndAddExtension(dialog.getSelectedFile(),
                                                          (SimpleFileFilter) dialog
                                                          .getFileFilter());
        }
        return null;
    }

    /**
     * Returns the directory to propose for the next file open or save
     * operation.
     *
     * @param file  a file to get the directory from.
     *              May be <code>null</code>.
     * @return the directory where the given file resides, if it exists.
     *         Returns the last directory used in any file dialog,
     *         otherwise.
     **/
    public File getCurrentDirectory(File file) {
        File f;
        if (file != null && (f = file.getParentFile()) != null && f.exists()) {
            return f;
        }
        return lastPath;
    }

    /**
     * Opens a file dialog according to the parameters
     * and returns the selected/entered files.
     * @param filename     File name to propose to the user.
     *                     The file name is checked and completed
     *                     to end with one of the default Extensions.
     *                     If the file name is prefixed with a
     *                     directory, the dialog initially switches
     *                     its actual directory to this prefix.
     *                     In the other case, the actual directory is
     *                     set to the last directory used for files
     *                     with the file's extension.
     * @param ff           The FileFilter that determins the
     *                     selectable file types in the JFileChooser.
     *                     Usually a CombinationFileFilter to be able to choose
     *                     a specific file type or a SimpleFileFilter if the
     *                     file type shall be fixed.
     * @param multiSelection Multiselection can be choosen.
     *                     When turned off the method returns an array
     *                     of File with only one element.
     * @return             Array of <code>File</code> objects or <code>null</code>, if
     *                     the dialog was cancelled.
     *                     The files are <i>not</i> checked and completed to
     *                     end with one of the default extensions
     *                     before they get returned.
     * @see #getLoadPath
     * @see #getSavePath
     * @see javax.swing.JFileChooser
     **/
    public File[] getLoadPath(File filename, FileFilter ff,
                              boolean multiSelection) {
        File currentDir = getCurrentDirectory(filename);

        JFileChooser dialog = new JFileChooser(currentDir);

        // do not provide the *
        dialog.setAcceptAllFileFilterUsed(false);
        dialog.setMultiSelectionEnabled(multiSelection);


        // but add all the Filefilter that are available
        //dialog.setCurrentDirectory(new File("projects"));
        if (ff != null) {
            // SimpleFileFilter can be combined to a new one with a new description add 
            // if so add also all the internal SimpleFilFilter
            if (ff instanceof CombinationFileFilter) {
                CombinationFileFilter cff = (CombinationFileFilter) ff;

                // Setting the current file filter first avoids bug #6189.
                dialog.setFileFilter(cff.getPreferedFileFilter());

                Iterator<SimpleFileFilter> it = cff.getFileFilters().iterator();
                while (it.hasNext()) {
                    dialog.addChoosableFileFilter(it.next());
                }
            } else {
                dialog.addChoosableFileFilter(ff);
            }
        } else {
            // not sure what to do so add * // this should not happen
            dialog.setAcceptAllFileFilterUsed(true);
        }
        if (filename != null) {
            dialog.setCurrentDirectory(filename);
        }
        int check;
        check = dialog.showOpenDialog(DrawPlugin.getGui().getFrame());
        if (check == JFileChooser.APPROVE_OPTION) {
            lastPath = dialog.getCurrentDirectory();
            FileFilter simpleff = dialog.getFileFilter();
            if (simpleff instanceof SimpleFileFilter
                        && DrawingTypeManager.getInstance().getDrawingTypes()
                                                     .values().contains(simpleff)) {
                lastSelectedFileFilter = (SimpleFileFilter) simpleff;
            }
            if (multiSelection) {
                return dialog.getSelectedFiles();
            } else {
                File[] oneFile = { dialog.getSelectedFile() };
                return oneFile;
            }
        }
        return null;
    }

    /**
     * Opens a file dialog according to the parameters
     * and returns the selected/entered file. The user will not be able to
     * select more than one file.
     *
     * @param filename     a file name to propose to the user.
     *                     The file name is checked and completed
     *                     to end with one of the default Extensions.
     *                     If the file name is prefixed with a
     *                     directory, the dialog initially switches
     *                     its actual directory to this prefix.
     *                     In the other case, the actual directory is
     *                     set to the last directory used for files
     *                     with the file's extension.
     * @param ff           The FileFilter that determins the
     *                     selectable file types in the JFileChooser.
     * @return             The chosen <code>File</code> object. <br>
     *                     Returns <code>null</code>, if the dialog was cancelled.
     *                     The file name is <i>not</i> checked and completed to
     *                     end with one of the default extensions
     *                     before it gets returned.
     * @see #getLoadPath(File, FileFilter)
     * @see JFileChooser
     **/
    public File getLoadPath(File filename, FileFilter ff) {
        File[] files = getLoadPath(filename, ff, false);
        if (files == null) {
            return null;
        }
        return files[0];
    }

    /**
      * Returns the files to be imported.
      * @param filter the FileFilter which is used
      * @return File[] an array of files to be imported.
      */
    public File[] getLoadPath(FileFilter filter) {
        File[] result = null;
        result = getLoadPath(null, filter, true);
        if (result == null) {
            result = new File[0];
        }
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
      * Returns the files to be imported.
      * @param filters a array of FileFilters that are used.
      * @return File[] an array of files to be imported.
      */
    public File[] getLoadPath(FileFilter[] filters) {
        File[] result = null;
        CombinationFileFilter filter = new CombinationFileFilter("All");
        for (int pos = 0; pos < filters.length; pos++) {
            filter.add((SimpleFileFilter) filters[pos]);
        }
        result = getLoadPath(filter);
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * TODO: JSC: put into DrawApplication
     * Loads a drawing from the given URL and opens it in the editor.
     *
     * @param url  the location where to retrieve the drawing.
     **/
    public synchronized void loadAndOpenDrawing(URL url) {
        PositionedDrawing posDrawing = DrawingFileHelper.loadPositionedDrawing(url,
                                                                               null);
        if (posDrawing != null) {
            DrawPlugin.getGui().openDrawing(posDrawing);
        }
    }

    /**
     * Loads a drawing from the given stream with the given name.
     *
     * @param stream stream containing the drawing
     * @param name name of the drawing
     * @throws FileNotFoundException file not found
     * @throws IOException any other IO exception
     */
    public synchronized void loadAndOpenDrawing(InputStream stream, String name)
            throws FileNotFoundException, IOException {
        PositionedDrawing posDrawing = DrawingFileHelper.loadPositionedDrawing(stream,
                                                                               name);
        if (posDrawing != null) {
            try {
                DrawApplication gui = DrawPlugin.getGui();
                if (gui != null) {
                    gui.openDrawing(posDrawing);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads a positioned drawing into a view container
     *
     * @param drawing drawing which should be loaded
     */
    public synchronized void loadInViewContainer(PositionedDrawing drawing) {
        DrawApplication gui = DrawPlugin.getGui();
        if (gui != null) {
            gui.openDrawing(drawing);
        }
    }

    /**
     * TODO: JSC: put into DrawApplication
     * Loads a drawing from the given file and opens it in the editor.
     *
     * @param file  the file name where to retrieve the drawing.
     **/
    public synchronized void loadAndOpenDrawing(File file) {
        PositionedDrawing posDrawing = DrawingFileHelper.loadPositionedDrawing(file,
                                                                               null);
        DrawPlugin.getGui().openDrawing(posDrawing);
        if (posDrawing != null) {
            posDrawing.getDrawing().init();
        }
    }

    /**
     * Queries the user to choose a file name to save the drawing into.
     *
     * @param file     the previous file name of the drawing or another file
     *                 name to propose to the user.
     * @param filter   an array of <code>SimpleFileFilter</code>s denoting
     *                 the available file formats and their extensions.
     * @param drawing  the <code>Drawing</code> to save.
     * @return  the file name chosen by the user.
     *          Returns <code>null</code> if the action was cancelled by
     *          the user.
     **/
    public File getSaveFile(File file, SimpleFileFilter[] filter,
                            Drawing drawing) {
        File result = null;
        if (file == null) {
            file = drawing.getFilename();
            if (file == null) {
                file = new File(getLastPath(), drawing.getName());
                file = DrawingFileHelper.checkAndAddExtension(file, filter[0]);
            }
        }
        File path = getSavePath(file, filter);
        if (path != null) {
            // If the file exists already, ask for permission to overwrite
            if (path.exists()) {
                DrawApplication app = DrawPlugin.getGui();

                // new Swing confirm dialog
                int answer = JOptionPane.showConfirmDialog(app.getViewContainer(drawing)
                                                              .getFrame(),
                                                           "The file \"" + path
                                                           + "\""
                                                           + " does already exist."
                                                           + "\nDo you want do proceed?",
                                                           "Renew: Confirm overwrite.",
                                                           JOptionPane.YES_NO_OPTION);

                if (answer == JOptionPane.OK_OPTION) {
                    result = path;
                }
            } else {
                result = path;
            }
        }
        return result;
    }

    /**
     * sets the last path
     * @param lp new last path
     */
    public void setLastPath(File lp) {
        this.lastPath = lp;
    }
}