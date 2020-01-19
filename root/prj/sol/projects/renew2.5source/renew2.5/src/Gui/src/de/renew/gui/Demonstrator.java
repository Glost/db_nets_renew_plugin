package de.renew.gui;

import CH.ifa.draw.framework.Drawing;

import de.renew.application.SimulatorPlugin;

import de.renew.plugin.command.CLCommand;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;


public class Demonstrator implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Demonstrator.class);
    private static JFrame frame = null;
    private static JList list = null;
    private static DefaultListModel listModel = null;

    /* specified by the CLCommand interface */
    public String getDescription() {
        return "opens a window with a list of drawing file names";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "fileNames";
    }

    /* specified by the CLCommand interface */
    public void execute(String[] args, PrintStream response) {
        response.println("The Renew demonstration GUI");
        if (args.length == 0) {
            response.println("Usage: demonstrator files...");
            response.println("or     demonstrator -conffile");
        } else if (args[0].startsWith("-")) {
            try {
                FileReader fileReader = new FileReader(args[0].substring(1));
                BufferedReader reader = new BufferedReader(fileReader);

                Vector<String> names = new Vector<String>();
                while (reader.ready()) {
                    String name = reader.readLine();
                    if (name != null && !name.equals("")) {
                        names.addElement(name);
                    }
                }
                String[] arr = new String[names.size()];
                names.copyInto(arr);
                setup(arr);
                reader.close();
            } catch (Exception e) {
                response.println("Error: could not access configuration file.");
                logger.error("Could not access configuration file.", e);
            }
        } else {
            setup(args);
        }
    }

    /**
     * Shows a frame with the given array of filenames.
     *
     * @param args an array of filenames
     **/
    public static synchronized void setup(final String[] args) {
        if (frame == null) {
            frame = new JFrame("Demonstration Drawings");

            frame.setSize(600, 200);
            GridBagLayout gridBag = new GridBagLayout();
            frame.getContentPane().setLayout(gridBag);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;

            listModel = new DefaultListModel();
            list = new JList(listModel);
            list.addMouseListener(new ActionJList(list));
            gridBag.setConstraints(list, c);
            frame.getContentPane().add(list);

            JPanel panel = new JPanel();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0;
            gridBag.setConstraints(panel, c);
            frame.getContentPane().add(panel);

            gridBag = new GridBagLayout();
            panel.setLayout(gridBag);
            c = new GridBagConstraints();
            c.weightx = 1;
            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = 1;

            JButton button;
            button = new JButton("Replace");
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        replaceDrawings();
                    }
                });
            gridBag.setConstraints(button, c);
            panel.add(button);

            button = new JButton("Include");
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        openDrawings();
                    }
                });
            gridBag.setConstraints(button, c);
            panel.add(button);

            button = new JButton("Exit");
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        GuiPlugin.getCurrent().closeGui();
                        frame.setVisible(false);
                    }
                });
            gridBag.setConstraints(button, c);
            panel.add(button);

        }

        listModel.removeAllElements();
        for (int i = 0; i < args.length; i++) {
            listModel.addElement(args[i]);
        }

        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    frame.setVisible(true);
                }
            });
    }

    static synchronized void replaceDrawings() {
        CPNApplication app = GuiPlugin.getCurrent().getGui();
        if (app == null) {
            GuiPlugin.getCurrent().openGui();
            app = GuiPlugin.getCurrent().getGui();
        }

        int i = list.getSelectedIndex();
        if (i >= 0 && i < listModel.size()) {
            SimulatorPlugin.getCurrent().terminateSimulation();
            app.cleanupSimulationWindows();

            Enumeration<Drawing> enumeration = app.drawings();
            while (enumeration.hasMoreElements()) {
                Drawing drawing = enumeration.nextElement();
                app.closeDrawing(drawing);
            }
        }

        openDrawings();
    }

    private static synchronized void openDrawings() {
        CPNApplication app = GuiPlugin.getCurrent().getGui();
        if (app == null) {
            GuiPlugin.getCurrent().openGui();
            app = GuiPlugin.getCurrent().getGui();
        }

        int i = list.getSelectedIndex();
        if (i >= 0 && i < listModel.size()) {
            StringTokenizer tokenizer = new StringTokenizer((String) listModel
                                            .elementAt(i), ",", false);
            Vector<String> tokens = new Vector<String>();
            while (tokenizer.hasMoreTokens()) {
                tokens.add(tokenizer.nextToken());
            }
            app.loadAndOpenCommandLineDrawings(tokens.toArray(new String[tokens
                                                                         .size()]));
        }
    }

    public static synchronized void cleanup() {
        if (frame != null) {
            final JFrame finalFrame = frame;
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        finalFrame.setVisible(false);
                        finalFrame.dispose();
                    }
                });
        }
        frame = null;
        list = null;
        listModel = null;
    }
}

class ActionJList extends MouseAdapter {
    protected JList list;

    public ActionJList(JList l) {
        list = l;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = list.locationToIndex(e.getPoint());
            list.ensureIndexIsVisible(index);
            Demonstrator.replaceDrawings();
            //System.out.println("Double clicked on " + item);
        }
    }
}