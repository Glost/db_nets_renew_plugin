package de.renew.formalism.fs;

import de.uni_hamburg.fs.Name;

import de.renew.shadow.ShadowInscribable;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNetElement;


public class ShadowAssoc extends ShadowInscribable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ShadowAssoc.class);
    public static final int ZERO_OR_ONE = 0;
    public static final int ZERO_OR_MORE = 1;
    public static final int ONE_OR_MORE = 2;
    private ShadowConcept concept;
    private ShadowConcept type;

    public ShadowAssoc(ShadowConcept concept, ShadowConcept type) {
        super(concept.getNet());
        this.concept = concept;
        this.type = type;
    }

    public ShadowConcept getConcept() {
        return concept;
    }

    public ShadowConcept getType() {
        return type;
    }

    public int getMultiplicity() {
        java.util.Iterator<ShadowNetElement> iterator = elements().iterator();
        while (iterator.hasNext()) {
            String inscr = ((ShadowInscription) iterator.next()).inscr.trim();
            if ("*".equals(inscr) || "0..*".equals(inscr)) {
                return ZERO_OR_MORE;
            } else if ("1..*".equals(inscr)) {
                return ONE_OR_MORE;
            }
        }
        return ZERO_OR_ONE;
    }

    public Name getFeature() {
        java.util.Iterator<ShadowNetElement> iterator = elements().iterator();
        String feature = null;
        boolean multi = false;
        while (iterator.hasNext() && feature == null) {
            String inscr = ((ShadowInscription) iterator.next()).inscr.trim();
            if (inscr.indexOf("*") >= 0) {
                multi = true;
            } else if (inscr.indexOf("..") < 0) {
                feature = inscr;
            }
        }
        if (feature == null) {
            feature = type.getName();
            int pos = feature.indexOf("::");
            if (pos >= 0) {
                feature = feature.substring(pos + 2);
            }
            feature = feature.substring(0, 1).toLowerCase()
                      + feature.substring(1);
            if (multi) {
                feature += "s";
            }
            logger.debug("Concept " + concept.getName()
                         + " gets default feature name " + feature);
        }
        return new Name(feature);
    }

    public void discard() {
        // is there anything else to do here?
    }
}