package de.renew.appleui;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.AppReOpenedEvent;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.PrintFilesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.AppReOpenedListener;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.PrintFilesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

import de.renew.plugin.PluginManager;
import de.renew.plugin.command.CLCommand;

import java.io.File;

import java.util.List;


/**
 * This listener actually does the work which is promised by the
 * {@link AppleUI} plugin. It handles Apple-specific application
 * events.
 * <p>
 * </p>
 * Created: Thu Jul  8  2004
 *
 * @author Michael Duvigneau
 **/
class AppleUIListener implements AboutHandler, PreferencesHandler,
                                 AppReOpenedListener, OpenFilesHandler,
                                 PrintFilesHandler, QuitHandler {
    private AboutDisplayer displayer = null;

    /**
     * Configures an <code>AboutDisplayer</code>.
     *
     * @param displayer  the about box displayer.
     **/
    void setAboutDisplayer(AboutDisplayer displayer) {
        this.displayer = displayer;
    }

    /**
     * Tells the plugin system to shut down when Command-Q has been pressed (or
     * a similar event occurs).
     **/
    @Override
    public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
        PluginManager.getInstance().stop();
        // Tell MacOS that handling the request may take some
        // time...
    }

    @Override
    public void printFiles(PrintFilesEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void openFiles(OpenFilesEvent openFilesEvent) {

        /**
         * Tries to open the file by issuing a <code>"gui"</code> command to the
         * plugin system. However, this command will only be available if the
         * "Renew Gui" plugin is loaded.
         **/
        CLCommand guiCommand = PluginManager.getInstance().getCLCommands()
                                            .get("gui");
        if (guiCommand != null) {
            List<File> files = openFilesEvent.getFiles();
            String[] filenames = new String[files.size()];
            int i = 0;
            for (File file : files) {
                filenames[i++] = file.getPath();
            }
            guiCommand.execute(filenames, System.out);
        }
    }

    /**
     * Tries to bring the main menu frame to front.
     * However, this command will
     * only be available if the "Renew Gui" plugin is loaded.
     **/
    @Override
    public void appReOpened(AppReOpenedEvent arg0) {
        displayer.bringMenuFrameToFront();
    }

    @Override
    public void handlePreferences(PreferencesEvent arg0) {
        // TODO Auto-generated method stub
    }

    /**
     * Opens the editor's about box if the "Renew Gui" plugin is loaded.
     **/
    @Override
    public void handleAbout(AboutEvent arg0) {
        displayer.displayAboutBox();
    }
}