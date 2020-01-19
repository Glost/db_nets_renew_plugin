package CH.ifa.draw.application;

import de.renew.util.StringUtil;

import java.io.File;
import java.io.FilenameFilter;


/**
 * FileByExtensionFilter.java
 *
 *
 * Created: Wed Apr 19  2000
 * Modified: Mon May 15  2000
 *
 * @author Michael Duvigneau
 * @author Frank Wienberg
 */
public class FileByExtensionFilter implements FilenameFilter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FileByExtensionFilter.class);
    private String[] extensions;

    public FileByExtensionFilter(String[] extensions) {
        logger.debug("created" + extensions.length);
        if (extensions.length == 0) {
            extensions = null;
        }
        this.extensions = extensions;
    }

    public boolean accept(File dir, String name) {
        if (extensions == null) {
            return true;
        } else {
            String ext = StringUtil.getExtension(name);
            logger.debug("ext:" + ext);
            for (int i = 0; i < extensions.length; i++) {
                logger.debug("???:" + extensions[i]);
                if (extensions[i].equals(name)) {
                    logger.debug("yes");
                    return true;
                }
            }
            logger.debug("no");
            return false;
        }
    }
}