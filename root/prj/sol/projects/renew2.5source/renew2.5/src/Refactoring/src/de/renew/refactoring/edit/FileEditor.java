package de.renew.refactoring.edit;

import de.renew.refactoring.match.FileMatch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.List;


public abstract class FileEditor extends IteratorEditor<FileMatch, Void> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FileEditor.class);

    public FileEditor(List<FileMatch> edits) {
        super(new FileMatchSorter().sorted(edits));
    }

    /**
     * Returns the current filename.
     */
    @Override
    public String getCurrentEditString() {
        return getCurrentEdit().getFile().getName();
    }

    @Override
    protected Void performEdit(FileMatch edit) {
        BufferedReader in = null;
        BufferedWriter out = null;
        File file = edit.getFile();
        try {
            File tmpFile = new File(file.getPath() + "tmp");
            tmpFile.createNewFile();
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));

            String oldLine = in.readLine();
            int lineNumber = 1;
            while (oldLine != null) {
                String newLine = (lineNumber == edit.getLine())
                                 ? editLine(oldLine, edit) : oldLine;
                out.write(newLine);
                out.newLine();
                oldLine = in.readLine();
                lineNumber += 1;
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

    protected abstract String editLine(final String line, final FileMatch edit);
}