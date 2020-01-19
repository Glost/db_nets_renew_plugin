package de.renew.refactoring.renamechannel;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.NullDrawingEditor;

import de.renew.formalism.FormalismPlugin;

import de.renew.refactoring.parse.JNPLinkParser;
import de.renew.refactoring.wizard.SingleWizardCommand;
import de.renew.refactoring.wizard.Wizard;
import de.renew.refactoring.wizard.WizardController;

import java.util.List;


/**
 * <p>Refactoring command that renames a channel across multiple drawings.</p>
 * <p>Limitations:</p>
 * <ul>
 * <li>no refactoring preconditions are checked</li>
 * <li>editor is not locked during refactoring</li>
 * </ul>
 *
 * <p>See {@code etc/rename-test.rnw} for an example.</p>
 *
 * @author 2mfriedr
 */
public class RenameChannelCommand extends SingleWizardCommand {
    // not UndoableCommand because it modifies multiple nets
    private static final String COMMAND_NAME = "Rename channel...";

    public RenameChannelCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public void execute() {
        Drawing drawing = getEditor().drawing();
        List<Figure> selection = getEditor().view().selection();

        try {
            WizardController controller = new RenameChannelWizardController(new JNPLinkParser(),
                                                                            drawing,
                                                                            selection);
            setWizard(new Wizard(controller));

        } catch (NoLinkSelectedException e) {
            getEditor()
                .showStatus("There is a syntax error or no uplink or downlink selected.");
        }
    }

    @Override
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }

        boolean isJavaFormalism = FormalismPlugin.getCurrent().getCompiler()
                                                 .equals(FormalismPlugin.JAVA_COMPILER);
        if (!isJavaFormalism) {
            return false;
        }


        // The refactoring is marked as executable if a single text figure or
        // transition figure is selected
        List<Figure> selection = getEditor().view().selection();
        Drawing drawing = getEditor().drawing();
        return new RenameChannelSelectedLinkFinder(null, drawing, selection)
                   .isTextFigureOrTransitionFigureSelected();
    }

    private static DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }
}