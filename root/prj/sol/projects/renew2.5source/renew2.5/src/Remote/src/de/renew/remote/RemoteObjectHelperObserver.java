/*
 * Created on 03.02.2006
 */
package de.renew.remote;



/**
 * Interface for classes the wish to observe changes in a RemoteObjectHelper.
 *
 * See {@link de.renew.remote.RemoteObjectHelper} for details.
 */
public interface RemoteObjectHelperObserver {

    /**
     * Notifies the observer of a newly found object and its location identifier
     *
     * @param identifier location identifier of this object
     *                   Note: The "identifier" isn't neccessarily a valid URL.
     * @param object the newly found object
     */
    public void notifyObjectAdded(String location, Object object);

    /**
     * Notifies the observer that a previously added object has been removed.
     * Since the object itself is no longer valid, its identifier is used.
     *
     * @param identifier location identifier of the removed object
     *                   Note: The "identifier" isn't neccessarily a valid URL.
     */
    public void notifyObjectRemoved(String location);
}