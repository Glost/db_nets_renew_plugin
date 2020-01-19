package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Command to change a font.
 * <p>
 * The command's effects are undoable step by step.
 * Each use of the apply button can be undone separately.
 * So this command doesn't need to inherit UndoableCommand.
 * </p>
 * @author Sven Offermann
 */
public class ChooseFontCommand extends ChooseAttributeCommand {
    private JList fontList;
    private JList sizeList;
    private JCheckBox cbBold;
    private JCheckBox cbItalic;
    private JTextArea txtSample;
    private static final int[] sizes = new int[] { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 30, 36, 48, 72 };
    private SizeListModel sizeModel;

    /**
     * Constructs a dialog with a FontChooser to
     * change the font of inscriptions etc.
     *
     * @param displayName the dialog name
     * @param name the command name
     * @param attributeName the name of the attribute to be changed
     * @param type the attribute type (class)
     */
    public ChooseFontCommand(String displayName, String name,
                             String attributeName, Class<?> type) {
        super(displayName, name, attributeName, type);
    }

    protected void specializeDialog() {
        // create all components
        fontList = new JList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                .getAvailableFontFamilyNames()) {
                public Dimension getPreferredScrollableViewportSize() {
                    return new Dimension(150, 144);
                }
            };
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sizeModel = new SizeListModel(sizes);
        sizeList = new JList(sizeModel) {
                public Dimension getPreferredScrollableViewportSize() {
                    return new Dimension(25, 144);
                }
            };
        sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cbBold = new JCheckBox("Bold");

        cbItalic = new JCheckBox("Italic");

        txtSample = new JTextArea() {
                public Dimension getPreferredScrollableViewportSize() {
                    return new Dimension(385, 80);
                }
            };
        txtSample.setText("This is a sample text.");

        // set the default font
        setFont(null);

