package de.renew.util;

import java.util.Hashtable;
import java.util.Vector;


/**
 * A parametered command line represents the argument
 * string array of the main method, after it has been
 * analyzed for any named parameters. A named parameter
 * is one beginning with a hyphen, followed by none or
 * some values. This class provides functionality for
 * switch parameters (without values) and valued
 * parameters. All parameteres that are not named are
 * also collected and can be used like the original
 * args parameter.
 */
public class ParameteredCommandLine {

    /**
     * The array of all remaining arguments, after they
     * have been filtered for named parameters.
     */
    private String[] remainingArgs = null;
    /**
     * The hashtable of all named parameters.
     */
    Hashtable<String, String[]> parameterValues = null;

    /**
     * Creates the parametered command line.
     * @param args The original main method arguments.
     * @param possibleParameters An array of all
     * accepted parameter names, each beginning with
     * a hyphen.
     * @param parameterValueCounts An array of the
     * number of values for each named parameter.
     * This is the number of parameters after the
     * parameter name. The array must have the same
     * length and order as the possibleParameters array.
     */
    public ParameteredCommandLine(String[] args, String[] possibleParameters,
                                  int[] parameterValueCounts) {
        Vector<String> remainingArgsVector = new Vector<String>();
        parameterValues = new Hashtable<String, String[]>();

        for (int argNumber = 0; argNumber < args.length; argNumber++) {
            if (args[argNumber].charAt(0) == '-') {
                String argParameter = args[argNumber].substring(1);
                for (int parameterNumber = 0;
                             parameterNumber < possibleParameters.length;
                             parameterNumber++) {
                    String possibleParameter = possibleParameters[parameterNumber];
                    if (possibleParameter.charAt(0) == '-') {
                        possibleParameter = possibleParameter.substring(1);
                    }

                    if (possibleParameter.equals(argParameter)) {
                        int parameterValueCount = parameterValueCounts[parameterNumber];
                        Vector<String> values = new Vector<String>();
                        int valueNumber;
                        for (valueNumber = 0, argNumber++;
                                     argNumber < args.length
                                     && valueNumber < parameterValueCount
                                     && args[argNumber].charAt(0) != '-';
                                     argNumber++, valueNumber++) {
                            values.addElement(args[argNumber]);
                        }

                        String[] valuesArray = new String[values.size()];
                        values.copyInto(valuesArray);
                        parameterValues.put(possibleParameter, valuesArray);

                        argNumber--;
                        break;
                    }
                }
            } else {
                remainingArgsVector.addElement(args[argNumber]);
            }
        }

        remainingArgs = new String[remainingArgsVector.size()];
        remainingArgsVector.copyInto(remainingArgs);
    }

    /**
     * Returns the array of values given in the
     * command line arguments after a parameter name
     * or null if the parameter name does not exist.
     * @param parameter The name of the parameter
     * to return its values of.
     * @return The values of the parameter.
     */
    public String[] getParameterValues(String parameter) {
        if (parameter.charAt(0) == '-') {
            parameter = parameter.substring(1);
        }
        return parameterValues.get(parameter);
    }

    /**
     * Returns an array of all parameters that are
     * not bound to any named parameter.
     * @return The array of all remaining parameters.
     */
    public String[] getRemainingArgs() {
        return remainingArgs;
    }

    /**
     * Returns if a parameter name exists in the
     * command line arguments.
     * @param parameter The name of the parameter
     * to checks its existance of.
     * @return If the parameter name exists.
     */
    public boolean hasParameter(String parameter) {
        if (parameter.charAt(0) == '-') {
            parameter = parameter.substring(1);
        }
        return parameterValues.containsKey(parameter);
    }
}