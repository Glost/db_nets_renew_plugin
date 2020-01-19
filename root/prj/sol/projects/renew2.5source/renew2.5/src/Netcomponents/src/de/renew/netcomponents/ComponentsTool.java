package de.renew.netcomponents;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import CH.ifa.draw.standard.ToolButton;

import CH.ifa.draw.util.Palette;

import de.renew.gui.GuiPlugin;

import de.renew.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.net.URL;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;


/**
 * ComponentsTool.java
 *
 * The ComponentsTool extends the usual functionality of the CPNApplication by
 * adding a new palette to the gui. It reads the drawings - which hold the
 * Net-Components - from the ".../tools" directory without opening them. Instead
 * the figures will be inserted into the current drawing when using the tools.
 * To create the palette there have to be - in addition to each tool drawing in
 * the ".../tools" directory - also three gif images located in
 * ".../tools/images" directory. If not a blank image will be assosiated with
 * the net-component
 *
 * <pre>
 * 0.3 Now sequence is determined by the &quot;.../tools/.sequence&quot; file
 * 0.4 The tools directory is now determined by the user property &quot;user.toolsdir&quot;
 *     which defines the parent directory  of tools. So the tools are now in
 *     user.toolsdir+&quot;tools/&quot;. If no user.toolsdir is specified the current
 *     directory is parent directory to tools.
 * 0.5 The ComponentsTool has now been adapted to the plugin architecture of renew.
 * 0.6 Label added, which consists in the tools-directory path, so far.
 *     The ComponentsTool can now remove itself from the toolsPanel.
 *     toString method added which displays the Label.
 * 0.7 loadDrawing changed to loadDrawingWithoutGUI (DrawApplication)
 * 0.8 remove detached palette solved
 * 0.9 fixed generic image substitution
 * 0.10 changed signature of createTools (added Name of Palette)
 *      changed signature of Constructor and added legacy constructor
 * </pre>
 *
 * @author Lawrence Cabac
 * @version 0.10, December 2006
 *
 */
