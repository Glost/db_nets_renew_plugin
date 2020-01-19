/**
 *
 */
package de.renew.lola;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.CPNDrawing;
import de.renew.gui.GuiPlugin;
import de.renew.gui.PlaceFigure;
import de.renew.gui.VirtualPlaceFigure;

import de.renew.io.importFormats.LolaImportFormat;

import de.renew.lola.commands.CheckAllCommand;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


/**
 * @author hewelt, mosteller
 * @date 16.08.2012
 */
public class LolaGUI extends JFrame {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(CheckAllCommand.class);
    private String lolaPath = "";

    /**
     * The panels used for the tabs
     */
    private JPanel _generalPanel = new JPanel();
    private JPanel _checkListPanel = new JPanel();
    private JPanel _markEdPanel = new JPanel();

    /**
     * The drawing being analyzed at the moment
     */
    private CPNDrawing drawing;

    /**
     * Marking editor components, which are needed globally
     */
    private JTable _markingTable;
    private JCheckBox _updateInitialMarkingCheckbox = new JCheckBox("Update initial marking",
                                                                    true);
    private CheckListAction checkListListener;
    private UpdatePlacesAction updateListener;

    /**
     * Labels showing the results of the net analysis
     */
    protected JLabel livenessResult = new JLabel();
    protected JLabel deadlockFreedomResult = new JLabel();
    protected JLabel reversibilityResult = new JLabel();
    protected JLabel homeMarkingResult = new JLabel();
    protected JLabel boundednessResult = new JLabel();
    protected JLabel quasiLivenessResult = new JLabel();
    protected JLabel nameOfNet = new JLabel();

    /**
     * Fields for the general panel needed globally
     */
    private static final Vector<String> taskColumnNames = new Vector<String>();
    {
        taskColumnNames.add("Result");
        taskColumnNames.add("Verification Task");
    }

    private JTable _taskTable;
    protected String newTaskText = "";

    /**
     * @throws HeadlessException
     */
    public LolaGUI() throws HeadlessException {
        init();
    }

    /**
     * @param gc
     */
    public LolaGUI(GraphicsConfiguration gc) {
        super(gc);
        init();
    }

    /**
     * @param title
     * @throws HeadlessException
     */
    public LolaGUI(String title) throws HeadlessException {
        super(title);
        init();
    }

    /**
     * @param title
     * @param gc
     */
    public LolaGUI(String title, GraphicsConfiguration gc) {
        super(title, gc);
        init();
    }

    public LolaGUI(String title, String path) {
        super(title);
        lolaPath = path;
        init();
    }

    /**
     * This runs the checks in the Checklist and
     * fills the table of the marking editor.
     */
    public void checkNow() {
        checkListListener.actionPerformed(null);
        updateListener.actionPerformed(null);
    }

    /**
     * Init the 3 tabs by running their individual setup methods.
     */
    public void init() {
        DrawApplication app = DrawPlugin.getGui();

        // current drawing needs to be a CPNDrawing
        if (app.drawing() instanceof CPNDrawing) {
            drawing = (CPNDrawing) app.drawing();
            JTabbedPane tabs = new JTabbedPane();
            _generalPanel.setVisible(true);
            _checkListPanel.setVisible(true);
            _markEdPanel.setVisible(true);
            setupMarkEdPanel();
            setupCheckListPanel();
            setupGeneralPanel();
            //tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            tabs.addTab("Checklist", _checkListPanel);
            tabs.addTab("Marking Editor", _markEdPanel);
            tabs.addTab("Tasks", _generalPanel);
            this.add(tabs, BorderLayout.CENTER);
            // now check
            checkNow();
        } else {
            logger.error("[Lola GUI] Could not initialize, no open CPNDrawing.");
        }
    }

