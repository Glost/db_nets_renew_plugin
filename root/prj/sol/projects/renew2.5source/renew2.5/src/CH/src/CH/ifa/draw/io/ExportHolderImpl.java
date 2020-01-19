package CH.ifa.draw.io;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.exportFormats.ExportFormat;
import CH.ifa.draw.io.exportFormats.ExportFormatCommand;
import CH.ifa.draw.io.exportFormats.ExportFormatMulti;

import CH.ifa.draw.standard.NullDrawing;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;

import de.renew.util.StringUtil;

import java.awt.Component;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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
public class ExportHolderImpl implements ExportHolder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ExportHolderImpl.class);
    private CommandMenu exportMenu;

    // Attributes
    // All ExportFormats that have been added.
    private List<ExportFormat> _exportFormats;

    // The ExportMenu
    private CommandMenu _exportMenu11;
    private CommandMenu _exportMenuNN;
    private CommandMenu _exportMenuN1;

    /**
     *
     */
    public ExportHolderImpl() {
        super();
        exportMenu = new CommandMenu("Export");
        exportMenu.putClientProperty(MenuManager.ID_PROPERTY,
                                     "ch.ifa.draw.io.export");
        initExportFormatHolder();
        createDefaultExportFormats();
    }

    private StatusDisplayer displayer() {
        StatusDisplayer result = DrawPlugin.getGui();
        return result;
    }

    private DrawApplication application() {
        DrawApplication result = DrawPlugin.getGui();

        //        result = _application;
        return result;
    }

    public JMenu getExportMenu() {
        return exportMenu();
    }

    //	---------------------------------------------------------------------
    // Implementation of the ExportHolder Interface
    // ---------------------------------------------------------------------
    // Methods


    /**
      * Initiation of the ExportHolder.
      */
    private void initExportFormatHolder() {
        setExportFormats(new LinkedList<ExportFormat>());
        setExportMenu11(new CommandMenu("Export current drawing"));
        setExportMenuNN(new CommandMenu("Export all drawings (single file each)"));
        setExportMenuN1(new CommandMenu("Export all drawings (merged file)"));
        buildExportAll();
        exportMenu.add(exportMenu11());
        exportMenu.add(exportMenuNN());
        exportMenu.add(exportMenuN1());
    }

    /**
      * List of all added ExportFormats.
      * @ensure result != null.
      * @return Map list of all sdded ExportFormats.
      */
    private List<ExportFormat> exportFormats() {
        List<ExportFormat> result = null;
        result = _exportFormats;
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
      * Sets the List of the ExportFormats
      * @require exportFormat != null;
      * @ensure exportFormats() != null.
      * @ensure exportFormats().equals(exportFormats)
      * @param exportFormats the value to be set.
      */
    private void setExportFormats(List<ExportFormat> exportFormats) {
        _exportFormats = exportFormats;
        assert (exportFormats() != null) : "Failure in GuiPlugin: exportFormats == null";
        assert (exportFormats().equals(exportFormats)) : "Failure in GuiPlugin: exportFormats != exportFormats()";
    }

    /**
      * Returns the ExportMenu.
      * @ensure result != null.
      * @return CommandMenu the ExportMenu of the ExportHolder.
      */
    private CommandMenu exportMenu() {
        CommandMenu result = null;
        result = exportMenu;
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
      * Returns the ExportMenu.
      * @ensure result != null.
      * @return CommandMenu the ExportMenu of the ExportHolder.
      */
    private CommandMenu exportMenu11() {
        CommandMenu result = null;
        result = _exportMenu11;
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
      * Sets the _exportMenu to exportMenu.
      * @require exportMenu != null.
      * @ensure exportMenu() != null.
      * @ensure exportMenu().equals(exportMenu)
      * @param exportMenu the value to be set.
           */
    private void setExportMenu11(CommandMenu exportMenu11) {
        _exportMenu11 = exportMenu11;
        assert (exportMenu11() != null) : "Failure in GuiPlugin: exportMenu == null";
        assert (exportMenu11().equals(exportMenu11)) : "Failure in GuiPlugin: exportMenu != exportMenu()";
    }

    /**
      * Returns the ExportMenu.
      * @ensure result != null.
      * @return CommandMenu the ExportMenu of the ExportHolder.
      */
    private CommandMenu exportMenuNN() {
        CommandMenu result = null;
        result = _exportMenuNN;
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
      * Sets the _exportMenu to exportMenu.
      * @require exportMenu != null.
      * @ensure exportMenu() != null.
      * @ensure exportMenu().equals(exportMenu)
      * @param exportMenu the value to be set.
      */
    private void setExportMenuNN(CommandMenu exportMenuNN) {
        _exportMenuNN = exportMenuNN;
        assert (exportMenuNN() != null) : "Failure in GuiPlugin: exportMenu == null";
        assert (exportMenuNN().equals(exportMenuNN)) : "Failure in GuiPlugin: exportMenu != exportMenu()";
    }

    /**
      * Returns the ExportMenu.
      * @ensure result != null.
      * @return CommandMenu the ExportMenu of the ExportHolder.
      */
    private CommandMenu exportMenuN1() {
        CommandMenu result = null;
        result = _exportMenuN1;
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
      * Sets the _exportMenu to exportMenu.
      * @require exportMenu != null.
      * @ensure exportMenu() != null.
      * @ensure exportMenu().equals(exportMenu)
      * @param exportMenu the value to be set.
      */
    private void setExportMenuN1(CommandMenu exportMenuN1) {
        _exportMenuN1 = exportMenuN1;
        assert (exportMenuN1() != null) : "Failure in GuiPlugin: exportMenu == null";
        assert (exportMenuN1().equals(exportMenuN1)) : "Failure in GuiPlugin: exportMenu != exportMenu()";
    }


    /**
     * Returns the list of all FileFilters (Does not contain CombinationFileFilters).
     * @ensure result != null.
     * @return List list of all FileFilters.
     */
    private SimpleFileFilter[] fileFilterExport(Drawing drawing) {
        SimpleFileFilter[] result = null;
        List<FileFilter[]> fileFilters = new LinkedList<FileFilter[]>();
        ExportFormat[] formats = allExportFormats();
        for (int pos = 0; pos < formats.length; pos++) {
            if (formats[pos].canExportDrawing(drawing)) {
                FileFilter[] filters = buildFileFilter(formats[pos]);
                fileFilters.add(filters);
            }
        }

        List<SimpleFileFilter> allFileFilters = new LinkedList<SimpleFileFilter>();
        allFileFilters.add(new NoFileFilter());
        Iterator<FileFilter[]> iter = fileFilters.iterator();
        while (iter.hasNext()) {
            FileFilter[] element = iter.next();
            for (int pos = 0; pos < element.length; pos++) {
                SimpleFileFilter current = (SimpleFileFilter) element[pos];

                // work around
                boolean wert = false;
                for (int pos2 = 0; pos2 <= pos; pos2++) {
                    wert = allFileFilters.get(pos2).equals(current);
                    if (wert == true) {
                        break;
                    }
                }
                if (wert == false) {
                    allFileFilters.add(current);
                }
            }
        }

        result = new SimpleFileFilter[allFileFilters.size()];
        for (int pos = 0; pos < allFileFilters.size(); pos++) {
            result[pos] = allFileFilters.get(pos);
        }
        return result;
    }

    /**
      *
      * @param importFormat
      * @return
      */
    private FileFilter[] buildFileFilter(ExportFormat exportFormat) {
        FileFilter[] result = null;
        FileFilter filter = exportFormat.fileFilter();
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
     * Constructs the menu item exportAll by using a Command.
     */
    private void buildExportAll() {
        Command command = new Command("Export current drawing (any type)...") {
            public void execute() {
                if (!(application().drawing() instanceof NullDrawing)) {
                    File path = DrawPlugin.getCurrent().getIOHelper()
                                          .getSaveFile(null,
                                                       fileFilterExport(application()
                                                                            .drawing()),
                                                       application().drawing());
                    if (path != null) {
                        List<ExportFormat[]> list = new LinkedList<ExportFormat[]>();
                        for (int pos = 0; pos < allExportFormats().length;
                                     pos++) {
                            ExportFormat[] formats = allExportFormats()[pos]
                              .canExport(path);
                            if (formats.length > 0) {
                                list.add(formats);
                            }
                        }
                        Iterator<ExportFormat[]> formatsIter = list.iterator();
                        List<ExportFormat> allFormats = new LinkedList<ExportFormat>();
                        while (formatsIter.hasNext()) {
                            ExportFormat[] formatArray = formatsIter.next();
                            for (int pos = 0; pos < formatArray.length;
                                         pos++) {
                                if (formatArray[pos].canExportDrawing(application()
                                                                                  .drawing())) {
                                    if (new NoFileFilter().equals((SimpleFileFilter) formatArray[pos]
                                                                          .fileFilter())) {
                                        if (StringUtil.getExtension(path.getPath())
                                                              .equals("")) {
                                            allFormats.add(formatArray[pos]);
                                        }
                                    } else {
                                        allFormats.add(formatArray[pos]);
                                    }
                                }
                            }
                        }
                        ExportFormat format = null;
                        if (allFormats.size() == 1) {
                            format = allFormats.get(0);
                        } else if (allFormats.size() > 1) {
                            Object choice = JOptionPane.showInputDialog(null,
                                                                        "Choose",
                                                                        "ExportFormats",
                                                                        JOptionPane.OK_CANCEL_OPTION,
                                                                        null,
                                                                        allFormats
                                                                        .toArray(),
                                                                        allFormats
                                                                        .get(0));
                            if (choice != null) {
                                format = (ExportFormat) choice;
                            }
                        }
                        if (format != null) {
                            saveDrawing(application().drawing(), format, path);
                        } else {
                            displayer().showStatus("no ExportFormat");
                        }
                    }
                } else {
                    displayer().showStatus("no drawing");
                }
            }

            public boolean isExecutable() {
                if (!super.isExecutable()) {
                    return false;
                }
                boolean result = false;
                if (application() != null) {
                    result = !(application().drawing() instanceof NullDrawing);
                }
                return result;
            }
        };
        exportMenu.add(command);
    }

    /**
      * Save an enumeration of drawings with the help of format (n to 1).
      * @require format != null.
      * @require drawings != null
      * @require path != null
      * @param drawings
                      * @param format
                      * @param path
      */
    public void saveDrawings(Enumeration<Drawing> drawings,
                             ExportFormat format, File path,
                             StatusDisplayer displ) {
        try {
            ArrayList<Drawing> list2 = Collections.list(drawings);
            Drawing[] array = list2.toArray(new Drawing[list2.size()]);
            format.export(array, path);
            displ.showStatus("Exported " + path.getPath() + ".");
        } catch (Exception e) {
            logger.error(e.getMessage());
            displ.showStatus(e.toString());
            if (logger.isDebugEnabled()) {
                logger.debug(ExportHolderImpl.class.getSimpleName() + ": ", e);
            }
        }
    }

    /**
     * Save an enumeration of drawings with the help of format.
     * @require format != null.
     * @require drawings != null
     * @param drawings
     * @param format
     * @param path
     */
    private void saveDrawings(Enumeration<Drawing> drawings, ExportFormat format) {
        try {
            List<Drawing> drawingList = new LinkedList<Drawing>();
            while (drawings.hasMoreElements()) {
                Drawing drawing = drawings.nextElement();

                // save drawing			
                if (drawing.getFilename() == null) {
                    //while (drawing.getFilename() == null) {
                    application().saveDrawingAs(drawing);


                    /*if (drawing.getFilename() == null) {
                                    int proced = JOptionPane.showConfirmDialog(null,"Do you want export " + drawing.getName(),"export Drawing",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                                    if (proced == 1) {
                                                    break;
                                    }
                    }*/


                    //}
                }

                // add Drawing to list
                if (drawing.getFilename() != null) {
                    drawingList.add(drawing);
                }
            }

            // list to array
            Drawing[] drawingArray = new Drawing[drawingList.size()];
            File[] paths = new File[drawingArray.length];
            for (int pos = 0; pos < drawingArray.length; pos++) {
                drawingArray[pos] = drawingList.get(pos);
                String name = drawingArray[pos].getName();
                File path = drawingArray[pos].getFilename();
                path = path.getCanonicalFile();
                String pathString = path.getParent() + File.separator + name;
                paths[pos] = DrawingFileHelper.checkAndAddExtension(new File(pathString),
                                                                    (SimpleFileFilter) format
                                                                    .fileFilter());
            }
            if (drawingArray.length > 0) {
                format.exportAll(drawingArray, paths);
                displayer().showStatus("Exported.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            displayer().showStatus(e.toString());
        }
    }

    /**
     * Save an array of drawings with the help of format.
     * @require drawing != null
     * @require format != null
     * @require path != null
     * @param drawing
     * @param format
     * @param path
     */
    public void saveDrawing(Drawing drawing, ExportFormat format, File path,
                            StatusDisplayer sd) {
        try {
            File pathResult = format.export(drawing, path);
            sd.showStatus("Exported " + pathResult.getPath() + ".");
        } catch (Exception e) {
            sd.showStatus(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save an array of drawings with the help of format.
     * @require drawing != null
     * @require format != null
     * @require path != null
     * @param drawing
     * @param format
     * @param path
     */
    private void saveDrawing(Drawing drawing, ExportFormat format, File path) {
        try {
            File pathResult = format.export(drawing, path);
            displayer().showStatus("Exported " + pathResult.getPath() + ".");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            displayer().showStatus(e.toString());
        }
    }


    /**
     * Constructs a menu item for this exportFormat in exportMenu.
     * @require exportFormat != null.
     * @require parent != null.
     * @param exportFormat the ExportFormat for the new menu item.
     */
    private void buildExportFormat(ExportFormat exportFormat,
                                   CommandMenu parent11, CommandMenu parentNN,
                                   CommandMenu parentN1) {
        if (exportFormat instanceof ExportFormatMulti) {
            ExportFormatMulti multi = (ExportFormatMulti) exportFormat;
            ExportFormat[] formats = multi.allExportFormats();
            CommandMenu menu11 = new CommandMenu(multi.formatName());
            CommandMenu menuNN = new CommandMenu(multi.formatName());
            CommandMenu menuN1 = new CommandMenu(multi.formatName());
            for (int posFor = 0; posFor < formats.length; posFor++) {
                buildExportFormat(formats[posFor], menu11, menuNN, menuN1);
            }
            parent11.add(menu11);
            parent11.addSeparator();
            parentNN.add(menuNN);
            parentNN.addSeparator();
            parentN1.add(menuN1);
            parentN1.addSeparator();
        } else {
            generateCommands(exportFormat, parent11, parentNN, parentN1);
        }
    }

    /**
     * @require format != null
     * @require menu != null
     * @param format
     * @param menu
     */
    private void generateCommands(ExportFormat format, CommandMenu menu11,
                                  CommandMenu menuNN, CommandMenu menuN1) {
        ExportFormatCommand command11 = null;
        ExportFormatCommand commandN1 = null;
        ExportFormatCommand commandNN = null;

        // 1 to 1
        command11 = generateExportCommand1to1(format);
        if (format.getShortCut() == -1) {
            menu11.add(command11);
        } else if (format.getModifier() == -1) {
            menu11.add(command11, format.getShortCut());
        } else {
            menu11.add(command11, format.getShortCut(), format.getModifier());
        }

        // n to n
        commandNN = generateExportCommandNtoN(format);
        menuNN.add(commandNN);
        // n to 1
        if (format.canExportNto1()) {
            commandN1 = generateExportCommandNto1(format);
            menuN1.add(commandN1);
        }
    }

    /**
     * @require format != null
     * @ensure result != null
     * @param format
     * @return
     */
    private ExportFormatCommand generateExportCommand1to1(ExportFormat format) {
        ExportFormatCommand result = null;
        if (format != null) {
            result = new ExportFormatCommand(format, " current drawing ...") {
                    public void execute() {
                        Drawing drawing = application().drawing();
                        if (drawing != null) {
                            if (!(drawing instanceof NullDrawing)) {
                                if (format().forceGivenName()) {
                                    if (drawing.getFilename() != null) {
                                        try {
                                            String fileNameText = drawing.getFilename()
                                                                         .getCanonicalPath()
                                                                  + drawing
                                                                     .getName();
                                            fileNameText = StringUtil.getPath(fileNameText)
                                                           + File.separator
                                                           + drawing.getName();
                                            File file = new File(fileNameText);
                                            saveDrawing(drawing, format(), file);
                                        } catch (IOException e) {
                                            logger.error("Could not create export file: ");
                                            logger.debug("Could not create export file: ",
                                                         e);
                                        }
                                    }
                                } else {
                                    File path = DrawPlugin.getCurrent()
                                                          .getIOHelper()
                                                          .getSaveFile(null,
                                                                       new SimpleFileFilter[] { (SimpleFileFilter) format()
                                                                                                                       .fileFilter() },
                                                                       drawing);
                                    if (path != null) {
                                        displayer()
                                            .showStatus("Exporting " + path
                                                        + " ...");
                                        saveDrawing(drawing, format(), path);
                                    }
                                }
                            } else {
                                displayer().showStatus("no drawing");
                            }
                        } else {
                            displayer().showStatus("no drawing");
                        }
                        application().toolDone();
                        //displayer().showStatus("export");
                    }

                    public boolean isExecutable() {
                        boolean result = false;
                        if (application() != null) {
                            if (!(application().drawing() instanceof NullDrawing)) {
                                if (format()
                                                .canExportDrawing(application()
                                                                              .drawing())) {
                                    result = true;
                                }
                            }
                        }
                        return result;
                    }
                };
        }
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * @require format != null
     * @ensure result != null
     * @param format
     * @return
     */
    private ExportFormatCommand generateExportCommandNto1(ExportFormat format) {
        ExportFormatCommand result = null;
        if (format != null) {
            result = new ExportFormatCommand(format,
                                             " all drawings (N to 1) ...") {
                    public void execute() {
                        Enumeration<Drawing> drawings = application().drawings();
                        if (drawings != null) {
                            if (drawings.hasMoreElements()) {
                                File path = DrawPlugin.getCurrent().getIOHelper()
                                                      .getSaveFile(null,
                                                                   new SimpleFileFilter[] { (SimpleFileFilter) format()
                                                                                                                   .fileFilter() },
                                                                   drawings
                                                         .nextElement());
                                if (path != null) {
                                    displayer()
                                        .showStatus("Exporting " + path
                                                    + " ...");
                                    saveDrawings(application().drawings(),
                                                 format(), path, displayer());
                                }
                            } else {
                                displayer().showStatus("no drawing");
                            }
                        }
                        application().toolDone();
                        displayer().showStatus("export");
                    }

                    public boolean isExecutable() {
                        boolean result = true;
                        if (application() == null) {
                            return false;
                        }
                        if (!(application().drawing() instanceof NullDrawing)) {
                            Enumeration<Drawing> drawings = application()
                                                                .drawings();
                            while (drawings.hasMoreElements()) {
                                Drawing drawing = drawings.nextElement();
                                if (!format().canExportDrawing(drawing)) {
                                    result = false;
                                    break;
                                }
                            }
                        } else {
                            result = false;
                        }
                        return result;
                    }
                };
        }
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
    * @require format != null
    * @ensure result != null
    * @param format
    * @return
    */
    private ExportFormatCommand generateExportCommandNtoN(ExportFormat format) {
        ExportFormatCommand result = null;
        if (format != null) {
            result = new ExportFormatCommand(format, " all drawings (N to N)") {
                    public void execute() {
                        displayer().showStatus("Exporting " + " ...");
                        Enumeration<Drawing> drawings = application().drawings();
                        if (drawings != null) {
                            if (drawings.hasMoreElements()) {
                                saveDrawings(drawings, format());
                            } else {
                                displayer().showStatus("no drawing");
                            }
                        }
                        application().toolDone();
                        displayer().showStatus("export");
                    }

                    public boolean isExecutable() {
                        boolean result = true;
                        if (application() == null) {
                            return false;
                        }

                        if (!(application().drawing() instanceof NullDrawing)) {
                            Enumeration<Drawing> drawings = application()
                                                                .drawings();
                            while (drawings.hasMoreElements()) {
                                Drawing drawing = drawings.nextElement();
                                if (!format().canExportDrawing(drawing)) {
                                    result = false;
                                    break;
                                }
                            }
                        } else {
                            result = false;
                        }
                        return result;
                    }
                };
        }
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportHolder#addExportFormat(de.renew.io.ExportFormat)
     */
    public void addExportFormat(ExportFormat exportFormat) {
        logger.debug(getClass() + ": adding export format " + exportFormat);
        exportFormats().add(exportFormat);
        buildExportFormat(exportFormat, exportMenu11(), exportMenuNN(),
                          exportMenuN1());
    }

    /**
      * @see de.renew.io.ExportHolder#allFormats()
      */
    public ExportFormat[] allExportFormats() {
        ExportFormat[] result = null;
        result = new ExportFormat[exportFormats().size()];
        for (int pos = 0; pos < result.length; pos++) {
            result[pos] = exportFormats().get(pos);
        }
        assert (result != null) : "Failure in GuiPlugin: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportHolder#removeExportFormat(de.renew.io.FormatId)
     */
    public void removeExportFormat(ExportFormat format) {
        Component[] ele = exportMenu().getMenuComponents();
        for (int pos = 0; pos < ele.length; pos++) {
            if (ele[pos] instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) ele[pos];
                if (item.getText().equals(format.formatName())) {
                    exportMenu().remove(item);
                }
            }
        }
        exportFormats().remove(format);
    }

    private void createDefaultExportFormats() {
//        addExportFormat(new PSExportFormat());
//        addExportFormat(new EPSExportFormat());
//        addExportFormat(new PDFExportFormat());
//        addExportFormat(new SVGExportFormat());
    }
}