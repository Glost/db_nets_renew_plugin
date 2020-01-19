package de.renew.shadow;



/**
 * A factory that creates shadow lookup extension
 * of a certain category.
 **/
public interface ShadowLookupExtensionFactory {

    /**
     * Return the extension category handled by this
     * factory.
     **/
    public String getCategory();

    /**
     * Return a new instance of the extension class
     * managed by this object.
     **/
    public ShadowLookupExtension createExtension();
}