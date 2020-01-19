package de.renew.watch;

import java.util.Iterator;


public class PrintWatcher implements ChannelWatcher {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PrintWatcher.class);

    public PrintWatcher() {
    }

    public void bindingsCalculated(Iterator<Object> iterator) {
        logger.debug("==================== Bindings calculated =====================");

        while (iterator.hasNext()) {
            logger.debug(iterator.next());
        }

        logger.debug("==================== Bindings listed =========================");
    }
}