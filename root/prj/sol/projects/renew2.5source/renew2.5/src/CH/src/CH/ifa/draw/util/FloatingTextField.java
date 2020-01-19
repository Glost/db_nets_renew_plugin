/*
 * @(#)FloatingTextField.java 5.1
 *
 */
package CH.ifa.draw.util;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * A text field overlay that is used to edit a TextFigure.
 * A FloatingTextField requires a two step initialization:
 * In a first step the overlay is created and in a
 * second step it can be positioned.
 *
 * @see CH.ifa.draw.figures.TextFigure
 */
public class FloatingTextField extends Object {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FloatingTextField.class);
    private Rectangle minimum;
    protected JScrollPane scrollPane;
    protected JTextArea fEditWidget;
    private Container fContainer;

    public FloatingTextField() {
        fEditWidget = new JTextArea(1, 10);
        // to change the view area when text changed:
        fEditWidget.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateOverlay();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateOverlay();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateOverlay();
                }
            });
        scrollPane = new JScrollPane(fEditWidget,
                                     JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        minimum = null;
    }

    /**
     * Updates the bounds of the JTextArea, when text has changed.
     */
    protected void updateOverlay() {
        // Executing this asynchronously on the AWT event dispatching thread
        // avoids a bug where the edit widget would return a too large
        // preferred size when inserting text with linebreaks because the text
        // was not internally layed out before calculating the size.
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Dimension d = fEditWidget.getPreferredSize();

                    //set minimum (+5 for scrollPane)
                    if (minimum != null) {
                        scrollPane.setBounds(minimum.x, minimum.y,
                                             (d.width > minimum.width ? d.width
                                                                      : minimum.width)
                                             + 5,
                                             (d.height > minimum.height
                                              ? d.height : minimum.height) + 5);
                    }
                }
            });
    }

    /**
     * Creates the overlay for the given Container using a
     * specific font.
     */
    public void createOverlay(Container container, Rectangle minimum, Font font) {
        this.minimum = minimum;
        fContainer = container;

        fContainer.add(scrollPane, 0);
        if (font != null) {
            fEditWidget.setFont(font);
        }
        scrollPane.setVisible(true);
        fEditWidget.selectAll();
        fEditWidget.requestFocus();
        updateOverlay();
    }

    /**
     * Removes the overlay.
     */
    public void endOverlay() {
        fContainer.requestFocus();
        if (fEditWidget == null) {
            return;
        }
        scrollPane.setVisible(false);
        fContainer.remove(scrollPane);
    }

    /**
     * Gets the text contents of the overlay.
     */
    public String getText() {
        return fEditWidget.getText();
    }

    /**
     * sets the text contents of the overlay.
     */
    public void setText(String text) {
        fEditWidget.setText(text);
    }

    public void setCaretPosition(int line, int column) {
        int pos = position(fEditWidget.getText(), line, column);
        fEditWidget.select(pos, pos); // clear selection
        fEditWidget.setCaretPosition(pos);
    }

    public void select(int startLine, int startColumn, int endLine,
                       int endColumn) {
        int startPos = position(fEditWidget.getText(), startLine, startColumn);
        int endPos = position(fEditWidget.getText(), endLine, endColumn);
        fEditWidget.select(startPos, endPos);
    }

    private static int position(String str, int line, int col) {
        int pos = 0;
        BufferedReader read = new BufferedReader(new StringReader(str));
        while (--line > 0) {
            try {
                String oneLine = read.readLine();
                pos += oneLine.length() + 1;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return pos + col - 1;
    }

    /** Allows to add a key listener to the <code>JTextArea</code> widget.
     *
     * @param keyListener - the key listener to be added to the widget.
     */
    public void addKeyListener(KeyListener keyListener) {
        fEditWidget.addKeyListener(keyListener);
    }
}