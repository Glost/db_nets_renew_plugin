package de.renew.navigator.vc;



/**
 * The VersionControlAggregator holds a list of different version controls while
 * behaving as a version control itself.
 *
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-14
 */
public interface VersionControlAggregator extends VersionControl {

    /**
     * Adds an VCS accessor to the Navigator.
     *
     * @param vcs The VCS to add
     */
    void addVersionControl(VersionControl vcs);

    /**
     * Removes an VCS accessor from the Navigator.
     *
     * @param vcs The VCS to remove
     * @return if successfully removed
     */
    boolean removeVersionControl(VersionControl vcs);
}