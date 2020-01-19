package de.renew.net;

import java.io.Serializable;


/**
 * An id for net elements. It consists of the figure id of the
 * figure it was created of, and a group id to distinguish net
 * elements of the same figure.
 */
public class NetElementID implements Serializable {

    /**
     * The default group id.
     */
    public static final Serializable DEFAULT_GROUP_ID = new DefaultGroupID();

    /**
     * The figure ID the net element was created of.
     */
    private int figureID;

    /**
     * A serializable object that identifies the net element
     * if there are several net elements for the same figure.
     * This group ID may be e.g. an integer object, a string,
     * or an array, according to the use case.
     * If there is only one net element for the respective
     * figure, you can use the DEFAULT_GROUP_ID, which is
     * the initial value.
     */
    private Serializable groupID;

    /**
     * Creates a new net element id.
     */
    public NetElementID() {
        groupID = DEFAULT_GROUP_ID;
    }

    /**
     * Creates a new net element id with a set figure id.
     * @param figureID The figure id.
     */
    public NetElementID(int figureID) {
        this.figureID = figureID;
        groupID = DEFAULT_GROUP_ID;
    }

    /**
     * Creates a new net element id with a set figure and group id.
     * @param figureID The figure id.
     * @param groupID The group id.
     */
    public NetElementID(int figureID, Serializable groupID) {
        if (groupID == null) {
            throw new IllegalArgumentException("A null group id is invalid");
        }
        this.figureID = figureID;
        this.groupID = groupID;
    }

    /**
     * Returns whether the ID equals another object.
     * If must be an instance of NetElementID, the figure
     * id must be the same and the group id must be equal.
     * @return Whether the ID equals another object.
     */
    public boolean equals(Object otherObject) {
        if (otherObject != null && otherObject instanceof NetElementID) {
            NetElementID otherID = (NetElementID) otherObject;
            return otherID.getFigureID() == figureID
                   && groupID.equals(otherID.getGroupID());
        } else {
            return false;
        }
    }

    /**
     * Returns the figure ID the net element was created of.
     * @return The figure ID.
     */
    public int getFigureID() {
        return figureID;
    }

    /**
     * Returns the serializable object that identifies the net element
     * if there are several net elements for the same figure.
     * @return The group ID.
     */
    public Serializable getGroupID() {
        return groupID;
    }

    /**
     * Returns the hash code of the id.
     * @return The hash code.
     */
    public int hashCode() {
        if (groupID != null) {
            return figureID + groupID.hashCode();
        } else {
            return figureID;
        }
    }

    /**
     * Returns the string representation of this id.
     * @return The string representation.
     */
    public String toString() {
        return "[" + figureID + "," + groupID + "]";
    }

    /**
     * An empty serializable object representing the default group id.
     */
    public static class DefaultGroupID implements Serializable {

        /**
         * Creates a new default group id.
         */
        private DefaultGroupID() {
        }

        /**
         * Returns whether the default group ID equals another object.
         * This is only if the object is also the default group ID.
         * @return Whether the group ID equals another object.
         */
        public boolean equals(Object object) {
            return object instanceof DefaultGroupID;
        }

        /**
         * Returns the hash code of the group id.
         * @return The hash code.
         */
        public int hashCode() {
            // An arbitrary integer for the default group id.
            return 1234;
        }

        /**
         * Returns the string representation of the default group id.
         * @return The string representation.
         */
        public String toString() {
            return "_default";
        }
    }
}