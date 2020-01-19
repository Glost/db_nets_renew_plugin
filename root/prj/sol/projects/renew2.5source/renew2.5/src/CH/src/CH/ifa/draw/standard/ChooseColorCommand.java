package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JColorChooser;


/**
 * Command to change the color of a figure.
 * <p>
 * The command's effects are undoable step by step.
 * Each use of the apply button can be undone separately.
 * So this command doesn't need to inherit UndoableCommand.
 * </p>
 * @author Sven Offermann
 */
public class ChooseColorCommand extends ChooseAttributeCommand {
    private JColorChooser chooser;

    /**
     * Constructs a dialog with a JColorChooser to
     * change the color of figures, fonts etc.
     *
     * @param displayName the dialog name
     * @param name the command name
     * @param attributeName the name of the attribute to be changed
     * @param type the attribute type (class)
     */
    public ChooseColorCommand(String displayName, String name,
                              String attributeName, Class<?> type) {
        super(displayName, name, attributeName, type);
    }

    protected void specializeDialog() {
        chooser = new JColorChooser();
        dialog.getContentPane().add(chooser, BorderLayout.CENTER);
        //NOTICEredundand
        dialog.addKeyListener(new KeyAdapter() {
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

        if (value != null) {
            if (value instanceof Color) {
                chooser.setColor((Color) value);
            }
        }
    }

    protected void apply() {
        Object value = null;
        value = chooser.getColor();
        new ChangeAttributeCommand("", fAttribute, value).execute();
    }
}