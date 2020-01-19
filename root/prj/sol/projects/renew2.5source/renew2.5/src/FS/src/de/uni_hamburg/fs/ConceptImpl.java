package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedSet;
import collections.UpdatableSet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ConceptImpl implements Concept {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConceptImpl.class);
    private String name = null;
    private boolean newExtensional = false;
    private boolean extensional = false;
    private UpdatableSet subsumes;
    private UpdatableSet subsumedBy;
    protected ConceptSet directSubs = new ConceptSet();
    protected ConceptSet directSupers = new ConceptSet();
    private OrderedTable newApprop = new OrderedTable();
    private OrderedTable approp = new OrderedTable();
    private boolean isDummy = false;
    /**
     * Contains all {@link Partition}s to which this concept belongs.
     * This <code>Set</code> is maintained by the <code>Partition</code> class.
     **/
    Set<Object> partitions = new HashSet<Object>();

    ConceptImpl() {
        name = "";
        transitiveClosure();
    }

    public ConceptImpl(String name, ConceptImpl[] directSuperList)
            throws TypeException {
        this(name);
        for (int i = 0; i < directSuperList.length; ++i) {
            addIsa(directSuperList[i]);
        }
    }

    public ConceptImpl(String name, ConceptImpl superConcept)
            throws TypeException {
        this(name, new ConceptImpl[] { superConcept });
    }

    public ConceptImpl(String name, boolean extensional, boolean isDummy) {
        this.extensional = extensional;
        this.isDummy = isDummy;
        subsumes = new HashedSet();
        subsumes.include(this);
        if (!setName(name)) {
            throw new RuntimeException("Concept " + name + " already exists!");
        }
        TypeSystem.instance().transitiveClosure();
    }

    public ConceptImpl(String name, boolean extensional) {
        this(name, extensional, false);
    }

    public ConceptImpl(String name) {
        this(name, true);
    }

    /** Return the name of this Concept. */
    public String getName() {
        int end = name.indexOf("::");
        if (end == -1) {
            return name;
        }
        return name.substring(end + 2);
    }

    /** Return the name of the namespace this Concept belongs to. */
    public String getNamespace() {
        int end = name.indexOf("::");
        if (end == -1) {
            return "";
        }
        return name.substring(0, end);
    }

    /** Return the full name of this Concept in the form
     * namespace::name.
     */
    public String getFullName() {
        return name;
    }

    /** Change the name of this Concept.
      * <p>A concept must have a unique name! If the new name already
      * exists, nothing is changed.
      */
    public boolean setName(String newname) {
        if (newname == null || newname.length() == 0) {
            return true;
        }
        if (newname.equals(name)) {
            return true;
        }
        TypeSystem ts = TypeSystem.instance();
        if (ts.hasConcept(newname)) {
            logger.error("Concept " + newname + " already exists!");
            return false;
        }
        if (name != null) {
            ts.removeConcept(this);
            logger.debug("Renaming concept " + name + " to " + newname);
        } else {
            logger.debug("Adding concept " + newname + " to  type system.");
        }
        name = newname;
        ts.addConcept(this);
        return true;
    }

    public boolean isDummy() {
        return isDummy;
    }

    /** Return whether this Concept is extensional. */
    public boolean isExtensional() {
        return extensional;
    }

    /** Return whether the feature <feature> is appropriate in this Concept.
     */
    public boolean isApprop(Name feature) {
        if (approp == null) {
            logger.debug("Approp is null in " + name + " (" + hashCode()
                         + ") !!!");
        }
        return approp.includesKey(feature);
    }

    public ParsedType appropParsedType(Name featureName)
            throws NoSuchFeatureException {
        return (ParsedType) approp.at(featureName);
    }

    /** Return the required Type of the Value found under the given feature. */
    public Type appropType(Name featureName) throws NoSuchFeatureException {
        try {
            return appropParsedType(featureName).asType();
        } catch (UnificationFailure uff) {
            throw new RuntimeException("The ConjunctiveType "
                                       + appropParsedType(featureName)
                                       + " is not defined!");
        }
    }

    /** Return an Enumeration of all appropriate features. */
    public CollectionEnumeration appropFeatureNames() {
        return approp.keys();
    }

    void inherit(UpdatableSet visited) throws UnificationFailure {
        visited.include(this);
        ConceptEnumeration subenumeration = directSubs.elements();
        while (subenumeration.hasMoreElements()) {
            ConceptImpl sub = (ConceptImpl) subenumeration.nextConcept();
            if (extensional) {
                if (!sub.extensional) {
                    sub.extensional = true;
                    sub.subsumedBy = new HashedSet();
                    sub.subsumedBy.include(sub);
                }
                sub.subsumedBy.include(this);
            }
            boolean subchanged = false;
            int subcount = 0;
            CollectionEnumeration appropenumeration = approp.keys();
            while (appropenumeration.hasMoreElements()) {
                Name featureName = (Name) appropenumeration.nextElement();
                ParsedType appropType = appropParsedType(featureName);

                // logger.debug("Concept: "+name+" feature: "+featureName);
                if (sub.isApprop(featureName)) {
                    logger.debug("Overwriting feature in subconcept "
                                 + sub.getName());
                    ParsedType oldsubapprop = (ParsedType) sub.approp.at(featureName);
                    ParsedType subapprop = oldsubapprop.unite(appropType);

                    //sub.approp.insertAt(featureName,subcount++,appropType);
                    if (!subchanged && !oldsubapprop.equals(subapprop)) {
                        subchanged = true;
                    }
                } else {
                    sub.approp.insertAt(featureName, subcount++, appropType);
                    subchanged = true;
                }
            }
            if (subchanged || !visited.includes(sub)) {
                sub.inherit(visited);
            }
        }
    }

    protected void transitiveClosure() {
        subsumes = new HashedSet();
        subsumes.include(this);
        ConceptEnumeration subenumeration = directSubs.elements();
        while (subenumeration.hasMoreElements()) {
            ConceptImpl sub = (ConceptImpl) subenumeration.nextConcept();
            sub.transitiveClosure();
            subsumes.includeElements(sub.subsumes.elements());
        }
    }

    void recalcDirectIsa(UpdatableSet visited) throws TypeException {
        if (!visited.includes(this)) {
            visited.include(this);
            try {
                directSupers.join();
            } catch (UnificationFailure uff) {
                TypeException tex = new TypeException(this);
                throw tex;
            }
            directSubs.meet();

            extensional = newExtensional;
            if (extensional) {
                subsumedBy = new HashedSet();
                subsumedBy.include(this);
            } else {
                subsumedBy = null;
            }
            approp = (OrderedTable) newApprop.duplicate();

            CollectionEnumeration subenumeration = directSubs.elements();
            while (subenumeration.hasMoreElements()) {
                ((ConceptImpl) subenumeration.nextElement()).recalcDirectIsa(visited);
            }
        }
    }

    void basicAddIsa(ConceptImpl superconcept) {
        directSupers.addConcept(superconcept);
        superconcept.directSubs.addConcept(this);
        logger.debug("Adding " + this + " isa " + superconcept);
    }

    public void addIsa(ConceptImpl superconcept)
            throws CyclicHierarchyException, TypeException {
        if (superconcept.isa(this)) {
            throw new CyclicHierarchyException();
        }
        if (isa(superconcept)) {
            return;
        }
        basicAddIsa(superconcept);
        TypeSystem.instance().recalcDirectIsa();
    }

    public void addApprop(String feature, ParsedType type) {
        addApprop(new Name(feature), type);
    }

    public void addApprop(Name featureName, ParsedType type) {
        logger.debug("Feature " + featureName + " defined in " + getName());
        newApprop.putAt(featureName, type);
    }

    public void setApprops(OrderedTable approps) {
        newApprop = (OrderedTable) approps.duplicate();
    }

    void buildFeatureTypes(UpdatableSet visited) throws TypeException {
        if (!visited.includes(this)) {
            visited.include(this);
            logger.debug("Converting feature types for concept "
                         + toDetailedString());
            CollectionEnumeration featenumeration = approp.keys();
            while (featenumeration.hasMoreElements()) {
                Name feature = (Name) featenumeration.nextElement();

                logger.debug("Converting feature " + feature);
                ParsedType pt = (ParsedType) approp.at(feature);
                try {
                    pt.asType();
                } catch (UnificationFailure uff) {
                    throw new TypeException(this, feature);
                }
            }
            CollectionEnumeration subenumeration = directSubs.elements();
            while (subenumeration.hasMoreElements()) {
                ((ConceptImpl) subenumeration.nextElement()).buildFeatureTypes(visited);
            }
        }
    }

    /** Return whether this Concept is-a <that> Concept.
      * In other words, return wheter this Concept is more special than <that>
      * Concept.
      */
    public boolean isa(Concept that) {
        if (that instanceof ConceptImpl) {
            return ((ConceptImpl) that).subsumes.includes(this);
        } else {
            return false;
        }
    }

    /** Return the Partition this ConceptImpl belongs to. */
    public Iterator<Object> getPartitions() {
        return partitions.iterator();
    }

    /** Return whether this Concept is-not-a <that> Concept.
      * In other words, return wheter this Concept is incompatible with <that>
      * Concept.
      */
    public boolean isNotA(Concept that) {
        if (this == that) {
            return false;
        }
        if (that instanceof ConceptImpl) {
            ConceptImpl thatCon = (ConceptImpl) that;
            if (intersects(partitions, thatCon.partitions)) {
                return true;
            }
            return superIsNotA(thatCon) || thatCon.superIsNotA(this);


            // TODO: this isNotA that is also true if some appropType
            // of this is incompatible with the corresponding appropType
            // of that. Caution: recursive definition!!!
        }
        return true;
    }

    private boolean intersects(Set<?> a, Set<?> b) {
        if (a.size() > b.size()) {
            return intersects(b, a);
        }
        for (Iterator<?> as = a.iterator(); as.hasNext();) {
            if (b.contains(as.next())) {
                return true;
            }
        }
        return false;
    }

    private boolean superIsNotA(ConceptImpl that) {
        ConceptEnumeration superenumeration = directSupers.elements();
        while (superenumeration.hasMoreElements()) {
            Concept superCon = superenumeration.nextConcept();
            if (superCon.isNotA(that)) {
                return true;
            }
        }
        return false;
    }

    public ConceptEnumeration extensionalSuperconcepts() {
        return new ConceptEnumeration(subsumedBy.elements());
    }

    public String toString() {
        return name.toString();
    }

    public String toDetailedString() {
        StringBuffer output = new StringBuffer(name);
        if (extensional) {
            output.append(" (extensional)");
        }
        if (approp == null) {
            logger.warn("Approp is null in " + name + " (" + hashCode()
                        + ") !!!");
        } else {
            CollectionEnumeration appropenum = approp.keys();
            while (appropenum.hasMoreElements()) {
                Name featureName = (Name) appropenum.nextElement();
                Object appropTypeGuess = approp.at(featureName);
                if (appropTypeGuess instanceof Type) {
                    Type appropType = (Type) appropTypeGuess;
                    output.append("\n ").append(featureName.toString())
                          .append(":").append(appropType.getName());
                } else {
                    output.append("\n ").append(featureName.toString())
                          .append(":").append(appropTypeGuess.toString());
                }
            }
        }
        output.append("\nDirect Supers: ").append(directSupers.toString());
        output.append("\nDirect Subs: ").append(directSubs.toString());
        return output.toString();
    }
}