package de.renew.gui;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeAdapter;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.FigureException;
import CH.ifa.draw.standard.TextHolder;

import de.renew.formalism.java.ConstructorSuggestion;
import de.renew.formalism.java.FieldSuggestion;
import de.renew.formalism.java.MethodSuggestion;
import de.renew.formalism.java.Suggestion;
import de.renew.formalism.java.VariableSuggestion;

import de.renew.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


class SyntaxExceptionFrame extends FigureChangeAdapter {
    private final JFrame frame;
    private final JButton selectButton;
    private final JTextArea textArea;
    private final CPNApplication editor;
    private FigureException e = null;
    private JPanel myPanel;
    private JList listOfSuggestions;
    private DefaultListModel listModel;
    private JTextArea methodListHeader;
    private JScrollPane scrollPane;
    private JButton applyButton;
    private QuickfixListener quickfixListener;

    SyntaxExceptionFrame(final CPNApplication editor) {
        this.editor = editor;
        frame = new JFrame("Renew: Syntax Error");
        frame.setIconImage(Toolkit.getDefaultToolkit()
                                  .createImage(editor.getIconImage().getSource()));
        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.getContentPane().add("Center", textArea);
        JPanel southPanel = new JPanel(new GridLayout(1, 2));
        frame.getContentPane().add("South", southPanel);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    frame.dispose();
                }
            });
        selectButton = new JButton("Select");
        selectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    GuiPlugin.getCurrent().getGui()
                             .showDrawingViewContainer(e.errorDrawing);
                    if (e != null && !editor.selectOffendingElements(e)) {
                        selectButton.setEnabled(false);
                    }
                }
            });
        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        southPanel.add("1", closeButton);
        southPanel.add("2", selectButton);
        southPanel.add("3", applyButton);
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    dispose();
                }
            });
        frame.getRootPane().setDefaultButton(selectButton);

    }

    void displayException(FigureException e, boolean displayImmediately) {
        if (displayImmediately) {
            selectButton.setEnabled(editor.selectOffendingElements(e));
        } else {
            selectButton.setEnabled(true);
        }

        this.e = e;

        setFrame();

        FigureEnumeration errorFigures = new FigureEnumerator(e.errorFigures);
        while (errorFigures.hasMoreElements()) {
            errorFigures.nextFigure().addFigureChangeListener(this);
        }

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Modifies the {@link JFrame} depending on whether the
     * {@link FigureException} stored in this object
     * contains additional information
     */
    private void setFrame() {
        String headMessage = e.getMessage();
        frame.setTitle(e.title);

        Object o = e.getProblemSpecificInformation();
        if (o == null) {
            if (myPanel != null) {
                frame.remove(myPanel);
            }
            textArea.setText(headMessage);
            frame.getContentPane().add("Center", textArea);
            applyButton.setEnabled(false);
            frame.getRootPane().setDefaultButton(selectButton);
        } else {
            if (quickfixListener == null) {
                quickfixListener = new QuickfixListener();
                applyButton.addActionListener(quickfixListener);
            }
            applyButton.setEnabled(true);
            frame.getRootPane().setDefaultButton(applyButton);

            if (listOfSuggestions == null) {
                listOfSuggestions = new JList();
                listOfSuggestions.addMouseListener(quickfixListener);
                listOfSuggestions.addKeyListener(quickfixListener);
                frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowOpened(WindowEvent e) {
                            listOfSuggestions.requestFocus();
                        }
                    });
            }

            if (myPanel == null) {
                myPanel = new JPanel();
                myPanel.setLayout(new BorderLayout());
            }

            // use a new list model every time to prevent triggering
            // gui related stuff on each addition of an element. 
            listModel = new DefaultListModel();

            if (methodListHeader == null) {
                methodListHeader = new JTextArea();
                methodListHeader.setEditable(false);
                methodListHeader.setEnabled(false);
            }
            if (scrollPane == null) {
                scrollPane = new JScrollPane();
                scrollPane.setViewportView(listOfSuggestions);
            }

            methodListHeader.setText(headMessage);

            frame.remove(textArea);

            if (o instanceof Collection<?> && !((Collection) o).isEmpty()
                        && ((Collection) o).iterator().next() instanceof Suggestion) {
                for (Suggestion sug : ((Collection<Suggestion>) o)) {
                    listModel.addElement(sug);
                }
            }

            listOfSuggestions.setModel(listModel);
            if (!listModel.isEmpty()) {
                listOfSuggestions.setSelectedIndex(0);
            }

            myPanel.add(methodListHeader, BorderLayout.NORTH);
            myPanel.add(scrollPane, BorderLayout.CENTER);
            frame.getContentPane().add("Center", myPanel);
        }
    }

    public void figureRemoved(FigureChangeEvent e) {
        dispose();
    }

    public void dispose() {
        if (e != null) {
            frame.dispose();
            FigureEnumeration errorFigures = new FigureEnumerator(e.errorFigures);
            while (errorFigures.hasMoreElements()) {
                errorFigures.nextFigure().removeFigureChangeListener(this);
            }
            e = null;
        }
    }

    private class QuickfixListener implements MouseListener, KeyListener,
                                              ActionListener {
        private void quickfix() {
            Object selectedValue = listOfSuggestions.getSelectedValue();

            editor.getUndoRedoManager().commitUndoSnapshot(editor.drawing());
            if (selectedValue instanceof Suggestion) {
                TextHolder th = e.textErrorFigure;
                String text = th.getText();

                // the error column points at the dot before the method or field name. 
                int i = getIndexFromLineAndColumn(text, e.line, e.column);
                String textPrefix = text.substring(0, i);
                String textSuffix = text.substring(i);

                boolean select;
                boolean edit;
                int lineStart = e.line;
                int lineEnd = e.line;
                int columnStart;
                int columnEnd = e.column;
                TextFigure tf;
                String newText;

                if (selectedValue instanceof MethodSuggestion) {
                    edit = true;
                    MethodSuggestion suggestion = (MethodSuggestion) selectedValue;
                    String name = suggestion.getAttemptedMethod();

                    Class<?>[] attemptedParameterTypes = suggestion
                                                             .getAttemptedTypes();
                    Class<?>[] selectedParameterTypes = suggestion.getMethod()
                                                                  .getParameterTypes();
                    boolean parametersMatch = false;
                    if (attemptedParameterTypes.length == selectedParameterTypes.length) {
                        parametersMatch = true;
                        for (int k = 0; k < attemptedParameterTypes.length;
                                     k++) {
                            if (!collectTypes(attemptedParameterTypes[k])
                                             .contains(selectedParameterTypes[k])) {
                                parametersMatch = false;
                            }
                        }
                    }

                    if (parametersMatch) {
                        textSuffix = textSuffix.replaceFirst(name,
                                                             suggestion.getName());
                        select = false;
                        // not the best position for the cursor but computing a better one is not so easy
                        columnStart = e.column + ".".length()
                                      + suggestion.getName().length();

                    } else if (textSuffix.startsWith("." + name + "()")) {
                        textSuffix = textSuffix.replaceFirst(name + "\\(\\)",
                                                             suggestion
                                         .getCallWithParameters());
                        if (suggestion.getParameters().length > 0) {
                            select = true;
                            columnStart = e.column + ".".length()
                                          + suggestion.getName().length()
                                          + "(".length();
                            columnEnd = columnStart
                                        + suggestion.getParameters()[0].length(); // select the first parameter
                        } else {
                            select = false;
                            columnStart = e.column + ".".length()
                                          + suggestion.getName().length()
                                          + "()".length();
                        }
                    } else {
                        textSuffix = textSuffix.replaceFirst(name,
                                                             suggestion
                                         .getCallWithParameters());
                        if (suggestion.getParameters().length > 0) {
                            select = true;
                            columnStart = e.column + ".".length()
                                          + suggestion.getName().length()
                                          + "(".length();
                            columnEnd = columnStart
                                        + suggestion.getParameters()[0].length(); // select the first parameter
                        } else {
                            select = false;
                            columnStart = e.column + ".".length()
                                          + suggestion.getName().length()
                                          + "()".length();
                        }
                    }
                    newText = textPrefix + textSuffix;
                    tf = (TextFigure) th;
                } else if (selectedValue instanceof ConstructorSuggestion) {
                    edit = select = false;
                    ConstructorSuggestion constructorSuggestion = (ConstructorSuggestion) selectedValue;
                    textPrefix = text.substring(0, text.indexOf('=') + 1);
                    textSuffix = " new "
                                 + constructorSuggestion.getCallWithParameters();
                    columnStart = textPrefix.length()
                                  + constructorSuggestion.getTypeName().length()
                                  + 7;

                    if (constructorSuggestion.getParameters().length > 0) {
                        edit = select = true;
                        columnEnd = columnStart
                                    + constructorSuggestion.getParameters()[0]
                                        .length();
                    }

                    newText = textPrefix + textSuffix;
                    tf = (TextFigure) th;
                } else if (selectedValue instanceof FieldSuggestion) {
                    FieldSuggestion fieldSuggestion = (FieldSuggestion) selectedValue;
                    textSuffix = textSuffix.replaceFirst(fieldSuggestion
                                     .getAttemptedName(),
                                                         fieldSuggestion.getName());
                    select = false;
                    edit = true;
                    columnStart = e.column + 1
                                  + fieldSuggestion.getName().length();
                    newText = textPrefix + textSuffix;
                    tf = (TextFigure) th;
                } else if (selectedValue instanceof VariableSuggestion) {
                    VariableSuggestion sug = (VariableSuggestion) selectedValue;
                    select = true;
                    edit = sug.isEditDesired();
                    FigureEnumeration figures = e.errorDrawing.figures();
                    DeclarationFigure declNode = null;
                    while (figures.hasMoreElements()) {
                        Figure fig = figures.nextElement();
                        if (fig instanceof DeclarationFigure) {
                            declNode = (DeclarationFigure) fig;
                            break;
                        }
                    }

                    if (declNode == null) {
                        throw new RuntimeException("no declaration node found.");
                    }

                    newText = declNode.getText();
                    lineStart = countLines(newText) + 1;
                    lineEnd = lineStart;
                    newText += '\n' + sug.getTypeName() + ' ' + sug.getName()
                    + ';';
                    columnStart = 1;
                    columnEnd = sug.getTypeName().length() + 1;

                    // import variable type if needed
                    if (sug.isImportNeeded()) {
                        String importStatement = "import "
                                                 + sug.getType().getPackage()
                                                      .getName() + ".*;";
                        List<String> lines = new ArrayList(Arrays.asList(newText
                                                                         .split("\n")));
                        int index = 0;
                        for (int j = lines.size() - 1; j >= 0; j--) {
                            String line = lines.get(j);
                            if (line.startsWith("import")) {
                                index = j;
                            }
                        }

                        if (index != 0) {
                            index++;
                        }
                        lines.add(index, importStatement);
                        newText = StringUtil.join(lines, "\n");
                    }

                    tf = declNode;

                } else {
                    throw new RuntimeException("unsupported suggestion type");
                }

                editor.toolDone(); // exit editing mode if TextFigure is already selected
                tf.setText(newText);
                editor.toolDone(); // apply changed text in TextFigure

                if (edit) {
                    if (select) {
                        editor.doTextEditSelected(tf, lineStart, columnStart,
                                                  lineEnd, columnEnd);
                    } else {
                        editor.doTextEdit(tf, lineStart, columnStart);
                    }
                }
            }

            dispose();
            listModel.clear();
        }

        private int getIndexFromLineAndColumn(String text, int line, int column) {
            int result = -1;
            String pattern = "^([^\n]*\n){" + (line - 1) + "}.{" + (column - 1)
                             + "}";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            if (m.find()) {
                result = m.end();
            }
            return result;
        }

        private int countLines(String text) {
            int result = 1;
            String pattern = "\n";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            while (m.find()) {
                result += 1;
            }
            return result;
        }

        private Set<Class<?>> collectTypes(Class<?> clazz) {
            Set<Class<?>> allTypes = new HashSet<Class<?>>();

            while (clazz != null) {
                allTypes.add(clazz);
                Class<?>[] interfaces = clazz.getInterfaces();
                List<Class<?>> interfacesAsList = Arrays.asList(interfaces);
                allTypes.addAll(interfacesAsList);
                clazz = clazz.getSuperclass();
            }

            return allTypes;
        }

        @Override
        public void mouseReleased(MouseEvent arg0) {
        }

        @Override
        public void mousePressed(MouseEvent arg0) {
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
            if (arg0.getClickCount() == 2) {
                quickfix();
            }
        }

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyReleased(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                quickfix();
            }
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            quickfix();
        }
    }
}