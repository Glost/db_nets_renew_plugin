package de.renew.lola;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.CPNDrawing;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import java.awt.Color;

import java.io.File;


/**
 * I am a Lola verification request aka. lola task.
 * If I'm given my text, I can determine my type, write myself to a file
 * and callLola to check myself. Afterwards I can color the figure, that
 * defined me in nice colors.
 *
 * @author hewelt
 *
 */
public class LolaTask {

    /**
     * I can produce console output via this logger
     */
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LolaTask.class);

    /**
     * The raw text, needs to be parsed (I do this during instantiation)
     */
    private String _text;

    /**
     * My type (e.g. marking, place, transition, ctl, statepredicate).
     * TODO indicate this through subtypes (e.g. LolaMarkingTask)
     */
    private String _type;

    /**
     * The TextFigure that defines/represents me in the drawing.
     */
    private final TextFigure _figure;

    /**
     * The net drawing in which I reside.
     */
    private final CPNDrawing _drawing;

    /**
     * What lola says about me, is put into this variable.
     */
    private LolaResult _result;

    /**
     * The file in which I'm stored
     */
    private File _file;

    /**
     * Am I a valid task? (Not checking syntax)
     */
    private boolean _valid;

    /**
     * I need to know my figure and the drawing in which it is located.
     * So I can callLola.
     * @param fig
     * @param drawing
     */
    public LolaTask(TextFigure fig, CPNDrawing drawing) {
        _figure = fig;
        _drawing = drawing;
        _text = fig.getText();
        _type = parseText(_text);
        _valid = (_type.equals("")) ? false : true;
    }

    /**
     * Determine which type of task I am, given a text
     *
     * @param text
     * @return The type of this task or "" if none determined
     */
    private String parseText(String text) {
        logger.info("[Lola Task] Parsing verification task " + this.toString());
        // First check if I am an "assertion". This can change my text
        String result = "";
        if (_figure.parent() != null && text.startsWith(LolaHelper.assertKey)) {
            LolaFileCreator creator = new LolaFileCreator();
            Figure parent = ((TextFigure) _figure).parent();
            String parentName = creator.name((FigureWithID) parent);
            if (parent instanceof PlaceFigure) {
                _text = LolaHelper.placeKey + " " + parentName;
                result = LolaHelper.placeFileName;
                if (logger.isDebugEnabled()) {
                    logger.debug("[Lola] Found assert task.");
                }
            } else if (parent instanceof TransitionFigure) {
                _text = LolaHelper.transitionKey + " " + parentName;
                result = LolaHelper.transitionFileName;
                if (logger.isDebugEnabled()) {
                    logger.debug("[Lola] Found assert task.");
                }
            } else {
                _type = ""; // so we won't call lola
                logger.error("[Lola] Assert statements need to be attached to places or transitions.");
            }
        } else if (_figure.parent() == null) {
            if (text.startsWith(LolaHelper.markingKey)) {
                result = LolaHelper.markingFileName;
            } else if (text.startsWith(LolaHelper.transitionKey)) {
                result = LolaHelper.transitionFileName;
            } else if (text.startsWith(LolaHelper.placeKey)) {
                result = LolaHelper.placeFileName;
            } else if (text.startsWith(LolaHelper.formulaKey)) {
                if (LolaHelper.ctlp(text)) {
                    result = LolaHelper.CTLFormulaFileName;
                } else {
                    result = LolaHelper.stateFormulaFileName;
                }
            } else {
                logger.error("[Lola] Didn't recognize the type of the task.");
            }
        }
        return result;
    }

    /**
     * Calls Lola, registers the result and returns it to my caller.
     * I instantiate a LolaAnalyzer to check myself, but I can't now
     * the lib folder (where the lola binaries are located). Fortunatly
     * LolaAnalyzer offers a no argument constructor which asks the
     * LolaPlugin for the lib folder.
     * <b>But this can only work, when a LolaPlugin is running, hence it
     * doesn't work from JUnit.</b>
     * @return a LolaResult
     */
    public LolaResult check() {
        if (_type.equals("")) {
            logger.error("[Lola] Not a valid task type, can't check.");
            return null;
        }
        LolaResult result = null;
        LolaAnalyzer analyzer = new LolaAnalyzer();
        File lolaTmpFile = LolaFileCreator.writeTemporaryLolaFile(_drawing);
        if (_file == null) { // need to write myself into file first
            logger.info("[Lola Task] Writing" + this.toString() + " into file.");
            _file = writeToFile();
        }
        if (_file != null && _file.exists()) {
            String lolaCommand = LolaHelper.taskCommandMap.get(_type);
            _result = analyzer.checkTask(_file, lolaTmpFile, lolaCommand);
            result = _result;
        } else {
            logger.error("[Lola Task] No taskfile to check.");
        }
        return result;
    }

    /**
     * Write this task into a file, so that it could be verified by lola.
     *
     * @return the File into which task was written
     */
    public File writeToFile() {
        File taskFile = null;
        LolaFileCreator creator = new LolaFileCreator();
        // TODO what happens if file writing errors occur in call?
        taskFile = creator.writeTaskFile(_drawing.getName(), _text, _type);

        if (taskFile.exists()) {
            _file = taskFile;
        }
        return taskFile;
    }

    /**
     * Changes the frame color of the figure this task belongs to
     * according to the LolaResult.
     */
    public void colorFigure() {
        if (_result != null) {
            if (_result.getExitValue() == 3) {
                _figure.setAttribute("FrameColor", Color.BLACK);
                logger.error("[Lola] Syntax Error, correct verification request.");
            } else if (_result.getExitValue() == 5) {
                _figure.setAttribute("FrameColor", Color.GRAY);
                logger.error("[Lola] Verification couldn't be verified. Maximal number of stated reached.");
            } else if (_result.getExitValue() == 0) {
                _figure.setAttribute("FrameColor",
                                     _type.equals(LolaHelper.placeFileName)
                                     ? Color.RED : Color.GREEN);
            } else if (_result.getExitValue() == 1) {
                _figure.setAttribute("FrameColor",
                                     _type.equals(LolaHelper.placeFileName)
                                     ? Color.GREEN : Color.RED);
            }
            _figure.changed();
            DrawPlugin.getGui().view().checkDamage();
        } else {
            logger.error("[Lola Task] Task seems to be not yet checked.");
        }
    }

    /**
     * Accessor for the field _valid
     * @return
     */
    public boolean isValid() {
        return _valid;
    }

    @Override
    public String toString() {
        return _text;
    }

    /**
     * I can tell you my type. This is required to find the correct
     * lola command to check me manually.
     * @return my type, a String
     */
    public String getType() {
        return _type;
    }

    public TextFigure getFigure() {
        return _figure;
    }
}