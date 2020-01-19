/*
 * Created on Dec 27, 2004
 *
 */
package de.renew.shadow;

import de.renew.net.loading.Finder;

import de.renew.util.ClassSource;
import de.renew.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import java.net.URL;

import java.util.Iterator;


/**
 * This class is responsible for finding compiled
 * shadow nets in either classpath relative (see
 * {@link #findNetClasspathRel(String, StringBuffer)})
 * or file sources (see {@link #findNetFile(String, StringBuffer)}).
 *
 * <p>
 * <!-- copied from old DefaultShadowNetLoader -->
 * Additionally, the shadow net system stored in the file may
 * contain only one shadow net, which must have the same name as
 * the net and the file, too. This restriction exists to avoid
 * unexpected results from loading shadow nets out of files with
 * different names.
 * </p>
 * @author Till Kothe
 *
 */
public class SNSFinder extends Finder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SNSFinder.class);


    /**
     * An array of file name extensions that indicate serialized
     * <code>ShadowNetSystem</code> files.
     **/
    private final String[] shadowExtensions = new String[] { ".sns", ".sha" };


    /* (non-Javadoc)
     * @see de.renew.net.loading.Finder#findNetFile(java.lang.String)
     */
    public ShadowNetSystem findNetFile(String name, String path) {
        int ext = 0; // counter for Shadow net extensions
        String fullFileName;
        boolean error = true;
        URL url = null;

        while (error && (ext < shadowExtensions.length)) {
            // First step: Check whether the
            // source file exists and create URL for it.
            error = false;

            fullFileName = path + shadowExtensions[ext];
            logger.debug("SNSFinder: looking for: " + fullFileName);
            try {
                File file = new File(fullFileName);
                if (file.canRead()) {
                    url = file.toURI().toURL();
                } else {
                    error = true;
                }
            } catch (Exception e) {
                logger.error("SNSFinder: " + fullFileName + " caused " + e);
                logger.debug(e.getMessage(), e);
                error = true;
            }


            // Second step: If we have an URL to an existing
            // source file, read, load and return it.
            if (!error) {
                try {
                    ShadowNetSystem netSystem = loadNetSystemFromURL(url, name);

                    if (netSystem != null) {
                        return netSystem;
                    }
                } catch (Exception e) {
                    logger.error("SNSFinder: " + url + " caused " + e);
                    logger.debug(e.getMessage(), e);
                    error = true;
                }
            }
            ext++;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see de.renew.net.loading.Finder#findNetClasspathRel(java.lang.String)
     */
    public ShadowNetSystem findNetClasspathRel(String name, String path) {
        int ext = 0;
        String fullFileName;
        boolean error = true;

        while (error && (ext < shadowExtensions.length)) {
            // First step: Generate URL from netpath, net
            // name and extension. Also check whether the
            // source file exists.
            error = false;

            fullFileName = path + shadowExtensions[ext];
            logger.debug("SNSFinder: looking for: " + fullFileName);
            URL url = ClassSource.getClassLoader()
                                 .getResource(StringUtil.convertToSlashes(fullFileName));
            error = (url == null);


            // Second step: If we have an URL to an existing
            // source file, read, load and return it.
            if (!error) {
                ShadowNetSystem netSystem;
                try {
                    netSystem = loadNetSystemFromURL(url, name);
                    if (netSystem != null) {
                        return netSystem;
                    }
                } catch (Exception e) {
                    logger.error("SNSFinder: " + url + " caused " + e);
                    logger.debug(e.getMessage(), e);
                    error = true;
                }
            }
            ext++;
        }

        return null;
    }

    /**
     * Load a net with the given name from the given URL.
     * @param url   the url from where to load the net system.
     * @param name  the name of the net to load. It is used to verify wether
     *              the net system really contains the net that is requested.
     * @return  the loaded <code>ShadowNetSystem</code> if successful or else
     *          <code>null</code>.
     * @throws IOException if an I/O error occurs
     **/
    private ShadowNetSystem loadNetSystemFromURL(URL url, String name)
            throws IOException, ClassNotFoundException {
        logger.debug("Loading shadow net from " + url);
        InputStream stream = url.openStream();
        ObjectInput input = new ObjectInputStream(stream);
        ShadowNetSystem netSystem = (ShadowNetSystem) input.readObject();
        input.close();

        Iterator<ShadowNet> nets = netSystem.elements().iterator();
        ShadowNet net;
        if (nets.hasNext()) {
            net = nets.next();
            if (nets.hasNext()) {
                logger.error("SNSFinder: " + url
                             + " contains more than one shadow net. Ignored.");
            } else if (net.getName().equals(name)) {
                // looks good. let's return the net system.
                logger.debug("SNSFinder: found net " + name + " at " + url);
                return netSystem;
            }
        }
        logger.warn("SNSFinder: " + url + " was empty!? Ignored.");

        return null;
    }
}