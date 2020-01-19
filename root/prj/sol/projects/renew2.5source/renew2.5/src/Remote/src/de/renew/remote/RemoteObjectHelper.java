/*
 * Created on 31.01.2006
 */
package de.renew.remote;

import de.renew.remote.RemoteServerRegistry.ServerDescriptor;

import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manages a list of remote objects of a given service type.
 * The service type corresponds to the registerObject-methods from
 * the {@link de.renew.remote.RemoteServerRegistry}.
 *
 * The list is automatically updated as new servers are connected.
 * These updates can be observed by any
 * {@link de.renew.remote.RemoteObjectHelperObserver}.
 *
 * Note that while all Maps retrieved via the getters are independant
 * from each other, the objects they obtain ARE NOT.
 * All classes that intend to use the map should properly implement
 * RemoteObjectHelperObserver.
 */
public class RemoteObjectHelper implements RemoteServerRegistryListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(RemoteObjectHelper.class);

    /**
     * The service-identifier this RemoteObjectHelper looks for
     */
    private String _service;

    /**
     * All currently known remote objects
     */
    private Map<String, Remote> _remoteObjects;

    /**
     * All currently known local objects
     */
    private Map<String, Object> _localObjects;

    /**
     * Counts the local objects in order to assign them a unique name
     */
    private int _localCounter = 0;

    /**
     * All currently subscribed observers
     */
    private Set<RemoteObjectHelperObserver> _observers;

    /**
     * Creates a new helper that manages a list of remote and local
     * objects that match the given service identifier.
     *
     * @param service identifies the service to look for
     */
    public RemoteObjectHelper(String service) {
        _service = service;
        _remoteObjects = new HashMap<String, Remote>();
        _observers = new HashSet<RemoteObjectHelperObserver>();
        _localObjects = new HashMap<String, Object>();

        RemoteServerRegistry registry = RemoteServerRegistry.instance();
        registry.addRegistryListener(this);
        registry.catchUpOnPreviousConnects(this);
    }

    /* (non-Javadoc)
     * @see de.renew.remote.RemoteServerRegistryListener#connectedTo(de.renew.remote.RemoteServerRegistry.ServerDescriptor)
     */
    public void connectedTo(ServerDescriptor desc) {
        Remote object = null;
        String url;
        url = desc.getUrl(_service);

        try {
            object = Naming.lookup(url);
            _remoteObjects.put(url, object);
            logger.info("Found service " + _service + " on " + url);
            fireNotifyObjectAdded(url, object);
        } catch (RemoteException e) {
            logger.error("Could not lookup service " + _service + " on " + url
                         + ".\nException:\n" + e.getMessage(), e);
        } catch (MalformedURLException e) {
            logger.error("Could not lookup service " + _service + " on " + url
                         + ".\nException:\n" + e.getMessage(), e);
        } catch (NotBoundException e) {
            // who cares
        }
    }

    /* (non-Javadoc)
     * @see de.renew.remote.RemoteServerRegistryListener#disconnectedFrom(de.renew.remote.RemoteServerRegistry.ServerDescriptor)
     */
    public void disconnectedFrom(ServerDescriptor desc) {
        String url = desc.getUrl(_service);

        if (_remoteObjects.remove(url) != null) {
            fireNotifyObjectRemoved(url);
        }
    }

    /**
     * Adds a local object to this Helper as if it was a remotely found one.
     * It will be assigned an identifier of "local" + a number.
     *
     * @param object the object to add
     * @return the identifier assigned to this object
     */
    public String addLocalObject(Object object) {
        String identifier = "local" + _localCounter++;
        _localObjects.put(identifier, object);
        fireNotifyObjectAdded(identifier, object);

        return identifier;
    }

    /**
     * Removes a local object from this Helper as if it was a now disconnected
     * remote object.
     *
     * @param identifier the identifier previously assigned to this object
     */
    public void removeLocalObjectByIdentifier(String identifier) {
        _localObjects.remove(identifier);
        fireNotifyObjectRemoved(identifier);
    }

    /**
     * Removes a local object from this Helper as if it was a now disconnected
     * remote object.
     *
     * @param the object to remove
     */
    public void removeLocalObject(Object object) {
        List<String> keysToRemove = new LinkedList<String>();


        // Step 1: search for objects (keys) that match the parameter
        //         and record them for later removal
        for (Iterator<String> iter = _localObjects.keySet().iterator();
                     iter.hasNext();) {
            String key = iter.next();
            Object element = _localObjects.get(key);

            //NOTICEnull
            if ((object == null && element == null)
                        || (object != null && object.equals(element))) {
                keysToRemove.add(key);
            }
        }

        // Step 2: Remove the previously gathered keys
        for (Iterator<String> iter = keysToRemove.iterator(); iter.hasNext();) {
            String key = iter.next();
            fireNotifyObjectRemoved(key);
            _localObjects.remove(key);
        }
    }

    /**
     * @return A Map<String identifier, Object> of all (remote and local) objects found.
     *         Note: The "identifier" isn't neccessarily a valid URL.
     */
    public Map<String, Object> getObjects() {
        Map<String, Object> allObjects = getRemoteObjects();
        allObjects.putAll(getLocalObjects());
        return allObjects;
    }

    /**
     * @return A Map<String identifier, Object> of all remote objects found.
     *         Do not use this method unless you're sure you don't need the
     *         local objects.
     *         Note: The "identifier" isn't neccessarily a valid URL.
     */
    public Map<String, Object> getRemoteObjects() {
        Map<String, Object> currentRemoteObjects = new HashMap<String, Object>(_remoteObjects);

        return currentRemoteObjects;
    }

    /**
     * @return A Map<String identifier, Object> of all local objects found.
     *         Do not use this method unless you're sure you don't need the
     *         remote objects.
     *         Note: The "identifier" isn't neccessarily a valid URL.
     */
    public Map<String, Object> getLocalObjects() {
        Map<String, Object> currentLocalObjects = new HashMap<String, Object>(_localObjects);

        return currentLocalObjects;
    }

    protected void finalize() throws Throwable {
        super.finalize();

        RemoteServerRegistry.instance().removeRegistryListener(this);
    }

    /**
     * Adds an observer to this RemoteObjectHelper.
     *
     * @param observer the observer to add
     */
    public void addObserver(RemoteObjectHelperObserver observer) {
        _observers.add(observer);
    }

    /**
     * Removes an observer from this RemoteObjectHelper.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(RemoteObjectHelperObserver observer) {
        _observers.remove(observer);
    }

    /**
     * Notifies all observers of the newly found object and it's location identifier
     *
     * @param identifier location identifier of this object
     *                   Note: The "identifier" isn't neccessarily a valid URL.
     * @param object the newly found object
     */
    protected void fireNotifyObjectAdded(String identifier, Object object) {
        for (Iterator<RemoteObjectHelperObserver> iter = _observers.iterator();
                     iter.hasNext();) {
            RemoteObjectHelperObserver observer = iter.next();
            observer.notifyObjectAdded(identifier, object);
        }
    }

    /**
     * Notifies all observers that a previously added object has been removed.
     * Since the object itself is no longer valid, its identifier is used.
     *
     * @param identifier location identifier of the removed object
     *                   Note: The "identifier" isn't neccessarily a valid URL.
     */
    protected void fireNotifyObjectRemoved(String identifier) {
        for (Iterator<RemoteObjectHelperObserver> iter = _observers.iterator();
                     iter.hasNext();) {
            RemoteObjectHelperObserver observer = iter.next();
            observer.notifyObjectRemoved(identifier);
        }
    }
}