    /**
     * Setup the general tab of the Lola GUI.
     * It offers the lola commands {@link de.renew.lola.commands} to the user.
     */
    private void setupGeneralPanel() {
        /**
             * A JTable, which can't be edited
             */
        _taskTable = new JTable() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        setupTaskTable();
        JScrollPane scrollPane = new JScrollPane(_taskTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        /**
             * Adding new Tasks
             */
        final JTextField newTask = new JTextField(20);
        newTask.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {
                    newTaskText = newTask.getText();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    newTaskText = newTask.getText();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });
        newTask.setToolTipText("<html>Input new verification task here and click the add button.<br>"
                               + "(some pointer on syntax should go here) <br>"
                               + "<i>There is no syntax check so far.</i></html> ");
        JButton addButton = new JButton("Add task");
        addButton.setToolTipText("Add the entered task to the list");
        addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (newTaskText != "") {
                        logger.info("[Lola GUI] Adding Task: " + newTaskText);
                        TextFigure tmpFig = new TextFigure(newTaskText);
                        LolaTask tmpTask = new LolaTask(tmpFig, drawing);
                        if (tmpTask.isValid()) {
                            drawing.add(tmpFig);
                            Vector<Object> newRow = new Vector<Object>();
                            newRow.add(new LolaResult(6));
                            newRow.add(tmpTask);
                            ((DefaultTableModel) _taskTable.getModel()).addRow(newRow);
                        }
                    } else {
                        logger.error("[Lola GUI] No text, no task to add.");
                    }
                }
            });

        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.X_AXIS));
        addPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        addPanel.add(newTask);
        addPanel.add(Box.createHorizontalStrut(5));
        addPanel.add(addButton);

        /*
             * The button Panel includes buttons for updating and checking tasks
             */
        JButton updateButton = new JButton("Update Tasks");
        updateButton.setAlignmentX(CENTER_ALIGNMENT);
        updateButton.setToolTipText("Parses the tasks in the current net drawing and displays them in the table.");
        updateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LolaFileCreator creator = new LolaFileCreator();
                    Vector<LolaTask> tasks = creator.parseTasks(drawing);
                    DefaultTableModel model = new DefaultTableModel();
                    model.addColumn("task", tasks);
                    model.addColumn("result", new Vector<LolaResult>());
                    model.setColumnIdentifiers(taskColumnNames);
                    _taskTable.setModel(model);
                    setupTaskTable();
                }
            });

        /**
             * Button and ActionListener for "check button"
             * Gets all LolaTask and call their check method.
             */
        JButton checkButton = new JButton("Check Tasks");
        checkButton.setAlignmentX(CENTER_ALIGNMENT);
        checkButton.setToolTipText("Checks the selected tasks and displays the result.");
        checkButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < _taskTable.getRowCount(); i++) {
                        LolaTask tmpTask = (LolaTask) _taskTable.getValueAt(i, 1);
                        logger.info("[Lola TaskCheck] checking task "
                                    + tmpTask.toString());
                        tmpTask.writeToFile();
                        LolaResult lolaResult = tmpTask.check();
                        _taskTable.setValueAt(lolaResult, i, 0);
                    }
                }
            });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(updateButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(checkButton);

        /*
             * Layouting the general Panel
             */
        _generalPanel.setLayout(new BorderLayout());
        _generalPanel.add(scrollPane, BorderLayout.CENTER);
        _generalPanel.add(addPanel, BorderLayout.PAGE_START);
        _generalPanel.add(buttonPanel, BorderLayout.PAGE_END);
    }

    /**
     * Do the setup of the taskTable:
     * <ul>
     * <li> parse the tasks from the current net drawing
     * <li> construct a new table model
     * <li> set this as the model of taskTable
     * <li> set width of columns, height of rows
     * <li> set Renderer for result column
     * </ul>
     * <p> This is done initially and everytime the "update tasks" button is clicked
     */
    private void setupTaskTable() {
        LolaFileCreator creator = new LolaFileCreator();
        if (drawing == null) {
            System.out.println("DRAWING IS NULL");
        }
        Vector<LolaTask> initialTasks = creator.parseTasks(drawing);

        // sort tasks by their position in the drawing (or by the string ordering of the text if the position is identical) 
        Collections.sort(initialTasks,
                         new Comparator<LolaTask>() {
                @Override
                public int compare(LolaTask o1, LolaTask o2) {
                    Rectangle r1 = o1.getFigure().displayBox();
                    Rectangle r2 = o2.getFigure().displayBox();
                    int result;
                    if (r1.x == r2.x && r1.y == r2.y) {
                        // same position, sort by text ordering
                        result = o1.toString().compareTo(o2.toString());
                    } else if (r1.y > r2.y || (r1.y == r2.y && r1.x > r2.x)) {
                        result = 1;
                    } else {
                        result = -1;
                    }
                    return result;
                }
            });

        Vector<LolaResult> initialResults = new Vector<LolaResult>();
        for (LolaTask lolaTask : initialTasks) {
            initialResults.add(new LolaResult(6));
            if (logger.isDebugEnabled()) {
                logger.debug(LolaGUI.class.getSimpleName() + ": "
                             + lolaTask.getType());
            }
        }

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("result", initialResults);
        model.addColumn("task", initialTasks);
        model.setColumnIdentifiers(taskColumnNames);

        _taskTable.setModel(model);
        _taskTable.setFillsViewportHeight(true);
        _taskTable.setRowHeight(20);
        _taskTable.setIntercellSpacing(new Dimension(4, 4));
        _taskTable.getColumnModel().getColumn(1).setPreferredWidth(350);
        _taskTable.getColumnModel().getColumn(1).setMinWidth(350);
        _taskTable.getColumnModel().getColumn(1)
                  .setCellRenderer(new DefaultTableCellRenderer());
        _taskTable.getColumnModel().getColumn(0).setMaxWidth(20);
        _taskTable.getColumnModel().getColumn(0).setMinWidth(20);
        _taskTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public void setValue(Object o) {
                    if (o instanceof LolaResult) {
                        logger.info("[Lola GUI] Rendering result column, " + o
                                    + "!");
                        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                        setText(((Integer) ((LolaResult) o).getExitValue())
                            .toString());
                        StringBuffer tip = new StringBuffer("<html>");
                        for (String s : ((LolaResult) o).getOutput()) {
                            tip.append(s + "<br>");
                        }
                        setToolTipText(tip.toString() + "</html>");
                        switch (((LolaResult) o).getExitValue()) {
                        case 0:
                            setBackground(Color.GREEN);
                            break;
                        case 1:
                            setBackground(Color.RED);
                            break;
                        case 3:
                            setBackground(Color.BLACK);
                            break;
                        case 5:
                            setBackground(Color.GRAY);
                            break;
                        default:
                            setBackground(Color.WHITE);
                            setText("");
                            break;
                        }
                    } else {
                        logger.error("[Lola GUI] This should use other renderer.");
                        super.setValue(o);
                    }
                }
            });
    }

    private File drawReachGraph() {
        LolaAnalyzer analyzer = new LolaAnalyzer();
        File tmpLolaFile = LolaFileCreator.writeTemporaryLolaFile(drawing);
        String baseName = tmpLolaFile.getAbsolutePath()
                                     .substring(0,
                                                tmpLolaFile.getAbsolutePath()
                                                           .lastIndexOf("."));
        String graphFileName = baseName + ".graph";
        String dotFileName = baseName + ".dot";
        String imgType = "png";
        String imgFileName = baseName + "." + imgType;
        File imageFile = null;

        LolaResult netBounded = analyzer.checkNet(LolaHelper.netBoundedCommand,
                                                  tmpLolaFile);
        if (netBounded.getExitValue() == 1) { // net is bounded
            Runtime myrun = Runtime.getRuntime();

            // construct lola command
            String[] lolaCmd = { lolaPath + "lola", tmpLolaFile.toString(), "-m" };
            String line = "";
            Boolean wasError = false;
            ArrayList<String> result = new ArrayList<String>();
            try {
                // run the lola command and let it finish, writing graph to file
                logger.info("[Lola Graph] Calling " + lolaCmd[0] + " "
                            + lolaCmd[1] + " " + lolaCmd[2] + " ");
                Process lolproc = myrun.exec(lolaCmd);
                BufferedReader output = new BufferedReader(new InputStreamReader(lolproc
                                                                                 .getInputStream()));
                while ((line = output.readLine()) != null) {
                    System.out.println(line);
                }
                output.close();
                lolproc.waitFor();

                // run graph2dot command, it reads the graph from file and produces a dot file
                String[] g2dCmd = { lolaPath + LolaHelper.dotCommand, "-g", graphFileName, "-d", dotFileName };

                logger.info("[Lola Graph] Calling " + g2dCmd[0] + " "
                            + g2dCmd[1] + " " + g2dCmd[2] + " " + g2dCmd[3]
                            + " " + g2dCmd[4] + " ");
                Process g2dproc = myrun.exec(g2dCmd);
                BufferedReader graph = new BufferedReader(new InputStreamReader(g2dproc
                                                                                .getInputStream()));
                BufferedReader error = new BufferedReader(new InputStreamReader(g2dproc
                                                                                .getErrorStream()));
                while ((line = error.readLine()) != null) {
                    logger.error("[graph2dot ERROR]" + line);
                    wasError = true;
                }
                while ((line = graph.readLine()) != null) {
                    result.add(line);
                    logger.error("[graph2dot]" + line);
                }
                int g2dRetVal = g2dproc.waitFor();
                if (!(wasError) && g2dRetVal == 0) {
                    logger.info("[graph2dot] Call returned exit value "
                                + g2dRetVal);
                    logger.info("[Lola Graph] Successfully created dot file in "
                                + dotFileName);
                }
                graph.close();

                // run dot on the dot File and create image
                if (!wasError) { // if no error occurred
                    String[] args = { "dot", "-T" + imgType, dotFileName, "-o", imgFileName };
                    Process dotproc = myrun.exec(args);
                    BufferedReader dotIn = new BufferedReader(new InputStreamReader(dotproc
                                                                                    .getInputStream()));
                    BufferedReader dotEr = new BufferedReader(new InputStreamReader(dotproc
                                                                                    .getErrorStream()));
                    while ((line = dotEr.readLine()) != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[dot ERROR]" + line);
                        }
                        wasError = true;
                    }
                    while ((line = dotIn.readLine()) != null) {
                        result.add(line);
                        if (logger.isDebugEnabled()) {
                            logger.debug("[dot]" + line);
                        }
                    }
                    int dotExitVal = dotproc.waitFor();

                    // create file, which is returned
                    imageFile = new File(imgFileName);
                    logger.info("[Lola Graph] dot returned " + dotExitVal
                                + " and imgFile in " + imgFileName
                                + (imageFile.exists() ? " exists." : "don't exist"));
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(LolaGUI.class.getSimpleName() + ": ", e);
                }
                JOptionPane.showMessageDialog(null,
                                              "Cannot find dot.\n"
                                              + "Make shure graphviz is installed and dot is available in your path.\n"
                                              + "Current PATH is set to : \n"
                                              + System.getenv("PATH"),
                                              "Cannot find dot.",
                                              JOptionPane.ERROR_MESSAGE,
                                              GuiPlugin.getRenewIcon());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else { // net is unbounded
            logger.error("[Lola GUI] Reachability graph not supported for unbounded nets.");
        }
        return imageFile;
    }

    private void setupMarkEdPanel() {
        /*
             * Set up initial table (1 row, 3 columns) and set column names
             */
        int numColumns = 3;
        int numRows = 1;
        _markingTable = new JTable(numRows, numColumns);

        _markingTable.getColumnModel().getColumn(0).setHeaderValue("Place");
        _markingTable.getColumnModel().getColumn(1).setHeaderValue("Initial");
        _markingTable.getColumnModel().getColumn(2).setHeaderValue("Check");

        /*
         * Create buttons and panes and make them visible/layout them
         */
        JScrollPane scrollPane = new JScrollPane(_markingTable);
        _markingTable.setFillsViewportHeight(true);

        JButton updateButton = new JButton("Update places!");
        updateButton.setToolTipText("Update the places from the currently active net.");
        JButton reachButton = new JButton("Check reachability!");
        reachButton.setToolTipText("Check if the marking entered into the table can be reached from the initial marking (from the table).");
        JButton homeButton = new JButton("Check home status!");
        homeButton.setToolTipText("Check if the marking entered into the table is a home state w.r.t. the initial marking (from the table).");
        JButton coverButton = new JButton("Check coverability!");
        coverButton.setToolTipText("Check if the marking entered into the table is coverable w.r.t. the initial marking (from the table).");

        _markEdPanel.setLayout(new BorderLayout());
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(5, 1));

        _markEdPanel.add("Center", scrollPane);

        /*
         * Add action to the buttons and then add the buttons
         */
        updateListener = new UpdatePlacesAction();
        updateButton.addActionListener(updateListener);
        sidePanel.add(updateButton);

        ReachableMarkingAction reachableMarkingListener = new ReachableMarkingAction();
        reachButton.addActionListener(reachableMarkingListener);
        sidePanel.add(reachButton);

        HomeStateAction homeStateListener = new HomeStateAction();
        homeButton.addActionListener(homeStateListener);
        sidePanel.add(homeButton);

        CoverabilityAction coverabilityListener = new CoverabilityAction();
        coverButton.addActionListener(coverabilityListener);
        sidePanel.add(coverButton);


        /*
         * Also add a checkbox, so that the user can decide if he wants to update the initial marking
         * from the net or keep the manually input one.
         */


        // JCheckBox updateInitialMarkingCheckbox = new JCheckBox("Update IM",true);
        _updateInitialMarkingCheckbox.setToolTipText("If this is checked the initial marking will be updated"
                                                     + " from the net when the places are updated.");
        sidePanel.add(_updateInitialMarkingCheckbox);
        _markEdPanel.add("East", sidePanel);
    }

    private void setupCheckListPanel() {
        GridLayout checkListLayout = new GridLayout(9, 2, 10, 5);
        _checkListPanel.setLayout(checkListLayout);

        JLabel netnameLabel = new JLabel("Currently checking: ");
        JLabel quasiLivenessLabel = new JLabel("Quasi-Liveness");
        JLabel livenessLabel = new JLabel("Liveness");
        JLabel deadlockFreedomLabel = new JLabel("Deadlock freedom");
        JLabel reversibilityLabel = new JLabel("Reversibility");
        JLabel homeMarkingLabel = new JLabel("Home Marking");
        JLabel boundednessLabel = new JLabel("Boundedness");

        JLabel[] labels = new JLabel[] { quasiLivenessLabel, livenessLabel, deadlockFreedomLabel, reversibilityLabel, homeMarkingLabel, boundednessLabel, netnameLabel };
        JLabel[] results = new JLabel[] { quasiLivenessResult, livenessResult, deadlockFreedomResult, reversibilityResult, homeMarkingResult, boundednessResult, nameOfNet };

        // set tooltips
        quasiLivenessLabel.setToolTipText("Is the net quasi-live (i.e. every transition non-dead in initial marking)?");
        livenessLabel.setToolTipText("Is the net live (i.e. for all reachable markings there is a firing sequence, so that every transition can be enabled)?");
        deadlockFreedomLabel.setToolTipText("Is the net deadlock-free?");
        reversibilityLabel.setToolTipText("Is the net reversible (is the initial marking a home state)?");
        homeMarkingLabel.setToolTipText("Is there a home marking in the net?");
        boundednessLabel.setToolTipText("Is the net bounded?");

        for (int i = 0; i < labels.length; i++) {
            labels[i].setHorizontalAlignment(JLabel.RIGHT);
            _checkListPanel.add(labels[i]);
            results[i].setText("n.y.c");
            results[i].setForeground(Color.GRAY);
            _checkListPanel.add(results[i]);
        }

        JButton checkButton = new JButton("Check properties");
        checkListListener = new CheckListAction();
        checkButton.addActionListener(checkListListener);

        /**
         * Clicking the button triggers the creation of a temporary lola net file
         * followed by an import of this file with {@link LolaImportFormat}.
         * The resulting drawing is then displayed.
         */
        final JButton ptnButton = new JButton("Show PTN projection");
        ptnButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DrawApplication app = DrawPlugin.getGui();
                    if (app.drawing() instanceof CPNDrawing) {
                        drawing = (CPNDrawing) app.drawing();
                        File netFile = LolaFileCreator.writeTemporaryLolaFile(drawing);
                        LolaImportFormat lolaImporter = new LolaImportFormat();
                        try {
                            Drawing imported = lolaImporter.importFile(netFile.toURI()
                                                                              .toURL());
                            app.showDrawingViewContainer(imported);
                        } catch (MalformedURLException e3) {
                            logger.error("[Lola GUI - Show PTN projection] Converting the file to a URL failed");
                            e3.printStackTrace();
                        } catch (Exception e3) {
                            logger.error("[Lola GUI - Show PTN projection] Something went wrong with the import");
                            e3.printStackTrace();
                        }
                    } else {
                        logger.error("[Lola GUI - Show PTN projection] Drawing must be CPNDrawing");
                    }
                }
            });

        final JButton drawButton = new JButton("Draw Reach. Graph");
        drawButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DrawApplication app = DrawPlugin.getGui();
                    if (app.drawing() instanceof CPNDrawing) {
                        drawing = (CPNDrawing) app.drawing();
                        File imgFile = drawReachGraph();
                        if (imgFile != null) {
                            BufferedImage img;
                            try {
                                img = ImageIO.read(imgFile);
                                ImageIcon rgIcon = new ImageIcon(img);
                                JFrame rgWindow = new JFrame();
                                rgWindow.add(new JScrollPane(new JLabel(rgIcon)));
                                rgWindow.setSize(300, 600);
                                rgWindow.setTitle(imgFile.getName());

                                JRootPane root = rgWindow.getRootPane();
                                root.putClientProperty("Window.documentFile",
                                                       imgFile);

                                Rectangle p = drawButton.getTopLevelAncestor()
                                                        .getBounds();
                                rgWindow.setLocation(p.x + p.width, p.y);
                                rgWindow.setVisible(true);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });

        _checkListPanel.add(checkButton);
        _checkListPanel.add(drawButton);
        _checkListPanel.add(ptnButton);
    }

    private int findCorrectRow(String value, int column) {
        int result = -1;
        for (int i = 0; i < _markingTable.getRowCount(); i++) {
            if (value.equals(_markingTable.getValueAt(i, column))) {
                result = i;
            }
        }

        return result;
    }

    /**
     * When checkButton is clicked the net properties are
     * checked by the LolaAnalyzer. The result of each property
     * is shown on the right and is either a green checkmark or
     * a red X. Initially the result is a gray n.y.c (not yet checked)
     *
     * @author hewelt, wagner
     */
    class CheckListAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            nameOfNet.setText("...");
            update(getGraphics());
            DrawApplication app = DrawPlugin.getGui();
            if (app.drawing() instanceof CPNDrawing) {
                drawing = (CPNDrawing) app.drawing();
            }
            logger.info("[Lola] performing checklist action with drawing "
                        + drawing.getName());

            LolaAnalyzer analyzer = new LolaAnalyzer(lolaPath);
            LolaFileCreator creator = new LolaFileCreator();
            File tmpLolaFile = LolaFileCreator.writeTemporaryLolaFile(drawing);


            /*
             * Check whether net live / quasi-live
             */
            Map<String, Integer> liveQuasilive = analyzer
                                                     .checkLivenesAndQuasiLiveness(drawing,
                                                                                   creator,
                                                                                   tmpLolaFile);
            if (logger.isDebugEnabled()) {
                logger.debug("[Lola Checklist] Set results for Liveness and Quasi-Liveness");
            }
            colorResultLabel(quasiLivenessResult, liveQuasilive.get("checkDead"));
            colorResultLabel(livenessResult, liveQuasilive.get("checkLive"));

            /*
             * Check whether net is bounded
             * Cave: the exit value for bounded is 1.
             */
            if (logger.isDebugEnabled()) {
                logger.debug("[Lola Checklist] Check whether net is bounded");
            }
            LolaResult boundedResult = analyzer.checkNet(LolaHelper.netBoundedCommand,
                                                         tmpLolaFile);
            colorResultLabel(boundednessResult, boundedResult.getExitValue(),
                             true);

            /*
             * Check whether net is reversible
             */
            if (logger.isDebugEnabled()) {
                logger.debug("[Lola Checklist] Check whether net is reversible");
            }
            LolaResult reversibleResult = analyzer.checkNet(LolaHelper.netReversibleCommand,
                                                            tmpLolaFile);
            colorResultLabel(reversibilityResult,
                             reversibleResult.getExitValue());

            /*
             * Check whether net deadlocks.
             * Cave: the exit value for no deadlock is 1.
             */
            if (logger.isDebugEnabled()) {
                logger.debug("[Lola Checklist] Check whether net deadlocks");
            }
            LolaResult deadlockResult = analyzer.checkNet(LolaHelper.netDeadlockCommand,
                                                          tmpLolaFile);
            colorResultLabel(deadlockFreedomResult,
                             deadlockResult.getExitValue(), true);

            /*
             * Check whether net has home-state
             */
            if (logger.isDebugEnabled()) {
                logger.debug("[Lola Checklist] Check whether net has home-state");
            }
            LolaResult homeResult = analyzer.checkNet(LolaHelper.nethomeStateCommand,
                                                      tmpLolaFile);
            colorResultLabel(homeMarkingResult, homeResult.getExitValue());
        }


        /**
         * This method needs to be used for boundedness and deadlock freedom, because
         * lola returns a 1, if the net is bounded and also a 1, if there is no deadlock.
         *
         * @param label
         * @param val
         * @param reverse
         */
        private void colorResultLabel(JLabel label, int val, boolean reverse) {
            if (reverse) {
                switch (val) {
                case 0:
                    colorResultLabel(label, 1);
                    break;
                case 1:
                    colorResultLabel(label, 0);
                    break;
                default:
                    colorResultLabel(label, val);
                    break;
                }
            }
        }

        /**
         * Changes a given result label according to the result of the net property
         * this label represents. [yes] means, that the net has the property, [no]
         * the opposite and [n/a] that it couldn't be verified by lola (in most cases
         * because the state space is infinite).
         *
         * @param label - which label to color
         * @param val - lola return value
         */
        public void colorResultLabel(JLabel label, int val) {
            switch (val) {
            case 0:
                label.setForeground(new Color(0, 153, 0)); // dark green
                label.setText("[yes]");
                break;
            case 1:
                label.setForeground(Color.RED);
                label.setText("[no]");
                break;
            case 5:
                label.setForeground(Color.GRAY);
                label.setText("[n/a]");
                break;
            default:
                label.setForeground(Color.BLACK);
                label.setText("ERROR");
                label.setToolTipText("An error occured, check the console output for more information.");
                logger.error("[Lola CheckList] The lola call resulted in an unexpected result value: "
                             + val);
                break;
            }
            nameOfNet.setText(drawing.getName());
        }
    }

    /**
     * Get a StringBuffer from the column column in the _markingTable of the marking editor
     * Depending on mode, the StringBuffer can have different forms:
     *
     * mode = 1 for reachability returns a StringBuffer of the form "MARKING p1:x,p2:y". This
     * is used if you want to get an initial or second marking from the table
     *
     * mode = 2 for coverability checks returns a StringBuffer of the form "p1>=x AND p2>=y". This
     * is used if a task for coverability needs to be extracted from th table
     *
     * mode = 3 for home marking checks returns a StringBuffer of the form "p1=x AND p2=y". This
     * is used if a task for home status of a marking needs to be extracted from the table
     *
     * @param column the column in which the marking is input
     * @param mode to be used for the different kind of StringBuffer results
     * @return a StringBuffer with Lola understandable marking text
     */
    public StringBuffer getTableMarking(int column, int mode) {
        String separator = "";
        String comparor = "";
        switch (mode) {
        // For reachability
        case 1: {
            separator = " , ";
            comparor = " : ";
            break;
        }

        // For coverability
        case 2: {
            separator = " AND ";
            comparor = " >= ";
            break;
        }

        // For Home marking
        case 3: {
            separator = " AND ";
            comparor = " = ";
            break;
        }
        }

        StringBuffer result = new StringBuffer();
        if (mode == 1) {
            result.append("MARKING ");
        }
        boolean initial = true;

        for (int i = 0; i < _markingTable.getRowCount(); i++) {
            if (!initial) {
                result.append(separator);
            }
            result.append(_markingTable.getValueAt(i, 0) + comparor);
            if (!(_markingTable.getValueAt(i, column) == null
                        || _markingTable.getValueAt(i, column).equals(""))) {
                result.append(_markingTable.getValueAt(i, column));

            } else {
                result.append("0");
            }
            initial = false;
        }
        return result;
    }

    /**
     * When updateButton is clicked the places and initial marking
     * are read from the current net and displayed in the marking
     * table
     *
     * @author hewelt, wagner
     */
    class UpdatePlacesAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            LolaFileCreator fileCreator = new LolaFileCreator();

            /*
             * Get current drawing
             */
            DrawApplication app = DrawPlugin.getGui();
            if (app.drawing() instanceof CPNDrawing) {
                drawing = (CPNDrawing) app.drawing();
            }
            logger.info("[Lola] performing updatePlacesAction with drawing "
                        + drawing.getName());

            FigureEnumeration figs = drawing.figures();

            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("Place");
            tableModel.addColumn("Initial");
            tableModel.addColumn("Check");
            String[] currentRow = { "", "", "" };
            HashMap<String, String> markingMap;
            String figureName;
            int correctRow;

            /*
             * Check all figures if they are places. If they are find out their name,
             * their marking in the net and if a second marking for that place is already
             * in the table (this is the only thing stored, the rest is overwritten with
             * data from the net)
             * Collect these three things in an Array and then add a row to a new Tablemodel
             * That Tablemodel replaces the previous model. This means we dont have to check
             * for removed places.
             */
            while (figs.hasMoreElements()) {
                Figure fig = figs.nextElement();
                if (fig instanceof PlaceFigure
                            && !(fig instanceof VirtualPlaceFigure)) {
                    figureName = fileCreator.name((FigureWithID) fig);

                    logger.info("[Lola] found place named " + figureName);

                    currentRow[0] = figureName;

                    if (_markingTable.getRowCount() >= tableModel.getRowCount()) {
                        correctRow = findCorrectRow(currentRow[0], 0);
                        if (correctRow > -1) {
                            currentRow[2] = (String) _markingTable.getValueAt(correctRow,
                                                                              2);
                            if (!_updateInitialMarkingCheckbox.isSelected()) {
                                currentRow[1] = (String) _markingTable
                                                    .getValueAt(correctRow, 1);
                            }
                        }
                    }


                    if (_updateInitialMarkingCheckbox.isSelected()) {
                        markingMap = fileCreator.getInitialMarking(drawing);
                        currentRow[1] = markingMap.get(currentRow[0]);
                    }

                    tableModel.addRow(currentRow);
                    logger.info("[Lola] adding marking table column "
                                + currentRow.toString());


                }
            }

            _markingTable.setModel(tableModel);
        }
    }

    /**
     * When reachButton is clicked the reachability of the marking input
     * in the third column of the table is checked against the initial
     * marking inout in the second column of the table for its
     * reachability from that initial marking.
     *
     * @author hewelt, wagner
     */
    class ReachableMarkingAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            LolaAnalyzer analyzer = new LolaAnalyzer(lolaPath);


            /*
             * Get current drawing
             */
            DrawApplication app = DrawPlugin.getGui();
            if (app.drawing() instanceof CPNDrawing) {
                drawing = (CPNDrawing) app.drawing();
            }
            logger.info("[Lola] performing updatePlacesAction with drawing "
                        + drawing.getName());

            /*
             * Get the Marking StringBuffers from the table for both columns
             */
            StringBuffer initialMarking = getTableMarking(1, 1).append(";");
            StringBuffer secondMarking = new StringBuffer("ANALYSE ").append(getTableMarking(2,
                                                                                             1));
            StringBuffer resultMarking = new StringBuffer(getTableMarking(2, 1));

            logger.info("[Lola] Initial marking from table "
                        + initialMarking.toString());
            logger.info("[Lola] second marking from table "
                        + secondMarking.toString());

            /*
             * Only check reachability if there is anything in the third column
             */
            if (!(secondMarking.toString().equals("ANALYSE MARKING "))) {
                File tmpLolaFile = LolaFileCreator.writeTemporaryLolaFile(drawing,
                                                                          initialMarking);
                LolaResult reachResult = analyzer.checkMarking(secondMarking,
                                                               tmpLolaFile);

                /*
                 * Depending on the result, show a positive or negative message
                 */
                if (reachResult.getExitValue() == 0) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "is reachable!",
                                                  "Lola Reachability",
                                                  JOptionPane.INFORMATION_MESSAGE);
                } else if (reachResult.getExitValue() == 1) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "<html>is <b>not</b> reachable! </html>",
                                                  "Lola Reachability",
                                                  JOptionPane.ERROR_MESSAGE);
                } else if (reachResult.getExitValue() > 1) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "is not reachable or cannot be computed!",
                                                  "Lola Reachability",
                                                  JOptionPane.WARNING_MESSAGE);
                }
            } else {
                logger.info("[Lola] No secondary marking input. Cancelling reachability check!");
            }
        }
    }

    /**
     * When the homeButton is clicked, the marking in the third column is checked
     * to be a home state. This is done with the initial marking entered into the
     * second column
     *
     * @author thomaswagner
     *
     */
    class HomeStateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            LolaAnalyzer analyzer = new LolaAnalyzer(lolaPath);


            /*
             * Get current drawing
             */
            DrawApplication app = DrawPlugin.getGui();
            if (app.drawing() instanceof CPNDrawing) {
                drawing = (CPNDrawing) app.drawing();
            }
            logger.info("[Lola] performing updatePlacesAction with drawing "
                        + drawing.getName());

            /*
             * Get the Marking StringBuffers from the table for both columns
             */
            StringBuffer initialMarking = getTableMarking(1, 1).append(";");
            StringBuffer homeMarking = new StringBuffer(getTableMarking(2, 3));
            StringBuffer resultMarking = new StringBuffer(getTableMarking(2, 1));

            logger.info("[Lola] Initial marking from table "
                        + initialMarking.toString());
            logger.info("[Lola] home marking in question from table "
                        + homeMarking.toString());

            /*
             * Only check reachability if there is anything in the third column
             */
            if (!(homeMarking.toString().equals(""))) {
                File tmpLolaFile = LolaFileCreator.writeTemporaryLolaFile(drawing,
                                                                          initialMarking);
                LolaResult reachResult = analyzer.checkStatePredicateLiveness(homeMarking
                                                                              .toString(),
                                                                              tmpLolaFile);
                /*
                 * Depending on the result, show a positive or negative message
                 */
                if (reachResult.getExitValue() == 0) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "is a home marking!",
                                                  "Lola Home Marking",
                                                  JOptionPane.INFORMATION_MESSAGE);
                } else if (reachResult.getExitValue() == 1) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "<html>is <b>not</b> a home marking!</html>",
                                                  "Lola Home Marking",
                                                  JOptionPane.ERROR_MESSAGE);
                } else if (reachResult.getExitValue() > 1) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "is not a home marking or cannot be computed!",
                                                  "Lola Home Marking",
                                                  JOptionPane.WARNING_MESSAGE);
                }
            } else {
                logger.info("[Lola] No home marking input. Cancelling reachability check!");
            }
        }
    }

    /**
     * When the coverButton is clicked, the marking in the third column is checked
     * to be coverable. As initial marking the marking from the second column is
     * used
     *
     * @author thomaswagner
     *
     */
    class CoverabilityAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            LolaAnalyzer analyzer = new LolaAnalyzer(lolaPath);


            /*
             * Get current drawing
             */
            DrawApplication app = DrawPlugin.getGui();
            if (app.drawing() instanceof CPNDrawing) {
                drawing = (CPNDrawing) app.drawing();
            }
            logger.info("[Lola] performing updatePlacesAction with drawing "
                        + drawing.getName());

            /*
             * Get the Marking StringBuffers from the table for both columns
             */
            StringBuffer initialMarking = getTableMarking(1, 1).append(";");
            StringBuffer coverMarking = new StringBuffer(getTableMarking(2, 2));
            StringBuffer resultMarking = new StringBuffer(getTableMarking(2, 1));

            logger.info("[Lola] Initial marking from table "
                        + initialMarking.toString());
            logger.info("[Lola] coverability marking in question from table "
                        + coverMarking.toString());

            /*
             * Only check reachability if there is anything in the third column
             */
            if (!(coverMarking.toString().equals(""))) {
                File tmpLolaFile = LolaFileCreator.writeTemporaryLolaFile(drawing,
                                                                          initialMarking);
                LolaResult reachResult = analyzer
                                             .checkStatePredicateReachability(coverMarking
                                                                              .toString(),
                                                                              tmpLolaFile);
                /*
                 * Depending on the result, show a positive or negative message
                 */
                if (reachResult.getExitValue() == 0) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "is coverable!",
                                                  "Lola Coverability",
                                                  JOptionPane.INFORMATION_MESSAGE);
                } else if (reachResult.getExitValue() == 1) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "<html>is <b>not</b> coverable!</html>",
                                                  "Lola Coverability",
                                                  JOptionPane.ERROR_MESSAGE);
                } else if (reachResult.getExitValue() > 1) {
                    JOptionPane.showMessageDialog(_markEdPanel,
                                                  "The marking \n" + "\n"
                                                  + resultMarking.toString()
                                                                 .substring(8)
                                                  + "\n" + "\n"
                                                  + "is not coverable or cannot be computed!",
                                                  "Lola Coverability",
                                                  JOptionPane.WARNING_MESSAGE);
                }
            } else {
                logger.info("[Lola] No coverable marking input. Cancelling reachability check!");
            }
        }
    }
}