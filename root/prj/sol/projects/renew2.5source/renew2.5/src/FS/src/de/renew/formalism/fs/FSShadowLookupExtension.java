package de.renew.formalism.fs;

import de.uni_hamburg.fs.ConceptImpl;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Partition;
import de.uni_hamburg.fs.TypeException;

import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowLookupExtension;
import de.renew.shadow.ShadowLookupExtensionFactory;
import de.renew.shadow.ShadowNetElement;

import java.util.Enumeration;
import java.util.Hashtable;


public class FSShadowLookupExtension implements ShadowLookupExtension {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FSShadowLookupExtension.class);

    // the factory producing this kind of extension
    private static final Factory _factory = new Factory();

    public static FSShadowLookupExtension lookup(ShadowLookup lookup) {
        return (FSShadowLookupExtension) lookup.getShadowLookupExtension(_factory);
    }

    /** Mapping from ShadowConcept to ConceptImpl. */
    private Hashtable<String, ConceptImpl> conceptMap = new Hashtable<String, ConceptImpl>();

    /** Mapping from ConceptImpl.Feature to ShadowConcept or ShadowAssoc. */
    private Hashtable<String, ShadowNetElement> appropMap = new Hashtable<String, ShadowNetElement>();

    /** Mapping from ConceptImpls to Partition of Subconcepts. */
    private Hashtable<ConceptImpl, Partition> partitionMap = new Hashtable<ConceptImpl, Partition>();

    public void setConcept(ShadowConcept shadowConcept, ConceptImpl concept) {
        conceptMap.put(shadowConcept.getName(), concept);
    }

    public ConceptImpl getConcept(ShadowConcept shadowConcept) {
        return conceptMap.get(shadowConcept.getName());
    }

    /*
    public ShadowConcept getShadowConcept(ConceptImpl concept) {
      // use inverse relation:
      Enumeration enum=conceptMap.keys();
      while (enumeration.hasMoreElements()) {
    ShadowConcept key=(ShadowConcept)enumeration.nextElement();
    if (conceptMap.get(key)==concept) {
            return key;
    }
      }
      throw new NoSuchElementException();
    }
    */
    public Enumeration<String> allConcepts() {
        return conceptMap.keys(); // an enumeration of Concept Namess
    }

    public void setApprop(ConceptImpl concept, Name feature,
                          ShadowNetElement elem) {
        appropMap.put(concept.getName() + "." + feature, elem);
        //logger.debug("Adding Attribute "+feature+" to concept "+concept.getName());
    }

    public ShadowNetElement getApprop(ConceptImpl concept, Name feature) {
        return appropMap.get(concept.getName() + "." + feature);
    }

    public void addToSubPartition(ConceptImpl supi, ConceptImpl sub)
            throws TypeException {
        Partition suppart = partitionMap.get(supi);
        if (suppart == null) {
            logger.debug("Creating new sub-partition for " + supi.getName());
            logger.debug("Adding " + sub.getName() + " to sub-partition of "
                         + supi.getName());
            suppart = new Partition(sub);
            partitionMap.put(supi, suppart);
        } else {
            logger.debug("Adding " + sub.getName() + " to sub-partition of "
                         + supi.getName());
            suppart.addConcept(sub);
        }
    }

    public static class Factory implements ShadowLookupExtensionFactory {
        public String getCategory() {
            return "de.renew.formalism.fs";
        }

        public ShadowLookupExtension createExtension() {
            return new FSShadowLookupExtension();
        }
    }
}