package de.renew.appleui;



/**
 * This interface hides gui details from the AppleUI plugin.
 *
 * @author Michael Duvigneau
 **/
interface AboutDisplayer {

    /**
     * Displays a modal about box.
     **/
    public void displayAboutBox();

    /**
     * Brings the menu frame to front.
     **/
    public void bringMenuFrameToFront();
}