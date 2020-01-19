package CH.ifa.draw.framework;

import CH.ifa.draw.io.SimpleFileFilter;


/**
 * Listeners of this type can be registered at
 * {@link DrawingTypeManager#addListener} and are informed about changes
 * in the set of known drawing types.
 * <p>
 * </p>
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public interface DrawingTypeManagerListener {

    /**
     * Called whenever a file type has been registered via
     * {@link DrawingTypeManager#register}. It does not matter,
     * whether the registration replaced previously known type
     * information or not.
     *
     * @param name    the registration key.
     * @param filter  the <code>SimpleFileFilter</code> object
     *                denoting the file type.
     **/
    public void typeRegistered(String name, SimpleFileFilter filter);

    /**
     * Called whenever the default file type has been changed via
     * {@link DrawingTypeManager#setDefaultFileFilter}.
     *
     * @param filter  the <code>SimpleFileFilter</code> object
     *                denoting the new default file type.
     **/
    public void defaultTypeChanged(SimpleFileFilter filter);
}