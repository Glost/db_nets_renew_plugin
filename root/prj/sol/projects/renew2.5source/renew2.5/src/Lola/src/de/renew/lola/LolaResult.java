/**
 *
 */
package de.renew.lola;

import java.util.ArrayList;


/**
 * @author hewelt
 *
 */
public class LolaResult {

    /**
     * The return value of the lola call
     */
    private final int _exitValue;

    /**
     * The output of the lola call
     */
    private final ArrayList<String> _output;

    /**
     * The Result of the lola call e.g. state space
     */
    private final ArrayList<String> _result;

    //private final String witnessPath;
    //private final String witnessState;
    public LolaResult(int val) {
        _exitValue = val;
        /* witnessState = "";
        witnessPath = ""; */
        _result = new ArrayList<String>();
        _output = new ArrayList<String>();
    }

    public LolaResult(int val, ArrayList<String> out) {
        _exitValue = val;
        /*witnessState = "";
        witnessPath = "";*/
        _result = new ArrayList<String>();
        _output = out;
    }

    public LolaResult(int val, ArrayList<String> out, ArrayList<String> res) {
        _exitValue = val;
        /*witnessState = "";
        witnessPath = "";*/
        _result = res;
        _output = out;
    }

    /**
     * @return the exitValue
     */
    public int getExitValue() {
        return _exitValue;
    }

    /**
     * The output of the lola call as it got written to the
     * standard error stream.
     *
     * @return the output
     */
    public ArrayList<String> getOutput() {
        return _output;
    }

    /**
     * @return the witnessPath
     */


//    protected String getWitnessPath() {
//        return witnessPath;
//    }
//
//    /**
//     * @return the witnessState
//     */
//    protected String getWitnessState() {
//        return witnessState;
//    }
    public boolean isEmpty() {
        return (_output.isEmpty());
    }

    /**
     * Returns the result of the lola call
     * @param _result
     */
    public ArrayList<String> getResult() {
        return _result;
    }
}