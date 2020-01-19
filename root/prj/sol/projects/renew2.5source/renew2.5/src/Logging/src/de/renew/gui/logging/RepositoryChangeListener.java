/*
 * Created on 26.11.2004
 *
 */
package de.renew.gui.logging;



/**
 * Methods to inform about changes in a StepTraceRepository.
 *
 * @author Sven Offermann
 */
public interface RepositoryChangeListener extends StepTraceChangeListener {

    /**
     * Called if a new StepTrace was created.
     *
     * @param repository the repository to which the created StepTrace
     *                   belongs.
     * @param stepTrace the new created StepTrace.
     */
    public void stepTraceAdded(StepTraceRepository repository,
                               StepTrace stepTrace);

    /**
     * Called if a StepTrace was removed from a repository.
     *
     * @param repository the repository to which the removed StepTrace
     *                   belonged.
     * @param stepTrace the removed StepTrace.
     */
    public void stepTraceRemoved(StepTraceRepository repository,
                                 StepTrace stepTrace);

    /**
     * Called if a StepTrace is requested for removal from a repository. The
     * Listeners can veto the removal of the StepTrace by calling the veto
     * method of the StepTraceRemovalRequest object.
     * <p>
     * It is not guaranteed that all listeners receive the request. Repositories
     * may cancel listener notification as soon as one listener issues a veto.
     * </p>
     *
     * @param request the StepTraceRemovalRequest with the StepTrace to remove.
     */
    public void stepTraceRemoveRequest(StepTraceRemoveRequest request);
}