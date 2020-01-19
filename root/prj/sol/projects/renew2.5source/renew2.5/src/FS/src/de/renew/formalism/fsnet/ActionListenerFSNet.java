package de.renew.formalism.fsnet;

import de.renew.net.NetInstance;

import java.awt.Button;
import java.awt.List;
import java.awt.MenuItem;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Class needed for an BDI example using FSNets by Frank Wienberg (in his dissertation models directory).
 */
public class ActionListenerFSNet extends EventListenerFSNet
        implements ActionListener {
    public ActionListenerFSNet() {
    }

    public ActionListenerFSNet(NetInstance instance) {
        super(instance);
    }

    public ActionListenerFSNet(NetInstance instance, Object component) {
        this(instance);
        setComponent(component);
    }

    public void actionPerformed(ActionEvent event) {
        eventOccured(event);
    }

    public void setComponent(Object comp) {
        if (myComponent != null) {
            // remove old listener:
            if (myComponent instanceof Button) {
                ((Button) myComponent).removeActionListener(this);
            } else if (myComponent instanceof List) {
                ((List) myComponent).removeActionListener(this);
            } else if (myComponent instanceof MenuItem) {
                ((MenuItem) myComponent).removeActionListener(this);
            } else if (myComponent instanceof TextField) {
                ((TextField) myComponent).removeActionListener(this);
            }
        }
        myComponent = comp;
        if (comp instanceof Button) {
            ((Button) comp).addActionListener(this);
        } else if (comp instanceof List) {
            ((List) comp).addActionListener(this);
        } else if (comp instanceof MenuItem) {
            ((MenuItem) comp).addActionListener(this);
        } else if (comp instanceof TextField) {
            ((TextField) comp).addActionListener(this);
        }
    }
}