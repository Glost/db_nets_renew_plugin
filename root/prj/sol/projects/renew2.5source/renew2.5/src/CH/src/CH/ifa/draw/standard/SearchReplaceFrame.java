package CH.ifa.draw.standard;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.util.GUIProperties;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** Constructs a SearchFrame or SearchAndReplaceFrame
 *  according to the selected mode and displays it.
 *
 *    * @author Lawrence Cabac, Marcin Hewelt (changes 2010-2012)
*/
public class SearchReplaceFrame {
    public static final int SEARCHMODE = 0;
    public static final int SEARCHREPLACEMODE = 1;

    // private DrawApplication application;
    private Enumeration<Drawing> drawings;
    private Set<Drawing> drawingsSet;
    private FigureEnumeration figures;
    private Drawing drawing;
    private JFrame frame;
    private boolean stringMatcherValid = false;
    private SubstringMatcher substringMatcher;
    private int mode;
    private boolean cancelSearchInAllDrawings = true;
    private boolean searchAll = true;
    private boolean ignoreCase = true;
    private boolean newSearch = true;

    public SearchReplaceFrame(int aMode) {
        String frameTitle;
        String buttonTitle;

        // this.application = application;
        mode = aMode;

        if (mode == SEARCHMODE) {
            frameTitle = "Search";
        } else {
            frameTitle = "Search & Replace";
        }

        frame = new JFrame(frameTitle);

        if (!GUIProperties.avoidFrameReshape()) {
            frame.setSize(600, 200);
        }
        GridBagLayout gridBag = new GridBagLayout();
        frame.getContentPane().setLayout(gridBag);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;

        JLabel label = new JLabel("Search for:");
        gridBag.setConstraints(label, c);
        frame.getContentPane().add(label);

        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        final JTextField searchTextField = new JTextField("", 30);
        gridBag.setConstraints(searchTextField, c);
        searchTextField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    reset(); //search string is modified: reset search process
                }
            });
        frame.getContentPane().add(searchTextField);

        final JTextField replaceTextField = new JTextField("", 30);
        if (mode == SEARCHREPLACEMODE) {
            c.gridwidth = 1;
            label = new JLabel("Replace with:");
            gridBag.setConstraints(label, c);
            frame.getContentPane().add(label);

            c.gridwidth = GridBagConstraints.REMAINDER; //end row
            gridBag.setConstraints(replaceTextField, c);
            replaceTextField.addKeyListener(new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        stringMatcherValid = false; //replace string is modified
                    }
                });
            frame.getContentPane().add(replaceTextField);
        }

        final JCheckBox searchAllCheckBox = new JCheckBox("Search all drawings",
                                                          searchAll);
        frame.getContentPane().add(searchAllCheckBox);
        searchAllCheckBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    searchAll = !searchAll;
                    reset();
                }
            });
        c.gridwidth = GridBagConstraints.EAST;
        gridBag.setConstraints(searchAllCheckBox, c);

        final JCheckBox ignoreCaseCheckBox = new JCheckBox("Ignore case",
                                                           ignoreCase);
        frame.getContentPane().add(ignoreCaseCheckBox);
        ignoreCaseCheckBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    ignoreCase = !ignoreCase;
                }
            });
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridBag.setConstraints(ignoreCaseCheckBox, c);

        c.weightx = 1;

        if (mode == SEARCHMODE) {
            buttonTitle = "Search";
        } else {
            buttonTitle = "Search & Replace";
        }
        JButton searchButton = new JButton(buttonTitle);
        gridBag.setConstraints(searchButton, c);
        frame.getContentPane().add(searchButton);
        frame.getRootPane().setDefaultButton(searchButton);
        searchButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if ("".equals(searchTextField.getText())) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    if (!stringMatcherValid) {
                        if (mode == SEARCHMODE) {
                            substringMatcher = new SubstringMatcher(searchTextField
                                                                    .getText(),
                                                                    null);
                        } else {
                            substringMatcher = new SubstringMatcher(searchTextField
                                                                    .getText(),
                                                                    replaceTextField
                                                                    .getText());
                        }
                        stringMatcherValid = true;
                    }
                    searchInDrawings();
                }
            });

        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        JButton cancelButton = new JButton("Cancel");
        gridBag.setConstraints(cancelButton, c);
        frame.getContentPane().add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });

        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent w) {
                    frame.setVisible(false);
                    MenuManager.getInstance().getWindowsMenu().removeFrame(frame);
                }
            });

        //frame.validate();
        frame.pack();

        reset();

    }

    /**
     * Handles a found TextFigure wrt. the actual mode. In case of a
     * SEARCHREPLACEMODE this is done by prompting the user with the
     * replacement.
     */
    private void handleFoundFigure(TextFigure foundFigure) {
        if (mode == SEARCHREPLACEMODE) {
            String foundText = foundFigure.getText();
            String replacement = "";
            int nextFromIndex = 0;
            int index;
            int answer;
            while (substringMatcher.matches(foundText, nextFromIndex, ignoreCase)) {
                replacement = substringMatcher.replacement(foundText,
                                                           nextFromIndex);
                index = substringMatcher.indexOf(foundText, nextFromIndex);
                answer = JOptionPane.showConfirmDialog(frame,
                                                       "Replace:\n" + foundText
                                                       + "\nwith:\n"
                                                       + replacement,
                                                       "Confirm Replacement",
                                                       JOptionPane.YES_NO_CANCEL_OPTION);

                switch (answer) {
                case JOptionPane.CANCEL_OPTION:
                    reset();
                    cancelSearchInAllDrawings = true;
                    setVisible(false);
                    return;
                case JOptionPane.YES_OPTION:
                    DrawApplication application = DrawPlugin.getGui();
                    application.prepareUndoSnapshot();
                    foundFigure.setText(replacement);
                    foundText = replacement;
                    nextFromIndex = index
                                    + substringMatcher.getReplaceString()
                                                      .length();
                    application.commitUndoSnapshot();
                    application.view().checkDamage();
                    break;
                case JOptionPane.NO_OPTION:
                    nextFromIndex = index
                                    + substringMatcher.getSearchString().length();
                    break;
                }
            }
        }
    }

    /**
     * Searches for a matching string in @see CH.ifa.draw.figures.TextFigure s
     *   and calls @see handleFoundFigure(TextFigure t) for each match.
     *
     * @author  Michael Koehler, Heiko Roelke
     *
     *
     */
    private void searchInDrawings() {
        if (mode == SEARCHREPLACEMODE) {
            reset(); //init search and replace
        } else if (!drawing.equals(DrawPlugin.getGui().drawing()) && !searchAll) { // if the current drawing changed the search has to be reset.
            reset();
        }
        if (mode == SEARCHMODE && searchAll) {
            Set<Drawing> currentDrawingsSet = new HashSet<Drawing>();
            Enumeration<Drawing> currentDrawings = DrawPlugin.getGui().drawings();
            while (currentDrawings.hasMoreElements()) {
                currentDrawingsSet.add(currentDrawings.nextElement());
            }
            if (!currentDrawingsSet.equals(drawingsSet)) {
                // reset if a drawing is closed or opened
                reset();
            }
        }

        TextFigure foundFigure = null;
        this.cancelSearchInAllDrawings = false;
        while (!cancelSearchInAllDrawings
                       && (drawings.hasMoreElements() || drawing != null)) {
            if (newSearch) { //start a new search in first/next drawing
                newSearch = false;
                if (drawings.hasMoreElements()) {
                    drawing = drawings.nextElement();
                    figures = drawing.figures();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                    return; //no more drawings :((   
                }
            }

            foundFigure = searchInOneDrawing();
            if (foundFigure != null) {
                DrawApplication application = DrawPlugin.getGui();

                if (mode == SEARCHMODE) {
                    FigureException e = new FigureException("Heureka", 1,
                                                            substringMatcher
                                            .getLastposition() + 1, drawing,
                                                            foundFigure);
                    application.selectOffendingElements(e);
                    return;
                } else {
                    FigureException e = new FigureException("Heureka", "",
                                                            drawing, foundFigure);
                    application.selectOffendingElements(e);
                    handleFoundFigure(foundFigure);
                }
            } else {
                // no more figures in this drawing, next one.
                newSearch = true;
                drawing = null;
            }
        }
        if (!cancelSearchInAllDrawings
                    && !figures.hasMoreElements() & foundFigure == null) {
            Toolkit.getDefaultToolkit().beep();
            // optional: reset at this point provides automatic wrap of search
            reset();
        }
    }

    /**
     * Warning figures is a Enumeration field. Each call continues with the
     * search instead of starting a new one.
     * @return next found matching figure in search context
     */
    private TextFigure searchInOneDrawing() {
        while (figures.hasMoreElements()) {
            Figure figure = figures.nextFigure();
            if (figure instanceof TextFigure) {
                TextFigure textFigure = (TextFigure) figure;
                String figureText = textFigure.getText();
                if (substringMatcher.matches(figureText, ignoreCase)) {
                    return textFigure; //Heureka!
                }
            }
        }
        return null;
    }

    /**
     * Resets the search or search and replace context to start a new search.
     * Either for all drawing or for the active drawing in GUI depending on
     * checkbox "Search all drawings" settings.
     */
    protected void reset() {
        DrawApplication application = DrawPlugin.getGui();
        if (application != null) {
            application.tool().deactivate();
            drawing = application.drawing();

            if (searchAll) {
                // search in all open drawings
                drawings = application.drawings();
                drawingsSet = new HashSet<Drawing>();
                while (drawings.hasMoreElements()) {
                    drawingsSet.add(drawings.nextElement());
                }
                drawings = application.drawings();
            } else {
                // search in the active drawing
                Vector<Drawing> list = new Vector<Drawing>();
                list.add(drawing);
                drawings = list.elements();
                drawingsSet = new HashSet<Drawing>(list);
            }

            figures = drawing.figures();
            newSearch = true;
            stringMatcherValid = false;
        }
    }

    private void cancel() {
        this.setVisible(false);
        DrawPlugin.getGui().toolDone();
    }

    protected void setVisible(boolean b) {
        frame.setVisible(b);
        if (b) {
            MenuManager.getInstance().getWindowsMenu()
                       .addFrame(DrawPlugin.WINDOWS_CATEGORY_TOOLS, frame);
        } else {
            MenuManager.getInstance().getWindowsMenu().removeFrame(frame);
        }
    }
}