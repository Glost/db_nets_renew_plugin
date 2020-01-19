package de.renew.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;


public class PatchingStorableInput extends CH.ifa.draw.util.StorableInput {

    /**
     * enables patching when a string attribute is read.
     */
    private boolean stringReadPatching = true;

    /**
     * maps prefixes which must be replaced by another prefix.
     */
    private static final Hashtable<String, String> patchPrefixes = new Hashtable<String, String>();

    static {
        patchPrefixes.put("CH.ifa.draw.cpn.", "de.renew.gui.");
    }

    /**
     * maps classes which must be replaced by other classes.
     */
    private static final Hashtable<String, String> patchMap = new Hashtable<String, String>();

    static {
        patchMap.put("de.renew.gui.fs.AssocArrowTip",
                     "de.renew.gui.AssocArrowTip");
        patchMap.put("de.renew.gui.fs.IsaArrowTip", "de.renew.gui.IsaArrowTip");
        patchMap.put("de.renew.diagram.AssocArrowTip",
                     "de.renew.gui.AssocArrowTip");
        patchMap.put("de.renew.fa.figures.AssocArrowTip",
                     "de.renew.gui.AssocArrowTip");
    }

    PatchingStorableInput(InputStream stream, boolean useUTF) {
        super(stream, useUTF);
    }

    PatchingStorableInput(File file, boolean useUTF)
            throws FileNotFoundException {
        super(file, useUTF);
    }

    /**
     * Initializes a Storable input with the given string.
     */
    public PatchingStorableInput(String stringStream) {
        super(stringStream);
    }

    public PatchingStorableInput(URL location, boolean useUTF)
            throws IOException {
        super(location, useUTF);
    }

    protected Object makeInstance(String className) throws IOException {
        return super.makeInstance(patchString(className));
    }

    public String readString() throws IOException {
        String s = super.readString();

        if (stringReadPatching) {
            s = patchString(s);
        }

        return s;
    }

    protected String patchString(String s) {
        boolean patched = false;
        Enumeration<String> e = patchPrefixes.keys();
        while (e.hasMoreElements() && !patched) {
            String patchPrefix = e.nextElement();
            if (s.startsWith(patchPrefix)) {
                s = patchPrefixes.get(patchPrefix)
                    + s.substring(patchPrefix.length());
                patched = true;
            }
        }
        String patch = patchMap.get(s);
        if (patch != null) {
            s = patch;
        }

        return s;
    }
}