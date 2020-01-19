package de.renew.netcomponents;

import java.util.Vector;


/**
 * Class RemoveToolsControl
 * Controls the removing of the palettes which are selected by the user.
 *
 * @see RemoveToolsWindow
 * @author Lawrence Cabac
 *
 */
public class RemoveToolsControl {

    /**
     * The display for selecting ComponentsTools for removal.
     */
    private RemoveToolsWindow rtw;

    /**
     * The List of the loaded ComponentsTools.
     */
    private Vector<ComponentsTool> v;

    /**
     * The CompontentsToolPlugin
     *
     */
    private ComponentsToolPlugin ctp;

    /**
     * Method RemoveToolsControl.
     * @param ctp - the ComponentToolPlugin.
     */
    public RemoveToolsControl(ComponentsToolPlugin ctp) {
        this.ctp = ctp;
        this.v = ctp.getCTList();
        //        this.cpnapp = cpnapp;
        this.rtw = new RemoveToolsWindow(v, this);
    }

    /**
     * Method removeFromList.
     * Removes the palette from the menuFrame (Renew GUI) and from the selection window.
     * @param os - the selected ComponentTools to be removed.
     */
    public void removeFromList(Object[] os) {
        for (int i = 0; i < os.length; i++) {
            ComponentsTool ct = (ComponentsTool) os[i];
            ct.remove();
            v.remove(os[i]);

            // check if remove selection is default palette 
            if (ct == ctp.getDefaultCT()) {
                ctp.setDefaultCT(null);
            }
        }

        //        cpnapp.menuFrame().pack();
        if (v.isEmpty()) {
            rtw.dispose();
        } else {
            rtw.update(v);
        }
    }

    public void update(Vector<ComponentsTool> v) {
        this.v = v;
        rtw.update(v);
    }
}