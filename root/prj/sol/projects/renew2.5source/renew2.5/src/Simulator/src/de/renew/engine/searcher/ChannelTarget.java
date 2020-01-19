package de.renew.engine.searcher;

import java.util.Collection;


public interface ChannelTarget {
    public Collection<UplinkProvider> getUplinkProviders(String channel);
}