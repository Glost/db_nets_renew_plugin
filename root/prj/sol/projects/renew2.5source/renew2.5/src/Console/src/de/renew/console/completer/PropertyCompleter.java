/**
 *
 */
package de.renew.console.completer;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import de.renew.plugin.command.GetPropertyCommand;

import java.util.List;


/**
 * @author cabac
 *
 */
public class PropertyCompleter implements Completer {
    public PropertyCompleter() {
    }

    /* (non-Javadoc)
     * @see jline.console.completer.Completer#complete(java.lang.String, int, java.util.List)
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        return new StringsCompleter(GetPropertyCommand.getPropertyNames())
                   .complete(buffer, cursor, candidates);
    }
}