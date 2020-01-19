package dbnets;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginProperties;

import java.net.URL;

public class DBNets extends PluginAdapter {

    public DBNets(URL location) throws PluginException {
        super(location);
    }

    public DBNets(PluginProperties props) {
        super(props);
    }

    @Override
    public void init() {
        logger.info("Initializing DB-Nets Plugin..."); // TODO: remove this line, it is only for the debugging purposes.
        super.init();
    }
}
