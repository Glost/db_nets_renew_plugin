package CH.ifa.draw.io.exportFormats;

import CH.ifa.draw.framework.Drawing;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * This interface must be implemented to define new ExportFormats.
 */
public interface ExportFormat {

    /**
      * Returns a name for the ExportFormat, e.g. PNML.
      * @ensure result != null.
      * @return String, the Name of the ExportFormat.
      */
    public String formatName();


    /**
      * Returns the FileFilter for the ExportFormat.
      * @ensure result != null.
      * @return FileFilter, the FileFilter for the ExportFormat.
      */
    public FileFilter fileFilter();

    /**
      * Converts a drawing into a file (1 to 1 export).
      * @require path != null.
      * @require drawing != null.
      * @ensure result != null.
      * @throws Exception, is catched by the ExportHolder.
      * @param drawings drawing to be exported.
      * @param file file name to export to.
      * @return TODO
      */
    public File export(Drawing drawing, File file) throws Exception;

    /**
      * Converts an array of drawings into a file (n to 1 export).
      * @require path != null.
      * @require drawings != null.
      * @require drawings.length > 0
      * @require canExportNto1()
      * @ensure result != null.
      * @throws Exception, is catched by the ExportHolder.
      * @param drawings array of drawings to be exported.
      * @param file file name to export to.
      * @return TODO
      */
    public File export(Drawing[] drawings, File file) throws Exception;

    /**
      * Converts an array of drawings into an array of files (n to n export).
      * @require path != null.
      * @require path.lentgh > 0
      * @require drawings != null.
      * @require drawings.length > 0
      * @require drawings.length == path.length
      * @ensure result != null.
      * @throws Exception, is catched by the ExportHolder.
      * @param drawings array of drawings to be exported.
      * @param file array of file names to export to.
      * @return TODO
      */
    public File[] exportAll(Drawing[] drawings, File[] files)
            throws Exception;

    /**
     * Tells whether this <code>ExportFormat</code> supports files that comprise
     * multiple drawings.
     * @return  <code>true</code> if this format can generate files
     *          including multiple drawings.
     *          Returns <code>false</code>, otherwise.
     */
    public boolean canExportNto1();

    /**
     * Tells whether this <code>ExportFormat</code> can export to a file
     * named <code>path</code>.
     * @param path  the file name of the exported drawing.
     * @return  <code>true</code>, if <code>path</code> is an allowed file
     *          name for this export format.
     *          Returns <code>false</code>, otherwise.
     */
    public ExportFormat[] canExport(File path);

    /**
     * Tells whether this <code>ExportFormat</code> can export the given
     * <code>drawing</code>.
     * @param drawing  the drawing of question.
     * @return  <code>true</code>, if this export format can handle the
     *          given <code>drawing</code>.
     *          Returns <code>false</code>, otherwise.
     */
    public boolean canExportDrawing(Drawing drawing);

    /**
     * Returns the AWT shortcut key that would be appropriate when
     * including this <code>ExportFormat</code> in a menu.
     * @return  the <code>int</code> code of the shortcut key for the export
     *          menu entry associated with this format.
     *          Returns <code>-1</code> if no shortcut is desired.
     */
    public int getShortCut();


    /**
     * Returns the AWT modifier key code.
     * Use @see java.awt.Toolkit#getMenuShortcutKeyMask for Ctrl. Key.
     * @see javax.swing.KeyStroke
     * @see java.awt.Toolkit
     *
     * @return the <code>int</code> code of the modifier key for the export
     *          menu entry associated with this format.
     *          Returns <code>-1</code> if no shortcut is desired.
     */
    public int getModifier();

    public boolean forceGivenName();
}