package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;

import CH.ifa.draw.util.Animatable;
import CH.ifa.draw.util.Animator;
import CH.ifa.draw.util.GUIProperties;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;


public class LayoutFrame implements AdjustmentListener, Animatable {
    private static int width = 200;
    private static String[] buttonTexts = { "Spring Length", "Spring Strength", "Repulsion Distance", "Repulsion Strength", "Torque Strength", "Friction" };
    private DrawingEditor editor;
    private Animator animator;
    private GraphLayout graphLayout = null;
    private JFrame frame;
    private JScrollBar[] scroller;
    private JButton startButton;
    private JButton stopButton;

    public LayoutFrame(DrawingEditor editor) {
        this.editor = editor;

        frame = new JFrame("Automatic Net Layout");
        if (!GUIProperties.avoidFrameReshape()) {
            frame.setSize(600, 200);
        }
        GridBagLayout gridBag = new GridBagLayout();
        frame.getContentPane().setLayout(gridBag);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1;

        scroller = new JScrollBar[buttonTexts.length];
        for (int i = 0; i < buttonTexts.length; i++) {
            c.gridwidth = 1;
            c.weightx = 0;
            JLabel label = new JLabel(buttonTexts[i]);
            label.setPreferredSize(new Dimension(150, 20));
            gridBag.setConstraints(label, c);
            frame.getContentPane().add(label);

            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1;
            scroller[i] = new JScrollBar(JScrollBar.HORIZONTAL, width / 2, 10,
                                         0, width);
            scroller[i].addAdjustmentListener(this);
            scroller[i].setPreferredSize(new Dimension(150, 20));
            gridBag.setConstraints(scroller[i], c);
            frame.getContentPane().add(scroller[i]);
        }

        JPanel panel = new JPanel();
        gridBag.setConstraints(panel, c);
        frame.getContentPane().add(panel);

        gridBag = new GridBagLayout();
        panel.setLayout(gridBag);
        panel.setPreferredSize(new Dimension(300, 40));
        c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startAnimation();
                }
            });
        gridBag.setConstraints(startButton, c);
        panel.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stopAnimation();
                }
            });
        gridBag.setConstraints(stopButton, c);
        panel.add(stopButton);

        JButton button = new JButton("Close");
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stopAnimation();
                    frame.setVisible(false);
                    DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                              .removeFrame(frame);
                }
            });
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(button, c);
        panel.add(button);

        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent w) {
                    stopAnimation();
                    frame.setVisible(false);
                    DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                              .removeFrame(frame);
                }
            });

        frame.pack();
    }

    public Dimension getPreferredSize() {
        return new Dimension(600, 200);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void stopAnimation() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        if (animator != null) {
            animator.end();
            animator = null;
        }
    }

    public void startAnimation() {
        stopAnimation();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        DrawingView view = editor.view();
        Drawing drawing = view.drawing();
        if (drawing instanceof LayoutableDrawing) {
            // Setup the graphics.
            frame.setTitle("Automatic Layout - " + drawing.getName());
            frame.setVisible(true);
            DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                      .addFrame(DrawPlugin.WINDOWS_CATEGORY_TOOLS, frame);

            //Take Undo-Snapshot.
            editor.prepareUndoSnapshot();
            editor.commitUndoSnapshot();

            // Setup the layout algorithm.
            graphLayout = new GraphLayout();
            ((LayoutableDrawing) drawing).fillInGraph(graphLayout);
            fillInScrollerValues();

            // Make sure the layout is relaxed periodically.
            animator = new Animator(this, view);
            new Thread(animator).start();
        }
    }


    /**
     * This method allows programmers to layout their drawings with given
     * parameters for a certain time, without explicitly opening the
     * Automatic Layout frame. If the given time is below below or equal to 0
     * the frame is opened anyways.
     *
     * @param springLength                        Value range from 0.0 to 0.9025 where 0.25 is standard value
     * @param springStrength                Value range from 0.0 to 0.9025 where 0.25 is standard value
     * @param repulsionDistance                Value range from 0.0 to 0.9025 where 0.25 is standard value
     * @param repulsionStrength                Value range from 0.0 to 0.9025 where 0.25 is standard value
     * @param torqueStrength                Value range from 0.0 to 0.9025 where 0.25 is standard value
     * @param friction                                Value range from 0.0 to 0.9025 where 0.25 is standard value
     * @param timeInMillis  For numbers less or equal to 0 the Automatic Layout Menu is opened.
     *                                                 For numbers from 0-999999 the drawing will be layouted for exactly that
     *                                                 time in milliseconds. Greater numbers will cause exceptions.
     */
    public void createLayout(double springLength, double springStrength,
                             double repulsionDistance,
                             double repulsionStrength, double torqueStrength,
                             double friction, long timeInMillis) {
        stopAnimation();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        DrawingView view = editor.view();
        Drawing drawing = view.drawing();
        if (drawing instanceof LayoutableDrawing) {
            //Take Undo-Snapshot.
            editor.prepareUndoSnapshot();
            editor.commitUndoSnapshot();

            // Setup the layout algorithm.
            graphLayout = new GraphLayout();
            ((LayoutableDrawing) drawing).fillInGraph(graphLayout);
            fillInScrollerValues();

            // Make sure the layout is relaxed periodically.
            animator = new Animator(this, view);
            new Thread(animator).start();

            graphLayout.LENGTH_FACTOR = 1.0 * 4 * springLength;
            graphLayout.SPRING_STRENGTH = 0.1 * 4 * springStrength;
            graphLayout.REPULSION_LIMIT = 200.0 * 4 * repulsionDistance;
            graphLayout.REPULSION_STRENGTH = 0.5 * 4 * repulsionStrength;
            graphLayout.TORQUE_STRENGTH = 0.25 * 4 * torqueStrength;
            graphLayout.FRICTION_FACTOR = 1 - friction;

            if (timeInMillis <= 0) {
                // Setup the graphics.
                frame.setTitle("Automatic Layout - " + drawing.getName());
                frame.setVisible(true);
                DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                          .addFrame(DrawPlugin.WINDOWS_CATEGORY_TOOLS, frame);
            } else {
                try {
                    Thread.sleep(timeInMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopAnimation();
        }
    }

    private static double calcFactor(int value) {
        double n = value / (double) width;
        return n * n;
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        fillInScrollerValues();
    }

    private void fillInScrollerValues() {
        int num = 0;
        if (graphLayout != null) {
            graphLayout.LENGTH_FACTOR = 1.0 * 4 * calcFactor(scroller[num++]
                                            .getValue());
            graphLayout.SPRING_STRENGTH = 0.1 * 4 * calcFactor(scroller[num++]
                                              .getValue());
            graphLayout.REPULSION_LIMIT = 200.0 * 4 * calcFactor(scroller[num++]
                                                                 .getValue());
            graphLayout.REPULSION_STRENGTH = 0.5 * 4 * calcFactor(scroller[num++]
                                                                  .getValue());
            graphLayout.TORQUE_STRENGTH = 0.25 * 4 * calcFactor(scroller[num++]
                                                                .getValue());
            graphLayout.FRICTION_FACTOR = 1
                                          - calcFactor(scroller[num++].getValue());
        }
    }

    synchronized public void animationStart() {
        // Nothing to do.
    }

    synchronized public void animationStop() {
        graphLayout.remove();
        graphLayout = null;
    }

    synchronized public void animationStep() {
        if (graphLayout != null) {
            graphLayout.relax();
        }
    }
}