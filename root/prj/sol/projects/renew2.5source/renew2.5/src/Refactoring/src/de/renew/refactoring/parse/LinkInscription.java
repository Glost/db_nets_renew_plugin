package de.renew.refactoring.parse;

import de.renew.expression.Expression;

import de.renew.net.UplinkInscription;
import de.renew.net.inscription.DownlinkInscription;


/**
 * Abstraction for {@link UplinkInscription} and {@link DownlinkInscription}
 * that responds to their common fields.
 *
 * @author 2mfriedr
 */
public class LinkInscription {
    private UplinkInscription _uplinkInscription;
    private DownlinkInscription _downlinkInscription;

    public LinkInscription(Object inscription) throws IllegalArgumentException {
        if (inscription instanceof UplinkInscription) {
            _uplinkInscription = (UplinkInscription) inscription;
        } else if (inscription instanceof DownlinkInscription) {
            _downlinkInscription = (DownlinkInscription) inscription;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Object getObject() {
        return isUplink() ? _uplinkInscription : _downlinkInscription;
    }

    public String getName() {
        return isUplink() ? _uplinkInscription.name : _downlinkInscription.name;
    }

    public int getBeginLine() {
        return isUplink() ? _uplinkInscription.uplinkBeginLine
                          : _downlinkInscription.downlinkBeginLine;
    }

    public int getBeginColumn() {
        return isUplink() ? _uplinkInscription.uplinkBeginColumn
                          : _downlinkInscription.downlinkBeginColumn;
    }

    public int getEndLine() {
        return isUplink() ? _uplinkInscription.uplinkEndLine
                          : _downlinkInscription.downlinkEndLine;
    }

    public int getEndColumn() {
        return isUplink() ? _uplinkInscription.uplinkEndColumn
                          : _downlinkInscription.downlinkEndColumn;
    }

    public int getNameBeginLine() {
        return isUplink() ? _uplinkInscription.nameBeginLine
                          : _downlinkInscription.nameBeginLine;
    }

    public int getNameBeginColumn() {
        return isUplink() ? _uplinkInscription.nameBeginColumn
                          : _downlinkInscription.nameBeginColumn;
    }

    public int getNameEndLine() {
        return isUplink() ? _uplinkInscription.nameEndLine
                          : _downlinkInscription.nameEndLine;
    }

    public int getNameEndColumn() {
        return isUplink() ? _uplinkInscription.nameEndColumn
                          : _downlinkInscription.nameEndColumn;
    }

    public Expression getParams() {
        return isUplink() ? _uplinkInscription.params
                          : _downlinkInscription.params;
    }

    private boolean isUplink() {
        return _uplinkInscription != null;
    }
}