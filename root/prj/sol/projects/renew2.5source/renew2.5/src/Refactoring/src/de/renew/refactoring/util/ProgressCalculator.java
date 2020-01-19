package de.renew.refactoring.util;



/**
 * Helper for calculating progress.
 *
 * @author 2mfriedr
 */
public final class ProgressCalculator {

    /**
     * Should not be instantiated
     */
    private ProgressCalculator() {
    }

    /**
     * Calculates progress as the ratio between the number of done items and
     * the number of all items to be done.
     *
     * @param done the number of done items
     * @param all the number of all items to be done
     * @return an integer that is greater than or equal to 0 and less than or
     * equal to 100
     */
    public static int calculateProgress(final int done, final int all) {
        if (all == 0) {
            return 100;
        }

        return sanitizeProgress(100.0 * done / all);
    }

    /**
     * Calculates the combined progress of two operations.
     *
     * @param firstOperationProgress the first operation's progress
     * @param firstOperationItems the first operations number of items
     * @param secondOperationProgress the second operation's progress
     * @param secondOperationItems the second operation's number of items
     * @return an integer that is greater than or equal to 0 and less than or
     * equal to 100
     */
    public static int combinedProgress(final int firstOperationProgress,
                                       final int firstOperationItems,
                                       final int secondOperationProgress,
                                       final int secondOperationItems) {
        if (firstOperationItems + secondOperationItems == 0) {
            return 100;
        }

        return sanitizeProgress((firstOperationItems * firstOperationProgress
                                + secondOperationItems * secondOperationProgress) / (firstOperationItems
                                                                                    + secondOperationItems));
    }

    /**
     * Returns a valid progress integer based on the input.
     *
     * @param progress a number
     * @return an integer that is greater than or equal to 0 and less than or
     * equal to 100
     */
    private static int sanitizeProgress(final double progress) {
        return sanitizeProgress((int) Math.round(progress));
    }

    /**
     * Returns a valid progress integer based on the input.
     *
     * @param progress a number
     * @return an integer that is greater than or equal to 0 and less than or
     * equal to 100
     */
    private static int sanitizeProgress(final int progress) {
        if (progress < 0) {
            return 0;
        }
        if (progress > 100) {
            return 100;
        }
        return progress;
    }
}