package de.renew.refactoring.renamevariable;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.DeclarationFigure;

import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.parse.VariableParser;
import de.renew.refactoring.search.DrawingSearcher;
import de.renew.refactoring.search.range.SingleDrawingSearchRange;

import java.util.ArrayList;
import java.util.List;


/**
 * Finds references to a variable.
 *
 * @author 2mfriedr
 */
class RenameVariableReferenceFinder extends DrawingSearcher<VariableNameMatch> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameVariableReferenceFinder.class);
    private final VariableParser _parser;
    private final String _name;

    /**
     * Constructs a new RenameVariableReferenceFinder object.
     *
     * @param parser the parser
     * @param drawing the drawing
     * @param name the variable name
     */
    RenameVariableReferenceFinder(final VariableParser parser,
                                  final Drawing drawing, final String name) {
        super(new SingleDrawingSearchRange(drawing));
        _parser = parser;
        _name = name;
    }

    @Override
    protected List<VariableNameMatch> searchDrawing(Drawing drawing) {
        List<VariableNameMatch> references = new ArrayList<VariableNameMatch>();
        FigureEnumeration figures = drawing.figures();
        while (figures.hasMoreElements()) {
            Figure figure = figures.nextElement();

            if (figure instanceof CPNTextFigure) {
                CPNTextFigure textFigure = (CPNTextFigure) figure;
                String text = textFigure.getText();
                List<StringMatch> allVariables = (figure instanceof DeclarationFigure)
                                                 ? _parser
                                                     .findVariablesInDeclarationNode()
                                                 : _parser.findVariables(text);

                for (StringMatch stringMatch : allVariables) {
                    if (stringMatch.match().equals(_name)) {
                        logger.debug("found reference: " + stringMatch);
                        references.add(new VariableNameMatch(drawing,
                                                             textFigure,
                                                             stringMatch));
                    }
                }
            }
        }
        return references;

    }
}