/*
 * Created on 19.10.2004
 */
package de.renew.engine.common;

import de.renew.engine.simulator.SimulationThreadPool;


/**
 * @author Sven Offermann
 */
public class StepIdentifier implements Comparable<StepIdentifier> {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StepIdentifier.class);
    private final long simulationRunId;
    private final long[] stepCountVector;

    public StepIdentifier(long simulationRunId, long[] stepCountVector) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.simulationRunId = simulationRunId;
        this.stepCountVector = stepCountVector;
    }

    public long[] getComponents() {
        return this.stepCountVector;
    }

    public long getSimulationRunId() {
        return this.simulationRunId;
    }

    public int hashCode() {
        if (this.stepCountVector.length >= 1) {
            return (int) this.stepCountVector[0];
        }

        return 0;
    }

    public boolean equals(Object otherObj) {
        if (otherObj instanceof StepIdentifier) {
            StepIdentifier other = (StepIdentifier) otherObj;

            return (compareTo(other) == 0);
        }

        return false;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");

        if (this.stepCountVector.length >= 1) {
            buffer.append(Long.toString(this.stepCountVector[0]));
        }

        for (int x = 1; x < this.stepCountVector.length; x++) {
            buffer.append(",");
            buffer.append(Long.toString(this.stepCountVector[x]));
        }

        buffer.append(")");

        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final StepIdentifier o) throws ClassCastException {
        StepIdentifier other = o;
        long[] otherComp = other.getComponents();

        // first compare the simulationRunId
        if (getSimulationRunId() < other.getSimulationRunId()) {
            // other simulationRunId is greater
            return -1;
        } else if (getSimulationRunId() < other.getSimulationRunId()) {
            // other simulationRunId is lesser
            return 1;
        }


        // second compare the elements of the stepCountVector
        // if the simulationId is equal, the stepIdentifiers should
        // come from the same simulator, so the length of the 
        // stepCountVector should be the same.
        for (int x = 0; x < otherComp.length; x++) {
            if (otherComp[x] > stepCountVector[x]) {
                // other StepIdentifier is greater
                return -1;
            } else if (otherComp[x] < stepCountVector[x]) {
                // other StepIdentifier is lesser
                return 1;
            }
        }


        // all vector elements and the simulationRunId are equal, 
        // so the StepIdentifers are equal.
        return 0;
    }
}