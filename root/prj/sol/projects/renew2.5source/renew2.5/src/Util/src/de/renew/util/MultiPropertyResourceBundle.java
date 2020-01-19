package de.renew.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;


/**
 * The multi property resource bundle is a resource bundle
 * that returns the string properties as string arrays,
 * separating the values at each comma.
 * So, if the property file contains
 * <blockquote><pre>
 * foo=this,that
 * </pre></blockquote>
 * then the multi property resource bundle returns this and that
 * in a two-element array if getStringArray is called.
 */
public class MultiPropertyResourceBundle extends ResourceBundle {

    /**
     * The encapsulated resource bundle.
     */
    private ResourceBundle bundle;

    /**
     * Creates a new multi property resource bundle.
     * @param bundle The resource bundle to be used.
     */
    public MultiPropertyResourceBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Creates a new multi property resource bundle.
     * Uses the default locale and the current class loader configured in
     * {@link ClassSource}.
     * @param baseName The base name of the resource bundle to be opened.
     */
    public MultiPropertyResourceBundle(String baseName) {
        this(baseName, Locale.getDefault());
    }

    /**
     * Creates a new multi property resource bundle.
     * Uses the current class loader configured in {@link ClassSource}.
     * @param baseName The base name of the resource bundle to be opened.
     * @param locale The locale.
     */
    public MultiPropertyResourceBundle(String baseName, Locale locale) {
        this(baseName, locale, ClassSource.getClassLoader());
    }

    /**
     * Creates a new multi property resource bundle.
     * @param baseName The base name of the resource bundle to be opened.
     * @param locale The locale.
     * @param loader The class loader to be used.
     */
    public MultiPropertyResourceBundle(String baseName, Locale locale,
                                       ClassLoader loader) {
        bundle = ResourceBundle.getBundle(baseName, locale, loader);
    }

    /**
     * Returns the bundle's keys.
     * @return The keys.
     */
    public Enumeration<String> getKeys() {
        return bundle.getKeys();
    }

    /**
     * Returns the bundle's locale.
     * @return The locale.
     */
    public Locale getLocale() {
        return bundle.getLocale();
    }

    /**
     * Returns the object for a given key.
     * @param key The key.
     * @return The object.
     * @exception MissingResourceException The resource cannot be found.
     */
    protected Object handleGetObject(String key)
            throws MissingResourceException {
        String value = bundle.getString(key);

        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        List<String> tokens = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }

        String[] strings = new String[tokens.size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = tokens.get(i).trim();
        }

        return strings;
    }

    /**
     * Sets the bundle's parent.
     * @param parent The new parent.
     */
    protected void setParent(ResourceBundle parent) {
        throw new UnsupportedOperationException("Multi property resource bundles do not support parents");
    }
}