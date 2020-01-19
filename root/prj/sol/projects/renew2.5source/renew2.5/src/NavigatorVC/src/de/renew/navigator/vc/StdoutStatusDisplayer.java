package de.renew.navigator.vc;

import CH.ifa.draw.io.StatusDisplayer;


/**
 * @author Konstantin Simon Maria Möllers
 * @version 0.1
 */
public class StdoutStatusDisplayer implements StatusDisplayer {
    @Override
    public void showStatus(String message) {
        System.out.println(message);
    }
}