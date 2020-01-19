package de.renew.refactoring.edit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.List;


/**
 * Abstract superclass for simple file editors that edit a file line by line.
 * This class handles i/o operations and leaves subclasses to implement
 * {@link #editLine(String)}.
 *
 * @author 2mfriedr
 */
public abstract class LineByLineFileEditor extends IteratorEditor<File, Void> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LineByLineFileEditor.class);

    /**
     * Constructs a new line by line file editor.
     *
     * @param edits a list of edits
     */
    public LineByLineFileEditor(final List<File> edits) {
        super(edits);
    }

    /**
     * Returns the current filename.
     */
    @Override
    public String getCurrentEditString() {
        return getCurrentEdit().getName();
    }

    @Override
    protected Void performEdit(final File file) {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            File tmpFile = new File(file.getPath() + "tmp");
            tmpFile.createNewFile();
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));

            String oldLine = in.readLine();
            while (oldLine != null) {
                String newLine = editLine(oldLine);
                out.write(newLine);
                out.newLine();
                oldLine = in.readLine();
            }
            if (file.delete() && tmpFile.renameTo(file)) {
                logger.info("Replacing file successful: " + file);
            }
        } catch (IOException e) {
            logger.error("I/O Error occured while trying to replace " + file
                         + ".");
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Returns the line that should replace the original line in the file.
     *
     * @param line the original line, is never {@code null}
     * @return the new line to replace the original line, should never be
     * {@code null}
     */
    protected abstract String editLine(String line);
}