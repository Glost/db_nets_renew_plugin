/*
 * @(#)ChangeAttributeCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;


/**
 * Command to change a named figure attribute.
 * <p>
 * The command's effects are undoable step by step.
 * Each use of the apply button can be undone separately.
 * So this command doesn't need to inherit UndoableCommand.
 * </p>
 */
public class QueryAttributeCommand extends ChooseAttributeCommand {
    JTextField input;
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(QueryAttributeCommand.class);

    /**
     * Constructs a query attribute command.
     * @param displayName the dialog name
     * @param name the command name
     * @param attributeName the name of the attribute to be changed
     * @param type the attribute type (class)
     */
    public QueryAttributeCommand(String displayName, String name,
                                 String attributeName, Class<?> type) {
        super(displayName, name, attributeName, type);

    }

    protected void specializeDialog() {
        input = new JTextField();
        dialog.getContentPane().add(input, BorderLayout.CENTER);
        //NOTICEredundand
        input.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        apply();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        closeDialog(dialog);
                    }
                }
            });
        dialog.pack();
    }

    protected void updateFromFigure() {
        DrawingView view = getEditor().view();
        Object value = null;
        FigureEnumeration k = view.selectionElements();
        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();
            Object val = f.getAttribute(fAttribute);
            if (val != null) {
                if (value != null && !value.equals(val)) {
                    // different values, use previous value
                    value = null;
                    break;
                }
                value = val;
            }
        }
        String valuestr = "";
        if (value != null) {
            valuestr = value.toString();
        }
        input.setText(valuestr);
        resetFocus();
    }

    /**
     * After call of this method the JTextField input has the focus
     * and its text is selected.
     */
    private void resetFocus() {
        input.requestFocus();
        input.setSelectionStart(0);
        input.setSelectionEnd(input.getText().length());
    }

    protected void apply() {
        String valuestr = input.getText();
        Object value = null;
        try {
            if (type == Integer.class) {
                value = Integer.valueOf(valuestr);
            } else if (type == String.class) {
                if (fAttribute.equals("LineStyle")
                            && (valuestr.startsWith("0")
                                       || !(valuestr.matches("(\\d+( \\d+)*)?")))) {
                    logger.error("In the '" + displayName
                                 + "' dialog only numbers separated by a dash or a gap are allowed. "
                                 + "String should not start with 0 (zero).");
                } else {
                    value = valuestr;
                }
            }
        } catch (NumberFormatException ex) {
            logger.error(ex.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(QueryAttributeCommand.class.getSimpleName()
                             + ": \n" + ex);
            }
        }
        if (value != null) {
            new ChangeAttributeCommand("", fAttribute, value).execute();
        }
        resetFocus();
    }
}