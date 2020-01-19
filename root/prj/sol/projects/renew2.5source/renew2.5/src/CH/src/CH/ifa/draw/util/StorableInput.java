/*
 * @(#)StorableInput.java 5.1
 *
 */
package CH.ifa.draw.util;

import de.renew.plugin.PluginManager;

import java.awt.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URL;


/**
 * An input stream that can be used to resurrect Storable objects.
 * StorableInput preserves the object identity of the stored objects.
 *
 * @see Storable
 * @see StorableOutput
 */
public class StorableInput extends StorableInOut {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StorableInput.class);
    private StreamTokenizer fTokenizer;
    private Reader fReader;

    // The version Integer.MAX_VALUE denoted the most current
    // version. It is used when no version is explicitly given.
    private int version = Integer.MAX_VALUE;

    /**
     * Initializes a Storable input from the file given by name.
     * useUTF specifies whether UTF or the platform default
     * character encoding is to be used.
     */
    public StorableInput(URL location, boolean useUTF)
            throws IOException {
        this(URI.create(location.toString()), location.openStream(), useUTF);
    }

    /**
     * Initializes a Storable input from the file given by name.
     * useUTF specifies whether UTF or the platform default
     * character encoding is to be used.
     */
    public StorableInput(File file, boolean useUTF)
            throws FileNotFoundException {
        this(file.toURI(), new FileInputStream(file), useUTF);
    }

    /**
     * Initializes a Storable input with the given input stream.
     * UTF character encoding is used.
     */
    public StorableInput(InputStream stream) {
        this(null, stream, true);
    }

    /**
     * Initializes a Storable input with the given input stream.
     * useUTF specifies whether UTF or the platform default
     * character encoding is to be used.
     */
    public StorableInput(InputStream stream, boolean useUTF) {
        this(null, stream, useUTF);
    }

    private StorableInput(URI location, InputStream stream, boolean useUTF) {
        super(location);
        if (useUTF) {
            try {
                fReader = new BufferedReader(new InputStreamReader(stream,
                                                                   "UTF8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("UTF-8 not supported!");
            }
        }
        if (fReader == null) {
            fReader = new BufferedReader(new InputStreamReader(stream));
        }
        fTokenizer = new StreamTokenizer(fReader);
        fTokenizer.wordChars('_', '_'); /* '_' can be in a class name */
    }

    /**
     * Initializes a Storable input with the given string.
     */
    public StorableInput(String stringStream) {
        this(null, stringStream);
    }

    private StorableInput(URI location, String stringStream) {
        super(location);
        if (fReader == null) {
            fReader = new BufferedReader(new StringReader(stringStream));
        }
        fTokenizer = new StreamTokenizer(fReader);
        fTokenizer.wordChars('_', '_'); /* '_' can be in a class name */
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Reads and resurrects a Storable object from the input stream.
     */
    public Storable readStorable() throws IOException {
        Storable storable;
        String s = readString();

        if (s.equals("NULL")) {
            return null;
        }

        if (s.equals("REF")) {
            int ref = readInt();
            return retrieve(ref);
        }

        storable = (Storable) makeInstance(s);
        map(storable);
        storable.read(this);
        return storable;
    }

    /**
     * Reads a string from the input stream.
     */
    public String readString() throws IOException {
        int token = fTokenizer.nextToken();
        if (token == StreamTokenizer.TT_WORD || token == '"') {
            return fTokenizer.sval;
        }

        String msg = "String expected in line: " + fTokenizer.lineno();
        throw new IOException(msg);
    }

    /**
     * Determines whether an int could be read from the input stream.
     */
    public boolean canReadInt() throws IOException {
        int token = fTokenizer.nextToken();
        fTokenizer.pushBack();
        return token == StreamTokenizer.TT_NUMBER;
    }

    /**
     * Reads an int from the input stream.
     */
    public int readInt() throws IOException {
        int token = fTokenizer.nextToken();
        if (token == StreamTokenizer.TT_NUMBER) {
            return (int) fTokenizer.nval;
        }

        String msg = "Integer expected in line: " + fTokenizer.lineno();
        throw new IOException(msg);
    }

    /**
     * Reads a color from the input stream.
     */
    public Color readColor() throws IOException {
        return new Color(readInt(), readInt(), readInt());
    }

    /**
     * Reads a double from the input stream.
     */
    public double readDouble() throws IOException {
        int token = fTokenizer.nextToken();
        if (token == StreamTokenizer.TT_NUMBER) {
            return fTokenizer.nval;
        }

        String msg = "Double expected in line: " + fTokenizer.lineno();
        throw new IOException(msg);
    }

    /**
     * Reads a boolean from the input stream.
     */
    public boolean readBoolean() throws IOException {
        int token = fTokenizer.nextToken();
        if (token == StreamTokenizer.TT_NUMBER) {
            return (int) fTokenizer.nval == 1;
        }

        String msg = "Integer expected in line: " + fTokenizer.lineno();
        throw new IOException(msg);
    }

    protected Object makeInstance(String className) throws IOException {
        try {
            Class<?> cl = Class.forName(className, true,
                                        PluginManager.getInstance()
                                                     .getBottomClassLoader());
            return cl.newInstance();
        } catch (NoSuchMethodError e) {
            throw new IOException("Class " + className
                                  + " does not seem to have a no-arg constructor",
                                  e);
        } catch (ClassNotFoundException e) {
            throw new UnknownTypeException("No class: " + className, className);
        } catch (InstantiationException e) {
            throw new IOException("Cannot instantiate: " + className);
        } catch (IllegalAccessException e) {
            throw new IOException("Class (" + className + ") not accessible");
        }
    }

    /**
     * Closes a storable input stream.
     */
    public void close() {
        try {
            fReader.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Put the token back.
     */
    public void putBack() {
        fTokenizer.pushBack();
    }
}