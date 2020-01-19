package de.renew.shadow;

import de.renew.net.Net;
import de.renew.net.NetNotFoundException;
import de.renew.net.loading.NetLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ShadowNetSystem implements java.io.Serializable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ShadowNetSystem.class);
    static final long serialVersionUID = 4619730346099695519L;
    private Set<ShadowNet> nets = new HashSet<ShadowNet>();
    private ShadowCompilerFactory defaultCompilerFactory = null;
    private transient Set<ShadowNet> compiledNets = new HashSet<ShadowNet>();
    private transient Set<ShadowNet> uncompiledNets = new HashSet<ShadowNet>();
    private transient ShadowNetLoader netLoader = null;

    public ShadowNetSystem(ShadowCompilerFactory defaultCompilerFactory) {
        setDefaultCompilerFactory(defaultCompilerFactory);
    }

    public ShadowNetSystem(ShadowCompilerFactory defaultCompilerFactory,
                           ShadowNetLoader netLoader) {
        this(defaultCompilerFactory);
        setNetLoader(netLoader);
    }

    public void setDefaultCompilerFactory(ShadowCompilerFactory defaultCompilerFactory) {
        this.defaultCompilerFactory = defaultCompilerFactory;

        // invalidate previously compiled nets
        uncompiledNets.addAll(compiledNets);
        compiledNets.clear();
    }

    public ShadowCompilerFactory getDefaultCompilerFactory() {
        return defaultCompilerFactory;
    }

    /**
     * Sets a shadow net loader to be asked when a net is missing
     * during compilation.
     **/
    public void setNetLoader(ShadowNetLoader netLoader) {
        this.netLoader = netLoader;
    }

    public ShadowNetLoader getNetLoader() {
        return netLoader;
    }

    // Add and remove nets. These methods should be called
    // indirectly via ShadowNet() and ShadowNet.discard(), so that
    // inconsistencies in the data structure cannot arise.
    protected void add(ShadowNet net) {
        nets.add(net);
        if (!compiledNets.contains(net)) {
            uncompiledNets.add(net);
        }
    }

    protected void remove(ShadowNet net) {
        nets.remove(net);
        compiledNets.remove(net);
        uncompiledNets.remove(net);
    }

    public void markAsCompiled(ShadowNet net) {
        if (nets.contains(net)) {
            compiledNets.add(net);
            uncompiledNets.remove(net);
        }
    }

    /**
     * Returns an enumeration of all nets in the system.
     * The enumeration will refuse to return more elements if the
     * shadow net system is modified.
     **/
    public Set<ShadowNet> elements() {
        return Collections.unmodifiableSet(nets);
    }

    /**
     * Returns an enumeration of all nets in the system which are
     * not marked as compiled yet. The enumeration is independent
     * from future modifications of the shadow net system.
     **/
    private Set<ShadowNet> uncompiledElements() {
        return new HashSet<ShadowNet>(uncompiledNets);
    }

    /**
     * Tells whether there are uncompiled nets in the system.
     **/
    private boolean hasUncompiledElements() {
        return !uncompiledNets.isEmpty();
    }

    public synchronized ShadowLookup compile() throws SyntaxException {
        logger.debug(this + " is compiled with default compiler factory "
                     + defaultCompilerFactory);
        ShadowLookup result = compile(new ShadowLookup(), true);
        return result;
    }

    public synchronized ShadowLookup compileMore() throws SyntaxException {
        ShadowLookup lookup = new ShadowLookup();


        Iterator<Net> known = Net.allKnownNets();
        while (known.hasNext()) {
            Net net = known.next();
            lookup.setNet(net.getName(), net);
        }
        return compile(lookup, false);
    }

    /**
     * Compile the yet uncompiled nets of a shadow net system.
     **/
    private ShadowLookup compile(ShadowLookup lookup,
                                 boolean mayRunPreprocessors)
            throws SyntaxException {
        createNets(lookup);

        preprocessNets(lookup, mayRunPreprocessors);

        boolean preprocessorsHaveRun = mayRunPreprocessors;
        while (hasUncompiledElements()) {
            Iterator<ShadowNet> iterator = uncompiledElements().iterator();
            while (iterator.hasNext()) {
                ShadowNet shadowNet = iterator.next();
                ShadowCompiler compiler = shadowNet
                                              .createInititalizedShadowNetCompiler(lookup);
                ShadowPreprocessor[] preprocessors = compiler
                                                         .getRequiredPreprocessors();
                if (preprocessors != null && preprocessors.length > 0
                            && !preprocessorsHaveRun) {
                    // A net that was loaded after the start of compilation
                    // requires a preprocessing phase. Too bad, we do not
                    // support this.
                    throw new SyntaxException("Net " + shadowNet.getName()
                                              + " was dynamically loaded and requires a preprocessing phase. that is not supported",
                                              new String[0]);
                }
                logger.debug("compiling " + shadowNet.getName() + " using "
                             + compiler);
                compiler.compile(shadowNet);
                // Mark the net as compiled.
                markAsCompiled(shadowNet);
            }
            preprocessorsHaveRun = false;


            // Repeat, if the compiler net loader has appended nets
            // to the net system.
        }

        return lookup;
    }

    /**
     * Creates empty nets for all uncompiled shadow nets of the
     * shadow net system. Does not mark the nets as compiled.
     **/
    private void createNets(ShadowLookup lookup) throws SyntaxException {
        // Create new nets for every shadow net.
        // Check the uniqueness of the names.
        Iterator<ShadowNet> iterator = uncompiledElements().iterator();
        while (iterator.hasNext()) {
            ShadowNet shadowNet = iterator.next();
            String netName = shadowNet.getName();
            if (Net.isKnownNet(netName) || (lookup.getNet(netName) != null)) {
                throw new SyntaxException("Detected two nets with the same name: "
                                          + shadowNet.getName() + ".");
            }
            Net net = shadowNet.createInititalizedShadowNetCompiler(lookup)
                               .createNet(shadowNet.getName());
            lookup.setNet(shadowNet.getName(), net);
        }
    }

    private void preprocessNets(ShadowLookup lookup, boolean mayRunPreprocessors)
            throws SyntaxException {
        // Collect all preprocessors.
        Set<ShadowPreprocessor> preprocessors = new HashSet<ShadowPreprocessor>();
        Set<String> preprocessorNets = new HashSet<String>();
        Iterator<ShadowNet> iterator = uncompiledElements().iterator();
        while (iterator.hasNext()) {
            ShadowNet shadowNet = iterator.next();
            ShadowCompiler compiler = shadowNet
                                          .createInititalizedShadowNetCompiler(lookup);
            ShadowPreprocessor[] prepArray = compiler.getRequiredPreprocessors();
            if (prepArray != null && prepArray.length > 0) { //NOTICEredundant da war vorher ein &
                preprocessors.addAll(Arrays.asList(prepArray));
                preprocessorNets.add(shadowNet.getName());
            }
        }

        // Check whether preprocessors are permitted.
        if (preprocessors.isEmpty()) {
            return;
        }
        if (!mayRunPreprocessors) {
            StringBuffer buf = new StringBuffer();
            if (preprocessorNets.size() == 1) {
                buf.append("The dynamically loaded net ");
                buf.append(preprocessorNets.iterator().next());
                buf.append(" requires");
            } else {
                buf.append("Some dynamically loaded nets (");
                for (Iterator<String> i = preprocessorNets.iterator();
                             i.hasNext();) {
                    buf.append(i.next());
                    if (i.hasNext()) {
                        buf.append(", ");
                    }
                }
                buf.append(" require");
            }
            buf.append(" a preprocessing phase. This is not allowed.");
            throw new SyntaxException(buf.toString(), new String[0]);
        }


        // Run all preprocessors.
        Iterator<ShadowPreprocessor> preprocessorIter = preprocessors.iterator();
        while (preprocessorIter.hasNext()) {
            ShadowPreprocessor preprocessor = preprocessorIter.next();
            preprocessor.setShadowLookup(lookup);
            preprocessor.preprocess(this);
        }
    }

    /**
     * Asks the shadow net loader to provide a net for the given
     * name and creates an empty net, if there was a shadow net.
     **/
    private Net appendNet(ShadowLookup lookup, String name)
            throws NetNotFoundException {
        if (getNetLoader() != null) {
            ShadowNet shadowNet = getNetLoader().loadShadowNet(name, this);
            if (!shadowNet.getName().equals(name)) {
                throw new NetNotFoundException(name
                                               + " (ShadowNetLoader provided "
                                               + shadowNet.getName()
                                               + " instead)");
            }
            Net net;
            try {
                net = shadowNet.createInititalizedShadowNetCompiler(lookup)
                               .createNet(shadowNet.getName());
            } catch (SyntaxException e) {
                throw new NetNotFoundException(e);
            }
            lookup.setNet(shadowNet.getName(), net);
            return net;
        } else {
            throw new NetNotFoundException(name);
        }
    }

    private class LoopbackNetLoader implements NetLoader {
        private ShadowLookup lookup;

        LoopbackNetLoader(ShadowLookup lookup) {
            this.lookup = lookup;
        }

        public Net loadNet(String netName) throws NetNotFoundException {
            return appendNet(lookup, netName);
        }
    }

    LoopbackNetLoader createNetLoader(ShadowLookup lookup) {
        return new LoopbackNetLoader(lookup);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method.
     * <p>
     * Reinitializes the transient fields:
     * <ul>
     * <li>  All nets are marked as uncompiled.
     * </li>
     * <li>  No net loader is known.
     * </li>
     * </ul></p>
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        uncompiledNets = new HashSet<ShadowNet>(nets);
        compiledNets = new HashSet<ShadowNet>();
    }
}