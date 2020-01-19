/*
 * Created on 16.05.2003
 *
 */
package de.renew.shadow;



/**
 * @author 6schumac
 *
 * This is a ShadowLookup extension expressing whether the lookup
 * is for sequential simulation only.
 * getSequentialOnly() returns true if any element in the lookup
 * is known to cause problems when used in parallel simulation.
 */
public class SequentialOnlyExtension implements ShadowLookupExtension {
    // the factory producing this kind of extension
    private static final Factory _factory = new Factory();

    // is this lookup sequential?
    private boolean _sequentialOnly = false;

    public boolean getSequentialOnly() {
        return _sequentialOnly;
    }

    public void setSequentialOnly(boolean sequential) {
        _sequentialOnly = sequential;
    }

    public static SequentialOnlyExtension lookup(ShadowLookup lookup) {
        return (SequentialOnlyExtension) lookup.getShadowLookupExtension(_factory);
    }

    public static class Factory implements ShadowLookupExtensionFactory {
        public String getCategory() {
            return "de.renew.formalism.sequential";
        }

        public ShadowLookupExtension createExtension() {
            return new SequentialOnlyExtension();
        }
    }
}