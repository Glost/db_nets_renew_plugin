package de.renew.formalism.fsnet;

import de.renew.net.NetInstance;

import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;


/**
 * Class needed for an BDI example using FSNets by Frank Wienberg (in his dissertation models directory).
 */
public class TextListenerFSNet extends EventListenerFSNet
        implements TextListener {
    public TextListenerFSNet() {
    }

    public TextListenerFSNet(NetInstance instance) {
        super(instance);
    }

    public TextListenerFSNet(NetInstance instance, TextComponent component) {
        this(instance);
        setComponent(component);
    }

    public void textValueChanged(TextEvent event) {
        eventOccured(event);
    }

    public void setComponent(Object comp) {
        if (myComponent instanceof TextComponent) {
            ((TextComponent) myComponent).removeTextListener(this);
        }
        myComponent = comp;
        if (myComponent instanceof TextComponent) {
            ((TextComponent) myComponent).addTextListener(this);
        }
    }
}