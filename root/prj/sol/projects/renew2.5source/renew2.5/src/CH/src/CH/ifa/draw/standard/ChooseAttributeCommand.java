/**
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.util.Command;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;


public abstract class ChooseAttributeCommand extends Command {
    //The subclasses of this abstract class could be further 
    //refactored.
    //At this time the subclass QueryAttributeCommand gets used for 
    //changing of the textsize and for changing of the linestyle. 
    //The difference is made by the parameters "type" and "attributeName"
    //The next step would be to split those to cases into multiple classes. 
    //"type" and "attributeName" should then become obsolete. 
    //QueryAttributeCommand should also be renamed. For example to 
    //"ChooseBasicAttributeCommand"
    //At last the redundancy that exists in the implementations of
    //updateFromFigure() should be moved into this abstract class. 
    static final String WINDOWS_CATEGORY_ATTRIBUTES = "Attributes";
    protected JFrame lastParent;
    protected JDialog dialog;
    protected String fAttribute;
    protected Class<?> type;
    protected String displayName;

    public ChooseAttributeCommand(String displayName, String name,
                                  String attributeName, Class<?> type) {
        super(name);
        this.fAttribute = attributeName;
        this.displayName = displayName;
        this.type = type;
    }

    /**
     * Creates the dialog.
     * It uses the existing one if possible.
     */
    protected void createDialog() {
        // We can reuse an existing dialog, but we must create a new dialog
        // when the gui frame has been closed and recreated inbetween.
        // This is possible because the menu command objects are kept across
        // gui instances by the MenuManager. Only a redesign of the
        // MenuManager would solve this. (See also bug #99 in bugzilla)
        JFrame parent = DrawPlugin.getGui().getFrame();
        if (dialog != null) {
            if (lastParent == parent) {
                // reuse the existing dialog
                return;
            }
            // or release the old one
            dialog.dispose();
        }
        dialog = new JDialog(parent, "Change " + displayName + ":");
        lastParent = parent;

        JButton apply = new JButton(" Apply ");
        apply.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    apply();
                }
            });

        JButton update = new JButton(" Update ");
        update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateFromFigure();
                }
            });

        JButton ok = new JButton(" OK ");
        ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    apply();
                    closeDialog(dialog);
                }
            });

        JButton cancel = new JButton(" Cancel ");
        cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    closeDialog(dialog);
                }
            });

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(apply);
        panel.add(update);
        panel.add(ok);
        panel.add(cancel);
        dialog.getContentPane().add(panel, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    closeDialog(dialog);
                }
            });

        specializeDialog();
    }

    /**
     * This method is part of the template method pattern.
     * It gets called by createDialog and adds more dialog elements that are
     * special for the implementing class.
     */
    abstract protected void specializeDialog();

    /**
     * Gets the configuration of the attributes of the current selected figure
     * and applies it to the configuration of this dialog.
     * If more then one figure is selected the attributes of one figure is
     * chosen non-deterministicly.
     */
    abstract protected void updateFromFigure();

    /**
     * Applies the current attribute configuration to the selected figures.
     */
    abstract protected void apply();

    /**
     * Sets the Dialog to visible after setting its values.
     */
    public void execute() {
        createDialog();

        updateFromFigure();

        dialog.setVisible(true);
        MenuManager.getInstance().getWindowsMenu()
                   .addDialog(WINDOWS_CATEGORY_ATTRIBUTES, dialog);
    }

    protected DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }

    /**
     * @return
     *   true, if one or more items is selected in the editor.
     */
    public boolean isExecutable() {
        if (getEditor() == NullDrawingEditor.INSTANCE) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }

    static protected void closeDialog(final JDialog dialog) {
        MenuManager.getInstance().getWindowsMenu().removeDialog(dialog);
        dialog.setVisible(false);
    }
}