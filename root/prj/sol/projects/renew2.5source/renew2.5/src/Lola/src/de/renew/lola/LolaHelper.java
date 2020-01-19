package de.renew.lola;

import de.renew.plugin.PluginProperties;

import java.io.File;

import java.util.Arrays;
import java.util.HashMap;


/**
 * Defines constants used in the context of lola.
 * @author hewelt
 *
 */
public class LolaHelper {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LolaHelper.class);
    /*
     * Keyword Strings
     */
    public static final String markingKey = "ANALYSE MARKING";
    public static final String placeKey = "ANALYSE PLACE";
    public static final String transitionKey = "ANALYSE TRANSITION";
    public static final String stateFormulaKey = "STATE PREDICATE FORMULA";
    public static final String CTLFormulaKey = "CTL FORMULA";
    public static final String formulaKey = "FORMULA";
    public static final String assertKey = "ASSERT";

    /*
     * Following strings are added to the name of a task file
     * to indicate the task type.
     */
    public static final String markingFileName = "Marking";
    public static final String CTLFormulaFileName = "CTL";
    public static final String stateFormulaFileName = "StatePredicate";
    public static final String transitionFileName = "Transition";
    public static final String placeFileName = "Place";
    public static final String livepropFileName = "Liveprop";

    /*
     * name of lola binaries ("commands") for the specific tasks.
     */
    public static final String dotCommand = "graph2dot";
    public static final String graphCommand = "lola -M";
    public static final String markingCommand = "lola-reach-mark";
    public static final String placeCommand = "lola-bounded-place";
    public static final String transitionCommand = "lola-dead-transition";
    public static final String CTLCommand = "lola-model-checking";
    public static final String statePredicateCommand = "lola-state-predicate";
    public static final String livepropCommand = "lola-liveprop";
    public static final String nethomeStateCommand = "lola-home-state";
    public static final String netReversibleCommand = "lola-reversible";
    public static final String netDeadlockCommand = "lola-deadlock";
    public static final String netBoundedCommand = "lola-bounded-net";

    /* Each verification task specification gets written to file whose name
     * consists of the netname, a portion defined in the taskFilenameMap, a
     * number and the * .task extenstion.
     * For example: lolatestStatePredicate1.task
     */
    public static HashMap<String, String> taskFilenameMap = new HashMap<String, String>();

    static {
        taskFilenameMap.put(markingKey, markingFileName);
        taskFilenameMap.put(placeKey, placeFileName);
        taskFilenameMap.put(transitionKey, transitionFileName);
        taskFilenameMap.put(stateFormulaKey, stateFormulaFileName);
        taskFilenameMap.put(CTLFormulaKey, CTLFormulaFileName);
    }

    /*
     * Depending on the type of a verification task, which is represented
     * in the Filename of the task file, a different command is issued.
     */
    public static HashMap<String, String> taskCommandMap = new HashMap<String, String>();

    static {
        taskCommandMap.put(markingFileName, markingCommand);
        taskCommandMap.put(placeFileName, placeCommand);
        taskCommandMap.put(transitionFileName, transitionCommand);
        taskCommandMap.put(CTLFormulaFileName, CTLCommand);
        taskCommandMap.put(stateFormulaFileName, statePredicateCommand);
        taskCommandMap.put(livepropFileName, livepropCommand);
    }


    /**
     * Determine the temporary directory to use.
         * @return dir
         *
         */
    public static File findTmpDir() {
        String tmp = PluginProperties.getUserProperties()
                                     .getProperty("de.renew.lola.tmpdir");
        File dir;
        if (tmp != null) {
            dir = new File(tmp);
        } else {
            tmp = System.getProperty("java.io.tmpdir");
            if (tmp != null) {
                dir = new File(tmp);
            } else {
                logger.error("System property java.io.tmpdir not found, please provide de.renew.lola.tmpdir in your user properties");
                return null;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola] tmpDir successfully set");
        }
        return dir;
    }

    /**
     * Checks if tempTxt contains one of the keywords for CTL Formulas.
     * @param tempTxt
     * @return true, if CTL Formula, false otherwise
     */
    protected static Boolean ctlp(String tempTxt) {
        java.util.List<String> forbidden = Arrays.asList("always", "until",
                                                         "eventually",
                                                         "nextstep", "expath",
                                                         "allpath");
        for (String key : forbidden) {
            if (tempTxt.toLowerCase().contains(key)) {
                return true;
            }
        }
        return false;
    }
}