        // add the listeners
        ListSelectionListener listListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                txtSample.setFont(getCurrentFont());
            }
        };

        fontList.addListSelectionListener(listListener);
        sizeList.addListSelectionListener(listListener);


        ActionListener cbListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtSample.setFont(getCurrentFont());
            }
        };

        cbBold.addActionListener(cbListener);
        cbItalic.addActionListener(cbListener);

        JPanel fontChoosePanel = new JPanel();
        fontChoosePanel.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.add(new JScrollPane(fontList), java.awt.BorderLayout.CENTER);
        leftPanel.add(new JScrollPane(sizeList), java.awt.BorderLayout.EAST);

        fontChoosePanel.add(leftPanel, java.awt.BorderLayout.CENTER);


        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new java.awt.FlowLayout());

        rightPanel.add(cbBold);
        rightPanel.add(cbItalic);

        fontChoosePanel.add(rightPanel, java.awt.BorderLayout.EAST);

        fontChoosePanel.add(new JScrollPane(txtSample),
                            java.awt.BorderLayout.SOUTH);

        dialog.getContentPane().add(fontChoosePanel, BorderLayout.CENTER);

        dialog.setSize(200, 200);
        dialog.setResizable(true);

        dialog.pack();
    }

    private void setFont(Font font) {
        if (font == null) {
            font = txtSample.getFont();
        }

        fontList.setSelectedValue(font.getName(), true);
        fontList.ensureIndexIsVisible(fontList.getSelectedIndex());
        Integer sizeInt = Integer.valueOf(font.getSize());
        sizeList.setSelectedValue(sizeInt, true);
        if (!sizeInt.equals(sizeList.getSelectedValue())) {
            // The current font size is missing in the list.
            // Let's add it and select it again.
            sizeModel.addSize(sizeInt);
            sizeList.setSelectedValue(sizeInt, true);
        }
        sizeList.ensureIndexIsVisible(sizeList.getSelectedIndex());

        cbBold.setSelected(font.isBold());
        cbItalic.setSelected(font.isItalic());

        txtSample.setFont(font);
    }

    private Font getCurrentFont() {
        String fontFamily = (String) fontList.getSelectedValue();
        int fontSize = ((Integer) sizeList.getSelectedValue()).intValue();

        int fontType = Font.PLAIN;

        if (cbBold.isSelected()) {
            fontType += Font.BOLD;
        }
        if (cbItalic.isSelected()) {
            fontType += Font.ITALIC;
        }

        return new Font(fontFamily, fontType, fontSize);
    }

    protected void updateFromFigure() {
        DrawingView view = getEditor().view();
        Font font = null;
        FigureEnumeration k = view.selectionElements();
        while ((k.hasMoreElements()) && (font == null)) {
            Figure f = k.nextFigure();
            Object fName = f.getAttribute("FontName");
            Object fStyle = f.getAttribute("FontStyle");
            Object fSize = f.getAttribute("FontSize");

            if ((fName != null) && (fStyle != null) && (fSize != null)) {
                font = new Font((String) fName, ((Integer) fStyle).intValue(),
                                ((Integer) fSize).intValue());
            }
        }

        setFont(font);
    }

    protected void apply() {
        String fontFamily = (String) fontList.getSelectedValue();
        Integer fontSize = (Integer) sizeList.getSelectedValue();

        int fontType = Font.PLAIN;
        if (cbBold.isSelected()) {
            fontType += Font.BOLD;
        }
        if (cbItalic.isSelected()) {
            fontType += Font.ITALIC;
        }

        // set the new font attributes of the selected figures
        // TODO: Do the attribute changes in a new ChangeAttributeCommand,
        //       which changes the three font attributes in one step, so
        //       the undo function works better.
        new ChangeAttributeCommand("", "FontName", fontFamily).execute();
        new ChangeAttributeCommand("", "FontSize", fontSize).execute();
        new ChangeAttributeCommand("", "FontStyle", new Integer(fontType))
            .execute();
    }

    /**
     * Maintains a sorted list of font sizes to be used as data model
     * for the JList within the choose font dialog.  Starting with a
     * base list of sizes, more sizes can be added when needed.
     *
     * Created: 25 Feb 2009
     * @author Michael Duvigneau
     **/
    private static class SizeListModel implements ListModel {
        private final List<Integer> sizeList;
        private final List<ListDataListener> listeners;

        /**
         * Creates a new font size list data model filled with the
         * given array of default font sizes.
         * @param sizes  an array of default font size values.
         *               May not be null.
         **/
        public SizeListModel(final int[] sizes) {
            sizeList = new ArrayList<Integer>(sizes.length * 2);
            for (int size : sizes) {
                sizeList.add(Integer.valueOf(size));
            }
            Collections.sort(sizeList);
            listeners = new ArrayList<ListDataListener>();
        }

        public Object getElementAt(final int index) {
            return sizeList.get(index);
        }

        public int getSize() {
            return sizeList.size();
        }

        public void addListDataListener(final ListDataListener listener) {
            listeners.add(listener);
        }

        public void removeListDataListener(final ListDataListener listener) {
            listeners.remove(listener);
        }

        /**
         * Inserts the given font size into the list, unless it is already
         * included.  The list remains sorted.
         *
         * @param size  the font size to include in the list.  May not be null.
         **/
        public void addSize(final Integer size) {
            assert (size != null);
            assert (sizeList != null);
            final int index = Collections.binarySearch(sizeList, size);
            if (index >= 0) {
                // Do nothing, the entry already exists.
            } else {
                final int indexToInsert = -index - 1;
                sizeList.add(indexToInsert, size);
                final ListDataEvent event = new ListDataEvent(this,
                                                              ListDataEvent.INTERVAL_ADDED,
                                                              indexToInsert,
                                                              indexToInsert);
                for (ListDataListener listener : listeners) {
                    listener.intervalAdded(event);
                }
            }
            assert (sizeList.contains(size));
        }
    }
}