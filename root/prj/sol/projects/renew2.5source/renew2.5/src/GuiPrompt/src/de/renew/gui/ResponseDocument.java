package de.renew.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/**
 * A plain-text <code>Document</code> that updates its contents to capture
 * all output from a stream. The document waits for input until a
 * {@link IOException} occurs or it is terminated explicitly.
 * <p>
 * If a thread writing to the stream dies (<code>IOException: Write end
 * dead</code>), the <code>ResponseDocument</code> goes to sleep until
 * {@link #revive} is called.
 * </p>
 *
 * Created: Mon Jun  6  2005
 *
 * @author Michael Duvigneau
 **/
public class ResponseDocument extends PlainDocument {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ResponseDocument.class);
    private final BufferedReader reader;
    private boolean terminated = false;
    private Object monitor = new Object();
    private Thread responseReaderThread;

    /**
     * Creates a new <code>ResponseDocument</code> listening to the given stream.
     *
     * @param stream  the <code>PipedOutputStream</code> where the document
     *                should connect to.
     *
     * @exception IOException if the initial connection to the stream fails.
     **/
    public ResponseDocument(final PipedOutputStream stream)
            throws IOException {
        super();
        this.reader = new BufferedReader(new InputStreamReader(new PipedInputStream(stream)));
        responseReaderThread = new Thread() {
                public void run() {
                    while (!terminated) {
                        try {
                            appendText(reader.readLine());
                        } catch (IOException e) {
                            if ("Write end dead".equals(e.getMessage())) {
                                appendText("\n----\n");
                                synchronized (monitor) {
                                    try {
                                        monitor.wait();
                                    } catch (InterruptedException e2) {
                                        logger.debug("Feedback thread interrupted.");
                                        appendText("\nFeedback thread interrupted.");
                                        terminate();
                                    }
                                }
                            } else {
                                logger.debug("Feedback exception: " + e, e);
                                appendText("\nFeedback exception: " + e);
                                terminate();
                            }
                        }
                    }
                    appendText("\nFeedback terminated. Press [Clear] to reinitialize.");
                }
            };
        responseReaderThread.setName("GuiPrompt feedback reader thread");
        responseReaderThread.start();
    }

    /**
     * Appends the given line of text to the end of the document.
     *
     * @param line  the text to append.
     **/
    public void appendText(String line) {
        try {
            insertString(getLength(), line + "\n", null);
        } catch (BadLocationException e) {
            logger.error("Bad location!? getLength=" + getLength(), e);
        }
    }

    /**
     * Cancels the automatical update feature of the <code>ResponseDocument</code>.
     **/
    public void terminate() {
        terminated = true;
        responseReaderThread.interrupt();
    }

    /**
     * Revives the automatical update feature of the
     * <code>ResponseDocument</code> if has been paused due to an inactive
     * output thread.
     * <p>
     * This method cannot revive the feature after {@link #terminate} has
     * been called.
     * </p>
     **/
    public void revive() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }
}