/**
 *
 */
package de.renew.lola;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.gui.CPNDrawing;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import java.awt.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * This is the main class for analyzing nets with LoLA. So far it offers the
 * capabilities to check if places are bounded and if transitions are dead.
 *
 * @author hewelt, wagner
 *
 */
public class LolaAnalyzer {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LolaAnalyzer.class);

    /**
     * Points to the location, where the lola binaries reside
     */
    private String lolaPath;
    private File tmpDir;
    private LolaFileCreator creator = new LolaFileCreator();

    public LolaAnalyzer() {
        lolaPath = LolaPlugin.getLolaPath();
    }

    public LolaAnalyzer(String lolaPath) {
        tmpDir = LolaHelper.findTmpDir();
        this.lolaPath = lolaPath;
    }

    /**
     * Check for a given TransitionFigure and a given net (in lola format) if the
     * Transition is dead.
     *
     * @param tfig
     *            - a TransitionFigure
     * @param netFile
     *            - the Lola net file
     * @return
     */
    public LolaResult checkTransition(TransitionFigure tfig, File netFile) {
        String transitionName = creator.name(tfig);
        return checkTransition(transitionName, netFile);
    }

    /**
     * Checks whether the transition named 'transitionName' located in the
     * lola net file 'netFile' is dead.
     *
     * @param transitionName
     * @param netFile
     * @return a LolaResult
     */
    public LolaResult checkTransition(String transitionName, File netFile) {
        String task = LolaHelper.transitionKey + " " + transitionName;
        String baseName = new File(tmpDir, "tmp").getAbsolutePath();
        File taskFile = creator.writeTaskFile(baseName, task,
                                              LolaHelper.transitionFileName);
        String cmd = LolaHelper.transitionCommand;
        if (logger.isDebugEnabled()) {
            logger.debug("[LolaAnalyzer] Checking Transition named "
                         + transitionName);
        }
        LolaResult lolaOutput = callLola(taskFile, netFile, cmd);
        return lolaOutput;
    }

    /**
     * Checks whether the given TransitionFigure is live in the given drawing.
     * TODO: remove drawing as a parameter, analyzer should work only on net files.
     *
     * @param transitionName
     * @param netFile
     * @param drawing
     * @return
     */
    public LolaResult checkTransitionLiveness(TransitionFigure tfig,
                                              File netFile, Drawing drawing) {
        LolaResult lolaOutput = null;
        String transitionName = creator.name(tfig);
        LolaResult deadResult = checkTransition(transitionName, netFile);
        if (deadResult.getExitValue() == 0) { // live
            StringBuffer statePredicate = creator.constructStatePredicate(tfig,
                                                                          drawing);
            lolaOutput = checkStatePredicateLiveness(statePredicate.toString(),
                                                     netFile);
        } else {
            lolaOutput = deadResult;
        }
        return lolaOutput;
    }

    /**
     * Check for a given PlaceFigure and a given net (in lola format) if the
     * Place is bounded.
     *
     * @param pfig
     *            - a PlaceFigure
     * @param netFile
     *            - the Lola net file
     * @return a LolaResult
     */
    public LolaResult checkPlace(PlaceFigure pfig, File netFile) {
        String placeName = creator.name(pfig);
        return checkPlace(placeName, netFile);
    }

    /**
     * Checks if the place named placeName is bounded in the net netFile
     * (which is in lola net file format).
     * @param placeName
     * @param netFile
     * @return a LolaResult
     */
    public LolaResult checkPlace(String placeName, File netFile) {
        String task = LolaHelper.placeKey + " " + placeName;
        String baseName = new File(tmpDir, "tmp").getAbsolutePath();
        File taskFile = creator.writeTaskFile(baseName, task,
                                              LolaHelper.placeFileName);
        String cmd = LolaHelper.placeCommand;
        if (logger.isDebugEnabled()) {
            logger.debug("[LolaAnalyzer] Checking Place named " + placeName);
        }
        LolaResult lolaOutput = callLola(taskFile, netFile, cmd);
        return lolaOutput;
    }

    /**
     * Changes the fillColor of a Figure to a certain Color.
     *
     * @param fig
     *            - The Figure to be colored
     * @param color
     *            - The color to be used
     */
    public void colorFigure(Figure fig, Color color) {
        fig.setAttribute("FillColor", color);
        fig.changed();
        DrawPlugin.getGui().view().checkDamage();
    }

    /**
     * Calls the lola binaries for a given netFile, taskFile and lolaCommand.
     * Returns the output of the call (maybe an error) capsulated in a
     * LolaResult object
     *
     * @param netFile
     *            - The net in lola file format
     * @param taskFile
     *            - The file which contains the task, can be null
     * @param lolaCommand
     *            - The appropriate command for the given task
     *
     * @return a @see LolaResult object
     */
    private LolaResult callLola(File taskFile, File netFile, String lolaCommand) {
        return callLola(taskFile, netFile, lolaCommand, new String[] {  });
    }

    private LolaResult callLola(File taskFile, File netFile,
                                String lolaCommand, String[] parameter) {
        Runtime myrun = Runtime.getRuntime();
        /*
         * construct lola call
         */
        File lolaBin = new File(lolaPath, lolaCommand);
        if (logger.isDebugEnabled()) {
            logger.debug("LolaAnalyzer.callLola: lolaBin, netFile, taskFile, parameter are"
                         + lolaBin + " " + netFile + " " + taskFile + " "
                         + parameter);
        }

        ArrayList<String> argList = new ArrayList<String>();
        argList.add(lolaBin.toString());
        argList.add(netFile.toString());
        if (taskFile != null) {
            argList.add("-a" + taskFile.toString());
        }
        for (String para : parameter) {
            argList.add(para.toString());
        }
        String[] args = new String[argList.size()];
        args = argList.toArray(args);

        if (logger.isInfoEnabled()) {
            String cmd = "";
            for (String s : args) {
                cmd += s + " ";
            }
            logger.info("[Lola] Executing " + cmd);
        }

        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> output = new ArrayList<String>();
        int lolaRetVal = 6;
        try {
            Process lolproc = myrun.exec(args);
            BufferedReader input = new BufferedReader(new InputStreamReader(lolproc
                                                                            .getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(lolproc
                                                                            .getErrorStream()));
            String line = "";
            while ((line = error.readLine()) != null) {
                output.add(line);
                if (logger.isDebugEnabled()) {
                    logger.debug("[callLola OUTPUT]" + line);
                }
            }
            while ((line = input.readLine()) != null) {
                result.add(line);
                if (logger.isDebugEnabled()) {
                    logger.debug("[callLola RESULT]" + line);
                }
            }
            lolaRetVal = lolproc.waitFor();
            if (logger.isDebugEnabled()) {
                logger.debug("[Lola] Call returned exit value " + lolaRetVal);
            }
            error.close();
            input.close();
        } catch (IOException e) {
            logger.error("[Lola] Execution failed");
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("[Lola] Execution interrupted");
            e.printStackTrace();
        }
        LolaResult lolaResult = (result.isEmpty())
                                ? new LolaResult(lolaRetVal, output)
                                : new LolaResult(lolaRetVal, output, result);
        return lolaResult;
    }

    /**
     * Check a property of the whole net (e.g. boundedness, reversibility,
     * existence of home state, deadlock freedom). To check Liveness and
     * Quasi-Liveness is harder and involves calling checkStatePredicate.
     *
     * @param cmd
     *            - the name of the lola binary to call
     * @param netFile
     *            - a lola net file
     * @return
     */
    public LolaResult checkNet(String cmd, File netFile) {
        LolaResult result = callLola(null, netFile, cmd);

        // result = parseLolaOutput(output, cmd);
        return result;
    }

    /**
     * Checks if a given state predicate (e.g. p1 >= 2 AND (p5 < 5 AND p3 = 2))
     * is live i.e. if from any reachable marking a marking can be reached,
     * which fulfills the given state predicate.
     *
     * @param statePredicate
     * @param netFile
     *            - a lola net file
     * @return true if state predicate is live
     */
    public LolaResult checkStatePredicateLiveness(String statePredicate,
                                                  File netFile) {
        String task = LolaHelper.formulaKey + " " + statePredicate;
        String baseName = new File(tmpDir, "tmp").getAbsolutePath();

        File tmpFile = creator.writeTaskFile(baseName, task,
                                             LolaHelper.livepropFileName);

        String tmpCmd = LolaHelper.livepropCommand;
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola Checklist] Checking liveness of state predicate: "
                         + statePredicate);
        }

        LolaResult lolaOutput = callLola(tmpFile, netFile, tmpCmd);
        return lolaOutput;
    }

    /**
     * Checks whether the given net is live, ie. if each of its
     * transitions is live.
     * @param drawing
     * @return
     */
    public LolaResult checkNetLiveness(CPNDrawing drawing) {
        File tmpLolaFile = LolaFileCreator.writeTemporaryLolaFile(drawing);
        LolaResult result = null;
        int checkLive = 0;
        FigureEnumeration figs = drawing.figures();
        while (figs.hasMoreElements() && checkLive == 0) {
            Figure fig = figs.nextElement();
            if (fig instanceof TransitionFigure) {
                TransitionFigure tfig = (TransitionFigure) fig;
                if (logger.isDebugEnabled()) {
                    logger.debug("---------------------------------------------"
                                 + "------------------------------------------------");
                    logger.debug("[LolaAnalyzer] Checking Transition " + tfig);
                }
                LolaResult tResult = checkTransitionLiveness(tfig, tmpLolaFile,
                                                             drawing);
                if (tResult.getExitValue() != 0) { // it is not-live
                    logger.info("[Lola Checklist] Found not-live transition. Net is not live. ");
                    checkLive = 1;
                }
            }
        }
        if (checkLive == 0) { // all tranistions live
            result = new LolaResult(0);
        } else {
            result = new LolaResult(1);
        }
        return result;
    }

    public Map<String, Integer> checkLivenesAndQuasiLiveness(Drawing drawing,
                                                             LolaFileCreator creator,
                                                             File tmpLolaFile) {
        logger.info("[Lola] Checking LIVENESS and QUASI-LIVENESS");
        FigureEnumeration figs = drawing.figures();
        int checkDead = 0;
        int checkLive = 0;
        Map<String, Integer> result = new HashMap<String, Integer>();
        while (figs.hasMoreElements() && checkDead == 0) {
            Figure fig = figs.nextElement();
            if (fig instanceof TransitionFigure) {
                TransitionFigure tfig = (TransitionFigure) fig;
                if (logger.isDebugEnabled()) {
                    logger.debug("[Lola Checklist] Checking deadness of Transition "
                                 + fig);
                }
                LolaResult deadResult = checkTransition(tfig, tmpLolaFile);
                checkDead = deadResult.getExitValue();
                if (checkDead != 0) { // it is dead
                    checkLive = checkDead; // also not live
                    logger.info("[Lola Checklist] Found dead transition. Net is neither quasi-live nor live. ");
                } else { // only check liveness of transition, if it is not dead
                         // construct the statepredicate for this transition
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Lola Checklist] creating state predicate");
                    }
                    StringBuffer statePredicate = creator
                                                      .constructStatePredicate(tfig,
                                                                               drawing);

                    if (logger.isDebugEnabled()) {
                        logger.debug("[Lola Checklist] resulting statepredicate: "
                                     + statePredicate);
                    }
                    LolaResult liveResult = checkStatePredicateLiveness(statePredicate
                                                                        .toString(),
                                                                        tmpLolaFile);
                    // if one transition is not live the net is not
                    checkLive = Math.max(checkLive, liveResult.getExitValue());
                    if (logger.isDebugEnabled()) {
                        logger.debug("[LolaAnalyzer]: is transition live (0 = YES)?: "
                                     + liveResult.getExitValue());
                        logger.debug("[LolaAnalyzer]: exit value checklive: "
                                     + checkLive);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("[Lola Checklist] Checking "
                                 + creator.name(tfig) + " finished. It is "
                                 + ((checkDead == 0)
                                    ? (checkLive == 0) ? "live." : "quasi-live"
                                    : " not live."));
                }
            }
        }
        result.put("checkDead", checkDead);
        result.put("checkLive", checkLive);
        return result;
    }

    /**
     * Checks if a given state predicate (e.g. p1 >= 2 AND (p5 < 5 AND p3 = 2))
     * is reachable i.e. if there is a marking reachable from the initial marking
     * where the given predicate is satisfied.
     *
     * @param statePredicate
     * @param netFile
     *            - a lola net file
     * @return LolaResult with the result
     */
    public LolaResult checkStatePredicateReachability(String statePredicate,
                                                      File netFile) {
        String task = LolaHelper.formulaKey + " " + statePredicate;
        String baseName = new File(tmpDir, "tmp").getAbsolutePath();

        File tmpFile = creator.writeTaskFile(baseName, task,
                                             LolaHelper.stateFormulaFileName);

        String tmpCmd = LolaHelper.statePredicateCommand;
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola Checklist] Checking liveness of state predicate: "
                         + statePredicate);
        }

        LolaResult lolaOutput = callLola(tmpFile, netFile, tmpCmd);

        return lolaOutput;
    }

    /**
     * Checks if a given marking is reachable from the provided netFile
     *
     * @param marking
     *            The marking task to be checked
     * @param netFile
     *            The netFile to be checked
     * @return the LolaResult of the check
     */
    public LolaResult checkMarking(StringBuffer marking, File netFile) {
        String task = marking.toString();
        String baseName = new File(tmpDir, "tmp").getAbsolutePath();

        File tmpFile = creator.writeTaskFile(baseName, task,
                                             LolaHelper.markingFileName);

        String tmpCmd = LolaHelper.markingCommand;
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola] Checking reachability of marking: " + task);
        }

        LolaResult lolaOutput = callLola(tmpFile, netFile, tmpCmd);

        return lolaOutput;
    }

    /**
     * Call lola with given command on given lola net file.
     * Use given parameter and return result.
     * Internally uses method @see callLola()
     *
     * @param lolaCommand
     * @param tmpLolaFile
     * @param parameter
     * @return
     */
    public LolaResult checkNet(String lolaCommand, File tmpLolaFile,
                               String[] parameter) {
        return callLola(null, tmpLolaFile, lolaCommand, parameter);
    }

    /**
     * Check a verification request from given task file by invocing the
     * given lolaCommand on the given lola net file.
     * @param taskFile
     * @param lolaFile
     * @param lolaCommand
     * @return The LolaResult
     */
    public LolaResult checkTask(File taskFile, File lolaFile, String lolaCommand) {
        if (lolaCommand.equals(LolaHelper.CTLCommand)) {
            return callLola(taskFile, lolaFile, lolaCommand,
                            new String[] { "-P" });
        }
        return callLola(taskFile, lolaFile, lolaCommand);
    }
}