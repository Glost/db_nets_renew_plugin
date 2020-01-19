/*
 * @(#CPNTextTool.java 1.0
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.ConnectedTextTool;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.standard.FigureException;
import CH.ifa.draw.standard.TextHolder;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.SyntaxException;


public class CPNTextTool extends ConnectedTextTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CPNTextTool.class);
    private CPNTextFigure fTextFigure = null;
    private CPNDrawing fDrawing = null;

    public CPNTextTool(DrawingEditor editor, CPNTextFigure prototype) {
        super(editor, prototype);
    }

    public CPNTextTool(DrawingEditor editor, CPNTextFigure prototype,
                       boolean mustConnect) {
        super(editor, prototype, mustConnect);
    }

    public void beginEdit(TextHolder figure) {
        super.beginEdit(figure);
        if (figure instanceof CPNTextFigure
                    && fEditor.drawing() instanceof CPNDrawing) {
            fTextFigure = (CPNTextFigure) figure;
            fDrawing = (CPNDrawing) fEditor.drawing();
        } else {
            fTextFigure = null;
        }
    }

    public void endEdit() {
        super.endEdit();
        if (fTextFigure != null) {
            CPNTextFigure textFigure = fTextFigure;
            fTextFigure = null;
            int type = textFigure.getType();
            boolean special = type == CPNTextFigure.AUX;
            if (type == CPNTextFigure.INSCRIPTION || special) {
                String newText = textFigure.getText();
                if (newText.trim().length() > 0) {
                    //logger.debug("New inscription: "+newText);
                    try {
                        CPNSimulation sim = ModeReplacement.getInstance()
                                                           .getSimulation();
                        sim.buildAllShadows();
                        ShadowNetSystem netSystem = sim.getNetSystem();

                        ShadowHolder parent = (ShadowHolder) textFigure.parent();
                        ShadowNet shadowNet = fDrawing.buildShadow(netSystem);


                        if (textFigure instanceof SemanticUpdateFigure) {
                            ((SemanticUpdateFigure) textFigure).semanticUpdate(shadowNet);
                        }
                        if (shadowNet != null) {
                            if (textFigure instanceof DeclarationFigure) {
                                fEditor.showStatus("Checking Declaration Node...");
                                shadowNet.checkDeclarationNode(newText, special);
                            } else if (parent != null && shadowNet != null) { //NOTICEredundant
                                if (parent instanceof TransitionFigure) {
                                    fEditor.showStatus("Checking Transition Inscription...");
                                    shadowNet.checkTransitionInscription(newText,
                                                                         special);
                                } else if (parent instanceof PlaceFigure) {
                                    fEditor.showStatus("Checking Place Inscription...");
                                    shadowNet.checkPlaceInscription(newText,
                                                                    special);
                                } else if (parent instanceof ArcConnection) {
                                    fEditor.showStatus("Checking Arc Inscription...");
                                    shadowNet.checkArcInscription(newText,
                                                                  special);
                                }
                            }
                        }
                        GuiPlugin.getCurrent().closeSyntaxErrorFrame();
                    } catch (SyntaxException e) {
                        fEditor.showStatus("A syntax error occured.");
                        FigureException fe = FigureExceptionFactory
                                                 .createFigureException(e);

                        // Test if the syntax error was in the edited text:
                        if (fe.errorDrawing == null) {
                            fe = FigureExceptionFactory.createFigureException(e,
                                                                              fDrawing,
                                                                              textFigure);
                        }
                        logger.debug(e.getMessage(), e);
                        GuiPlugin.getCurrent().processSyntaxException(fe, false);

                    }
                }
            }
        }
    }
}