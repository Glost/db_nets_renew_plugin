/**
 *
 */
package de.renew.ant;

import org.gnu.jfiglet.FIGDriver;
import org.gnu.jfiglet.core.FIGFont;
import org.gnu.jfiglet.core.FIGure;
import org.gnu.jfiglet.core.IllegalFIGFontFileException;
import org.gnu.jfiglet.utils.FIGUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.zip.ZipException;


/**
 * A FigletText writes some text with figlet
 * @see http://www.figlet.org/
 * @see http://sourceforge.net/projects/jfiglet/
 * @author daniel
 * @since Apr 20, 2010
 */
public class FigletText /*extends Task*/ {
    private static final String _lineBreak = System.getProperty("line.separator");
    private String _text = "";
    private FIGDriver _converter = new FIGDriver();
    private enum Fonts {standard,
        small,
        straight,
        nipples;
    }
    private FIGFont _font = new FIGFont(Fonts.small.toString());
    {
        try {
            _font.loadFromFile();
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalFIGFontFileException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FigletText figletText = new FigletText();
        if (args.length > 0) {
            figletText.setText(args[0]);
        } else {
            figletText.setText("please give a text as parameter");
        }
        figletText.execute();
    }

    /**
     * prints the text as FIGlet
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() /*throws BuildException*/ {
        try {
            FIGure figText = _converter.getBannerFIGure(_text, _font);
            FIGUtils.renderFIGureToOutputStream(figText, System.out, _lineBreak);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * the content of the attribute 'text'
     * @param text
     */
    public void setText(String text) {
        _text = text;
    }

    /**
     * the text between the start and end tag
     * @param text
     */
    public void addText(String text) {
        _text += text;
    }
}