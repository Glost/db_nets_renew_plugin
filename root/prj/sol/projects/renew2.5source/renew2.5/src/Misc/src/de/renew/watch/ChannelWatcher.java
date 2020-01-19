package de.renew.watch;

import java.util.Iterator;


public interface ChannelWatcher {
    public void bindingsCalculated(Iterator<Object> iter);
}