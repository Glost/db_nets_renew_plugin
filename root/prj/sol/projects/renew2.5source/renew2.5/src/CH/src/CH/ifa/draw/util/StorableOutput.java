/*
 * @(#)StorableOutput.java 5.1
 *
 */
package CH.ifa.draw.util;

import java.awt.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.URI;


/**
 * An output stream that can be used to flatten Storable objects.
 * StorableOutput preserves the object identity of the stored objects.
 *
 * @see Storable
 * @see StorableInput
 */
public class StorableOutput extends StorableInOut {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StorableOutput.class);
    private PrintWriter fStream;
    private int fIndent;

    /**
     * Initializes the StorableOutput with the file given by name.
     */
    public StorableOutput(File file) throws FileNotFoundException {
        this(file.toURI(), new FileOutputStream(file));
    }

    /**
     * Initializes the StorableOutput with the given output stream.
     */
    public StorableOutput(OutputStream stream) {
        this(null, stream);
    }

    /**
     * Initializes the StorableOutput with the given output stream.
     */
    private StorableOutput(URI location, OutputStream stream) {
        super(location);
        try {
            fStream = new PrintWriter(new OutputStreamWriter(stream, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("UTF-8 not supported!");
            fStream = new PrintWriter(stream);
        }
        fIndent = 0;
    }

    /**
     * Writes a storable object to the output stream.
     */
    public void writeStorable(Storable storable) {
        if (storable == null) {
            fStream.print("NULL");
            space();
            return;
        }

        if (mapped(storable)) {
            writeRef(storable);
            return;
        }

        incrementIndent();
        startNewLine();
        map(storable);
        fStream.print(storable.getClass().getName());
        space();
        storable.write(this);
        space();
        decrementIndent();
    }

    /**
     * Writes an int to the output stream.
     */
    public void writeInt(int i) {
        fStream.print(i);
        space();
    }

    public void writeColor(Color c) {
        writeInt(c.getRed());
        writeInt(c.getGreen());
        writeInt(c.getBlue());
    }

    /**
     * Writes an int to the output stream.
     */
    public void writeDouble(double d) {
        fStream.print(d);
        space();
    }

    /**
     * Writes an int to the output stream.
     */
    public void writeBoolean(boolean b) {
        if (b) {
            fStream.print(1);
        } else {
            fStream.print(0);
        }
        space();
    }

    /**
     * Writes a string to the output stream. Special characters
     * are quoted.
     */
    public void writeString(String s) {
        fStream.print('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\n':
                fStream.print('\\');
                fStream.print('n');
                break;
            case '"':
                fStream.print('\\');
                fStream.print('"');
                break;
            case '\\':
                fStream.print('\\');
                fStream.print('\\');
                break;
            case '\t':
                fStream.print('\\');
                fStream.print('\t');
                break;
            default:
                fStream.print(c);
            }
        }
        fStream.print('"');
        space();
    }

    /**
     * Closes a storable output stream.
     */
    public void close() {
        fStream.close();
    }

    private void writeRef(Storable storable) {
        int ref = getRef(storable);

        fStream.print("REF");
        space();
        fStream.print(ref);
        space();
    }

    private void incrementIndent() {
        fIndent += 4;
    }

    private void decrementIndent() {
        fIndent -= 4;
        if (fIndent < 0) {
            fIndent = 0;
        }
    }

    private void startNewLine() {
        fStream.println();
        for (int i = 0; i < fIndent; i++) {
            space();
        }
    }

    private void space() {
        fStream.print(' ');
    }
}