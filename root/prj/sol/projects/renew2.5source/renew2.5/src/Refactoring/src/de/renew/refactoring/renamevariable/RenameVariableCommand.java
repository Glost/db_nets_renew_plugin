package de.renew.refactoring.renamevariable;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

import de.renew.formalism.FormalismPlugin;

import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNDrawingView;

import de.renew.refactoring.inline.SingleInlineUndoableCommand;
import de.renew.refactoring.parse.DeclarationFinder;
import de.renew.refactoring.parse.JNPVariableParser;

import java.util.List;


/**
 * Command for the refactoring Rename Variable.
 *
 * @author 2mfriedr
 */
public class RenameVariableCommand extends SingleInlineUndoableCommand {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameVariableCommand.class);
    private static final String COMMAND_NAME = "Rename variable...";

    public RenameVariableCommand() {
        super(COMMAND_NAME);
    }

    @Override
    protected boolean executeUndoable() {
        Drawing drawing = getEditor().drawing();
        String declaration = new DeclarationFinder(drawing).declarationText();
        CPNDrawingView drawingView = (CPNDrawingView) getEditor().view();
        try {
            RenameVariableInlineController controller = new RenameVariableInlineController(new JNPVariableParser(declaration),
                                                                                           drawingView);
            setInline(controller);
        } catch (NoVariableSelectedException e) {
            getEditor()
                .showStatus("There is a syntax error or no variable is selected.");
            return false;
        }

        return true;
    }

    @Override
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }

        Drawing drawing = getEditor().drawing();
        if (!(drawing instanceof CPNDrawing)) {
            return false;
        }

        boolean isJavaFormalism = FormalismPlugin.getCurrent().getCompiler()
                                                 .equals(FormalismPlugin.JAVA_COMPILER);
        if (!isJavaFormalism) {
            return false;
        }

        List<Figure> selection = getEditor().view().selection();

        // The refactoring is marked as executable if
        // a) a single text figure is selected
        RenameVariableSelectionChecker selChecker = new RenameVariableSelectionChecker(selection,
                                                                                       null);
        if (selChecker.isTextFigureSelected()) {
            return true;
        }

        DeclarationFinder declFinder = new DeclarationFinder(drawing);

        // or b) the drawing has a declaration figure
        return declFinder.hasDeclarationFigure();
    }
}