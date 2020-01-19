package de.renew.formalism.fsnet;

import de.renew.net.NetInstance;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * Class needed for an BDI example using FSNets by Frank Wienberg (in his dissertation models directory).
 */
public class ItemListenerFSNet extends EventListenerFSNet
        implements ItemListener {
    public ItemListenerFSNet() {
    }

    public ItemListenerFSNet(NetInstance instance) {
        super(instance);
    }

    public ItemListenerFSNet(NetInstance instance, ItemSelectable component) {
        this(instance);
        setComponent(component);
    }

    public void itemStateChanged(ItemEvent event) {
        eventOccured(event);
    }

    public void setComponent(Object comp) {
        if (myComponent instanceof ItemSelectable) {
            ((ItemSelectable) myComponent).removeItemListener(this);
        }
        myComponent = comp;
        if (myComponent instanceof ItemSelectable) {
            ((ItemSelectable) myComponent).addItemListener(this);
        }
    }
}