public class ComponentsTool implements StatusDisplayer {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ComponentsTool.class);

    /**
     * The toolsdir property name.
     */
    protected static String TOOLDIRPROPERTY = "user.toolsdir";

    /**
     * The panel that contains the palettes / ComponentsTools.
     */


    // private Panel toolPanel;

    /**
     * The menuFrame of the CPN application / Renew GUI.
     */


    // private Frame menuFrame;

    /**
     * The CPN application / Renew GUI
     */


    // private CPNApplication cpnapp;

    /**
     * The palette that holds the tool buttons of this ComponentsTool.
     */
    private Palette palette;

    /**
     * The signifier of this ComponentsTool.
     */
    private String label;

    /**
     * Shows if the given toolsdir directory contains some drawings and button
     * images.
     */
    private boolean toolsDirCheck = false;

    /**
     * Shows if the given toolsdir directory contains some drawings and button
     * images.
     */
    private boolean toolsDirIsSet = false;

    /**
     * Constructor for ComponentsTool. It has to know the CPN application. The
     * tools directory is determined by the "user.toolsdir" java commandlind
     * argument.
     *
     */
    public ComponentsTool() {
        // get the toolPanel and the menuFrame from the CPNApplication
        // this.cpnapp = cpnapp;
        // this.toolPanel = cpnapp.getToolsPanel();
        // this.menuFrame = cpnapp.menuFrame();
        // *** still have to decide where to put those
        String dirName = System.getProperty(TOOLDIRPROPERTY);
        logger.debug(dirName);
        if (dirName == null || !new File(dirName).exists()) {
            URL location = ComponentsToolPlugin.getLocation();
            String toolsLoc = new File(location.getFile()).getAbsolutePath();
            toolsLoc = toolsLoc.substring(0,
                                          toolsLoc.lastIndexOf(File.separator))
                       + File.separator + "tools";
            if (new File(toolsLoc).exists()) {
                dirName = toolsLoc;
                toolsDirIsSet = true;
                String statusMessage = "The " + TOOLDIRPROPERTY
                                       + " property is not set or not valid. Trying plugin location...";
                GuiPlugin.getCurrent().showStatus(statusMessage);
                logger.debug(statusMessage);
            } else {
                toolsDirIsSet = false;
                dirName = "";
                logger.info("Netcomponents: The " + TOOLDIRPROPERTY
                            + " is not set! \n" + "Override by setting the "
                            + TOOLDIRPROPERTY + " property.");

                if (logger.isDebugEnabled()) {
                    logger.debug("netcomponents: using default tooldir '"
                                 + dirName + "'." + "Override by setting the "
                                 + TOOLDIRPROPERTY + " property.");
                }
            }
        } else {
            toolsDirIsSet = true;
        }

        setLabel("default " + dirName);
        createTools(dirName, "Components", null);
    }

    // public ComponentsTool(String dirName) {
    // new ComponentsTool(dirName, dirName,null);
    // }

    /**
     * Constructor for ComponentsTool. It has to know the CPN application and
     * the path name of the tools directory as a String..
     *
     * @param dirName -
     *            the path name of the tools directory (absolut)
     */
    public ComponentsTool(String dirName, String paletteName,
                          ComponentsPluginExtender plugin) {
        // get the toolPanel and the menuFrame from the CPNApplication
        // this.cpnapp = cpnapp;
        // this.toolPanel = cpnapp.getToolsPanel();
        // this.menuFrame = cpnapp.menuFrame();
        // this._plugin = plugin;
        toolsDirIsSet = true;
        setLabel(dirName);
        createTools(dirName, paletteName, plugin);
    }

    /**
     * Method createTools. Adds the Renew Drawings of the specified dirctory
     * into this ComponentsTool, adds the images from "<user.toolsdir>/images"
     * to the palette sorted as specified in "<user.toolsdir>/.sequence" and
     * adds the palette to the toolsPanel.
     *
     * @param dirName -
     *            the tools directory name.
     */
    void createTools(String dirName, String paletteName,
                     ComponentsPluginExtender plugin) {
        // create tools for Net-Components
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter == null) {
            logger.error("ComponentsPlugin: no GuiPlugin available!");
            //NOTICEnull add return statement since the following code would crash
            return;
        }
        JFrame frame = starter.getGuiFrame();
        if (frame == null) {
            logger.error("NetComponents: could not create Tools: no GUI available.");
        }
        palette = new Palette(paletteName);

        File dir = new File(dirName);
        File imageDir = new File(dirName + File.separator + "images");


        // read the sequence of the buttons in the palette
        Vector<String> v_seq = new Vector<String>();

        try {
            BufferedReader seq = new BufferedReader(new FileReader(dirName
                                                                   + File.separator
                                                                   + ".sequence"));

            while (seq.ready()) {
                v_seq.add(seq.readLine());
            }

            seq.close();
        } catch (Exception e) {
            logger.error("The File" + dirName + File.separator + ".sequence"
                         + " could not be found " + e);
        }

        // check if "tools" directory exists
        if (dir.isDirectory()) {
            if (imageDir.isDirectory()) {
                // add all renew drawings that are not in sequence list
                String[] fileNames = dir.list();

                for (int i = 0; i < fileNames.length; i++) {
                    String s = StringUtil.getFilename(fileNames[i]);
                    String ext = StringUtil.getExtension(fileNames[i]);

                    // check if file is a renew drawing
                    if ((new File(dirName + File.separator + fileNames[i]))
                                    .isFile() && "rnw".equals(ext)) {
                        if (!v_seq.contains(s)) {
                            v_seq.add(s);
                        }
                    }
                }
                if ((new File(dirName + File.separator + "images"
                                      + File.separator + "generic.gif")).exists()) {
                    Iterator<String> it = v_seq.iterator();

                    while (it.hasNext()) {
                        String toolName = it.next();

                        ToolButton button = createToolButton(toolName, dirName,
                                                             starter);
                        if (button != null) {
                            palette.add(button);
                        }

                        // at least one drawing is loaded so we presume that the
                        // directory is a tools directory
                        toolsDirCheck = true;
                    }

                    // try to add additional buttons
                    if (plugin != null) {
                        Vector<ToolButton> additionalButtons = plugin
                                                                   .getAdditionalButtons();
                        if (additionalButtons != null) {
                            Iterator<ToolButton> ite = additionalButtons
                                                           .iterator();
                            while (ite.hasNext()) {
                                ToolButton toolButton = ite.next();
                                palette.add(toolButton);
                            }
                        }
                    }

                    // while it has next
                    // do not bother if no tool is added.
                    if (toolsDirIsValid()) {
                        starter.getPaletteHolder().addPalette(palette);
                    }
                } // end of if "generic.gif"
                else {
                    logger.error("The directory " + dirName + File.separator
                                 + "images seems to exist");
                    logger.error("but it does not contain the files generic.gif");
                    logger.error("So I think that the toolsdir variable is not set to a proper toolsdir directory");
                    logger.error("or the selected directory does not match the requirements.");
                    logger.error("A proper toolsdir directory contains the images directory  ");
                    logger.error("which contains at least the file generic.gif as fallback and a .sequence file.");
                    logger.error("");
                }
            } // images dir exists
            else {
                logger.error("The directory " + dirName + File.separator
                             + "images does not exist!");
            }
        } // tools dir exists
        else {
            logger.error("The directory " + dirName + " does not exist!");
        }
    }

    private ToolButton createToolButton(String toolName, String dirName,
                                        GuiPlugin starter) {
        Tool tool;
        String fileName = toolName + ".rnw";
        if (logger.isDebugEnabled()) {
            logger.debug(ComponentsTool.class.getName() + ": dirName: "
                         + dirName);
            logger.debug(ComponentsTool.class.getName() + ": fileName: "
                         + fileName);
        }
        Drawing drawing = DrawingFileHelper.loadDrawing(new File(dirName,
                                                                 fileName), this);
        if (drawing == null) {
            return null;
        }
        tool = createInsertionTool(starter, drawing);

        // String toolName = StringUtil.getFilename(fileName);
        String imageName = "";

        // check Images
        // in case of missing gifs use default, plain button
        // generic (old was: 1.gif 2.gif 3.gif)
        String toolBase = dirName + File.separator + "images" + File.separator;
        File imageFile = (new File(toolBase + toolName + ".gif"));
        File selFile = new File(toolBase + toolName + ".gif");
        if (imageFile.exists() && selFile.exists()) {
            imageName = toolName;
        } else {
            imageName = "generic";
            logger.warn("File does not exist: " + imageFile.getAbsolutePath()
                        + " trying to load generic.gif instead.");
        }

        ToolButton button = starter.getPaletteHolder()
                                   .createToolButton(toolBase + imageName,
                                                     "NC " + toolName, tool);
        return button;
    }

    private Tool createInsertionTool(GuiPlugin starter, Drawing drawing) {
        DrawingEditor editor = starter.getDrawingEditor();
        Tool tool;
        if (logger.isDebugEnabled()) {
            logger.debug("ComponentsTool: " + drawing);
        }

        // have to put all the figures each by each into the Vector
        // any other way? ****
        FigureEnumeration fe = drawing.figures();
        Vector<Figure> v = new Vector<Figure>();

        while (fe.hasMoreElements()) {
            v.addElement(fe.nextElement());
        }

        tool = new InsertionTool(editor, v);
        return tool;
    }

    // createTools

    /**
     * Method remove. Removes the ComponentsTool form the toosPanel.
     */
    public void remove() {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter != null) {
            starter.getPaletteHolder().removePalette(palette);
        }
    }

    private void setLabel(String s) {
        label = s;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return getLabel();
    }

    public boolean toolsDirIsSet() {
        return toolsDirIsSet;
    }

    public boolean toolsDirIsValid() {
        return toolsDirCheck;
    }

    @Override
    public void showStatus(String message) {
        logger.warn(ComponentsTool.class.getSimpleName()
                    + ": A file could not be loaded.");
        logger.warn(ComponentsTool.class.getSimpleName()
                    + ": Turn on debug to display more information.");
        logger.warn(ComponentsTool.class.getSimpleName()
                    + ": To do so add line log4j.logger.de.renew.netcomponents.ComponentsTool=DEBUG");
        logger.warn(ComponentsTool.class.getSimpleName()
                    + ": to your log4j configuration in e.g. ~/.log4j.properties.");
        logger.debug(message);
    }
}