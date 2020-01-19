package de.renew.gui.fs;

import de.renew.formalism.fs.ShadowAssoc;
import de.renew.formalism.fs.ShadowConcept;

import de.renew.gui.AssocArrowTip;
import de.renew.gui.InscribableFigure;

import de.renew.shadow.ShadowNetElement;


public class AssocConnection extends ConceptConnection
        implements InscribableFigure {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(AssocConnection.class);

    public AssocConnection() {
        super(new AssocArrowTip());
    }

    protected ShadowNetElement createShadow(ShadowConcept from, ShadowConcept to) {
        // logger.debug("assoc shadow created!");
        return new ShadowAssoc(from, to);
    }
}