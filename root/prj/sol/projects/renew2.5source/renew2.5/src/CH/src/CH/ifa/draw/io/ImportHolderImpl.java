package CH.ifa.draw.io;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.importFormats.ImportFormat;
import CH.ifa.draw.io.importFormats.ImportFormatCommand;
import CH.ifa.draw.io.importFormats.ImportFormatMulti;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;

import java.awt.Component;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


/**
 *
 */
public class ImportHolderImpl implements ImportHolder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ImportHolderImpl.class);

    //	Attributes
    // All ImportFormats that have been added.
    private List<ImportFormat> _importFormats;

    // The ImportMenu
    private CommandMenu _importMenu;

    /**
     *
     */
    public ImportHolderImpl() {
        super();
        setImportMenu(new CommandMenu("Import"));
        initImportFormatHolder();
    }

    public JMenu getImportMenu() {
        return _importMenu;
    }

    //	---------------------------------------------------------------------
    // Implementation of the ImportHolder Interface
    // ---------------------------------------------------------------------
    // Methods


    /**
     * Initiation of the ImportHolder.
     */
    private void initImportFormatHolder() {
        setImportFormats(new LinkedList<ImportFormat>());
        buildImportAll();
        loadDefaultImportFormats();
    }

    /**
     * List of all added ImportFormats.
     * @ensure result != null.
     * @return List, list of all sdded ImportFormats.
     */
    private List<ImportFormat> importFormats() {
        List<ImportFormat> result = null;
        result = _importFormats;
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * Sets the List of the ImportFormats
     * @require importFormat != null;
     * @ensure importFormats() != null.
     * @ensure importFormats().equals(importFormats)
     * @param importFormats the value to be set.
     */
    private void setImportFormats(List<ImportFormat> importFormats) {
        _importFormats = importFormats;
        assert (importFormats() != null) : "Failure in GuiPlugin: importFormats == null";
        assert (importFormats().equals(importFormats)) : "Failure in GuiPlugin: importFormats != importFormats()";
    }

    /**
     * Returns the ImportMenu.
     * @ensure result != null.
     * @return CommandMenu the ImportMenu of the ImportHolder.
     */
    private CommandMenu importMenu() {
        CommandMenu result = null;
        result = _importMenu;
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * Sets the _importMenu to importMenu.
     * @require importMenu != null.
     * @ensure importMenu() != null.
     * @ensure importMenu().equals(importMenu)
     * @param importMenu the value to be set.
     */
    private void setImportMenu(CommandMenu importMenu) {
        _importMenu = importMenu;
        assert (importMenu() != null) : "Failure in GuiPlugin: importMenu == null";
        assert (importMenu().equals(importMenu)) : "Failure in GuiPlugin: importMenu != importMenu()";
    }

    private StatusDisplayer displayer() {
        StatusDisplayer result = DrawPlugin.getGui();
        return result;
    }

    //    private void setDisplayer(StatusDisplayer displayer) {
    //        _displayer = displayer;
    //    }
    private DrawApplication application() {
        DrawApplication result = DrawPlugin.getGui();
        return result;
    }

    //    private void setApplication(DrawApplication application) {
    //        _application = application;
    //    }


    /**
     * Returns the list of all FileFilters (Does not contain CombinationFileFilters).
     * @ensure result != null.
     * @return List list of all FileFilters.
     */
    private FileFilter[] fileFilterImport() {
        FileFilter[] result = null;
        List<FileFilter[]> fileFilters = new LinkedList<FileFilter[]>();
        ImportFormat[] formats = allImportFormats();
        int count = 0;
        for (int pos = 0; pos < formats.length; pos++) {
            FileFilter[] filters = buildFileFilter(formats[pos]);
            count = count + filters.length;
            fileFilters.add(filters);
        }
        result = new FileFilter[++count];
        Iterator<FileFilter[]> iter = fileFilters.iterator();
        while (iter.hasNext()) {
            FileFilter[] element = iter.next();
            for (int pos = 0; pos < element.length; pos++) {
                result[--count] = element[pos];
            }
        }
        result[0] = new NoFileFilter();
        return result;
    }

    /**
     * load default importformats.
     *
     */
    private void loadDefaultImportFormats() {
    }

    /**
     *
     * @param importFormat
     * @return
     */
    private FileFilter[] buildFileFilter(ImportFormat importFormat) {
        FileFilter[] result = null;
        FileFilter filter = importFormat.fileFilter();
        List<FileFilter> list = new LinkedList<FileFilter>();
        if (filter instanceof CombinationFileFilter) {
            CombinationFileFilter comFilter = (CombinationFileFilter) filter;
            Iterator<SimpleFileFilter> filters = comFilter.getFileFilters()
                                                          .iterator();
            while (filters.hasNext()) {
                FileFilter element = filters.next();
                list.add(element);
            }
        } else {
            list.add(filter);
        }
        result = new FileFilter[list.size()];
        for (int pos = 0; pos < result.length; pos++) {
            result[pos] = list.get(pos);
        }
        return result;
    }

    /**
     * Constructs the menu item importAll by using a Command.
     */
    private void buildImportAll() {
        Command command = new Command("Import (any type)") {
            public void execute() {
                displayer().showStatus("import...");
                File[] files = DrawPlugin.getCurrent().getIOHelper()
                                         .getLoadPath(fileFilterImport());
                if (files != null) {
                    for (int posFile = 0; posFile < files.length; posFile++) {
                        File currentFile = files[posFile];

                        List<ImportFormat> list = new LinkedList<ImportFormat>();
                        ImportFormat impFormat = null;
                        for (int posFormat = 0;
                                     posFormat < allImportFormats().length;
                                     posFormat++) {
                            try {
                                if (allImportFormats()[posFormat].canImport(currentFile.toURI()
                                                                                               .toURL())) {
                                    list.add(allImportFormats()[posFormat]);
                                }
                            } catch (MalformedURLException e) {
                                logger.error(e.getMessage(), e);
                                displayer().showStatus(e.toString());
                            }
                        }
                        if (list.size() == 1) {
                            impFormat = list.get(0);
                        } else if (list.size() > 1) {
                            Object choice = JOptionPane.showInputDialog(null,
                                                                        "Choose",
                                                                        "ExportFormats",
                                                                        JOptionPane.OK_CANCEL_OPTION,
                                                                        null,
                                                                        list
                              .toArray(), list.get(0));
                            if (choice != null) {
                                impFormat = (ImportFormat) choice;
                            }
                        }
                        if (impFormat != null) {
                            try {
                                loadDrawings(new URL[] { currentFile.toURI()
                                                                    .toURL() },
                                             impFormat);
                            } catch (MalformedURLException e) {
                                logger.error(e.getMessage(), e);
                                displayer().showStatus(e.toString());
                            }
                        } else {
                            displayer().showStatus("no import Format");
                        }

                        // }
                    }
                }
            }
        };
        importMenu().add(command);
    }


    /**
     * Loads an array of files with the help of format.
     * @require format != null.
     * @param files the array of files to be imported.
     * @param format the format.
     */
    private void loadDrawings(URL[] files, ImportFormat format) {
        try {
            if (files != null) {
                if (files.length > 0) {
                    Drawing[] drawings = format.importFiles(files);
                    if (drawings != null) {
                        for (int pos = 0; pos < drawings.length; pos++) {
                            application().openDrawing(drawings[pos]);
                            displayer().showStatus("import");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            displayer().showStatus(e.toString());
        }
    }


    /**
     * Constructs a menu item for this importFormat in importMenu.
     * @require importFormat != null.
     * @param importFormat the ImportFormat for the new menu item.
     */
    private void buildImportFormat(ImportFormat importFormat, CommandMenu parent) {
        ImportFormatCommand command = null;
        if (importFormat instanceof ImportFormatMulti) {
            ImportFormatMulti multi = (ImportFormatMulti) importFormat;
            ImportFormat[] formats = multi.allImportFormats();
            CommandMenu menu = new CommandMenu(multi.formatName());
            for (int pos = 0; pos < formats.length; pos++) {
                buildImportFormat(formats[pos], menu);
            }
            parent.add(menu);
        } else {
            command = generateImportCommand(importFormat);
            parent.add(command);
        }
    }

    /**
     * Returns a FormatCommand.
     * @ensure result != null
     * @param formatName the name of the menu item.
     * @param formatId the Id of the ImportFormat.
     * @return FormatCommand the menu item.
     */
    private ImportFormatCommand generateImportCommand(ImportFormat format) {
        ImportFormatCommand result = null;
        result = new ImportFormatCommand(format) {
                public void execute() {
                    displayer().showStatus("import ...");
                    File[] files = DrawPlugin.getCurrent().getIOHelper()
                                             .getLoadPath(format().fileFilter());
                    URL[] paths = new URL[files.length];
                    for (int pos = 0; pos < paths.length; pos++) {
                        try {
                            paths[pos] = files[pos].toURI().toURL();
                        } catch (MalformedURLException e) {
                            logger.error(e.getMessage(), e);
                            displayer().showStatus(e.toString());
                        }
                    }
                    loadDrawings(paths, format());
                    application().toolDone();
                    displayer().showStatus("import");
                }
            };
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ImportHolder#addImportFormat(de.renew.io.ImportFormat)
     */
    public void addImportFormat(ImportFormat importFormat) {
        logger.debug(getClass() + ": adding import format " + importFormat);
        importFormats().add(importFormat);
        buildImportFormat(importFormat, importMenu());
    }

    /**
     * @see de.renew.io.ImportHolder#allFormats()
     */
    public ImportFormat[] allImportFormats() {
        ImportFormat[] result = null;
        result = new ImportFormat[importFormats().size()];
        for (int pos = 0; pos < result.length; pos++) {
            result[pos] = importFormats().get(pos);
        }
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ImportHolder#removeImportFormat(de.renew.io.FormatId)
     */
    public void removeImportFormat(ImportFormat format) {
        Component[] ele = importMenu().getMenuComponents();
        for (int pos = 0; pos < ele.length; pos++) {
            if (ele[pos] instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) ele[pos];
                if (item.getText().equals(format.formatName())) {
                    importMenu().remove(item);
                }
            }
        }
        importFormats().remove(format);
    }
}