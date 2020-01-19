package de.renew.console.completer;

import de.renew.console.completer.RenewArgumentCompleter.AbstractArgumentDelimiter;


public class FirstWhitespaceArgumentDelimiter extends AbstractArgumentDelimiter {
    @Override
    public boolean isDelimiterChar(final CharSequence buffer, final int pos) {
        boolean result = Character.isWhitespace(buffer.charAt(pos));
        if (result) {
            for (int i = 0; i < pos; i++) {
                if (Character.isWhitespace(buffer.charAt(i))) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}