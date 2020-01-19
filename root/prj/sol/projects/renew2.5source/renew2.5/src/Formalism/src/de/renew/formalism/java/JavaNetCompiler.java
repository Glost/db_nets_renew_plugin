package de.renew.formalism.java;

import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;


public class JavaNetCompiler implements ShadowCompilerFactory {
    static final long serialVersionUID = -3422169407566489409L;
    private boolean allowDangerousArcs = false;
    private boolean allowTimeInscriptions = false;
    private boolean wantEarlyTokens = false;

    public JavaNetCompiler() {
        this(false, false, false);
    }

    public JavaNetCompiler(boolean allowDangerousArcs,
                           boolean allowTimeInscriptions,
                           boolean wantEarlyTokens) {
        this.allowDangerousArcs = allowDangerousArcs;
        this.allowTimeInscriptions = allowTimeInscriptions;
        this.wantEarlyTokens = wantEarlyTokens;
    }

    public ShadowCompiler createCompiler() {
        return new SingleJavaNetCompiler(allowDangerousArcs,
                                         allowTimeInscriptions, wantEarlyTokens);
    }

    /*
    protected void channelCheck(ShadowNetSystem netSystem)
        throws SyntaxException {
    UpdatableSet spontaneousChannels = new HashedSet();
    Hashtable channelMap = new Hashtable();

    CollectionEnumeration nets = netSystem.elements();
    while (nets.hasMoreElements()) {
        ShadowNet shadowNet = (ShadowNet) nets.nextElement();

        ParsedDeclarationNode declarations = makeDeclarationNode(shadowNet);

        CollectionEnumeration elements = shadowNet.elements();
        while (elements.hasMoreElements()) {
            Object element = elements.nextElement();
            if (element instanceof ShadowTransition) {
                ShadowTransition transition = (ShadowTransition) element;

                ChannelInscription uplink = null;
                UpdatableSet downlinks = new HashedSet();

                CollectionEnumeration inscriptions = transition.elements();
                while (inscriptions.hasMoreElements()) {
                    Object inscription = inscriptions.nextElement();
                    if (inscription instanceof ShadowInscription) {
                        String text = ((ShadowInscription) inscription).inscr;
                        try {
                            InscriptionParser parser = makeParser(text);
                            parser.setDeclarationNode(declarations);

                            ChannelInscription ch = parser
                                                    .tryParseChannelInscription();
                            if (ch.isUplink) {
                                if (uplink != null) {
                                    throw new SyntaxException("Transition has more than one uplink.",
                                                              null)
                                          .addObject(transition);
                                }
                                uplink = ch;
                            } else {
                                downlinks.include(ch);
                            }
                        } catch (ParseException e) {
                            // This is expected. We parsed something other than
                            // a channel. We ignore this inscription.
                        }
                    }
                }


                // Reduce all channel inscriptions to downlinks and
                // spontaneous transitions to invoked transitions.
                if (uplink == null) {
                    uplink = new ChannelInscription(false, "", 0);
                } else {
                    uplink = new ChannelInscription(false, uplink.name,
                                                    uplink.arity);
                }
                ChannelCheckNode source = (ChannelCheckNode) channelMap.get(uplink);
                if (source == null) {
                    source = new ChannelCheckNode(uplink.name, uplink.arity);
                    channelMap.put(uplink, source);
                }

                CollectionEnumeration channels = downlinks.elements();
                if (channels.hasMoreElements()) {
                    do {
                        ChannelInscription channel = (ChannelInscription) channels
                                                     .nextElement();
                        ChannelCheckNode target = (ChannelCheckNode) channelMap
                                                  .get(channel);
                        if (target == null) {
                            target = new ChannelCheckNode(channel.name,
                                                          channel.arity);
                            channelMap.put(channel, target);
                        }

                        source.addInvokableChannel(target);
                    } while (channels.hasMoreElements());
                } else {
                    source.setSatisfiable();
                }
            }
        }
    }


    // Check all channels, even though they might not
    // be accessible.
    Enumeration enumeration = channelMap.elements();
    while (enumeration.hasMoreElements()) {
        ChannelCheckNode node = (ChannelCheckNode) enumeration.nextElement();
        node.check();
    }
    }
    */


    /*
    private void doubleNameCheck(ShadowNetSystem netSystem)
            throws SyntaxException {
        CollectionEnumeration nets = netSystem.elements();
        while (nets.hasMoreElements()) {
            ShadowNet shadowNet = (ShadowNet) nets.nextElement();

            Hashtable names = new Hashtable();
            CollectionEnumeration elements = shadowNet.elements();
            while (elements.hasMoreElements()) {
                Object element = elements.nextElement();
                if (element instanceof ShadowNode) {
                    ShadowNode node = (ShadowNode) element;
                    String name = node.getName();
                    if (name != null && !name.equals("")) {
                        if (names.containsKey(name)) {
                            throw new SyntaxException("Detected two net elements with the same name: "
                                                      + name + ".", null).addObject(node)
                                                                                                                          .addObject((ShadowNode) names
                                                                                                                                     .get(name));
                        } else {
                            names.put(name, node);
                        }
                    }
                }
            }
        }
    }

    private void isolatedNodeCheck(ShadowNetSystem netSystem)
            throws SyntaxException {
        CollectionEnumeration nets = netSystem.elements();
        while (nets.hasMoreElements()) {
            ShadowNet shadowNet = (ShadowNet) nets.nextElement();

            CollectionEnumeration elements = shadowNet.elements();
            while (elements.hasMoreElements()) {
                Object element = elements.nextElement();
                if (element instanceof ShadowNode) {
                    ShadowNode node = (ShadowNode) element;
                    CollectionEnumeration inscriptions = node.elements();
                    boolean arcFound = false;
                    while (inscriptions.hasMoreElements() && !arcFound) {
                        Object inscription = inscriptions.nextElement();
                        if (inscription instanceof ShadowArc) {
                            arcFound = true;
                        }
                    }
                    if (!arcFound) {
                        throw new SyntaxException("Detected isolated node: "
                                                  + node.getName() + ".", null)
                              .addObject(node);
                    }
                }
            }
        }
    }

    public String[] getLintNames() {
        return lintNames;
    }

    public void lint(int lintNr, ShadowNetSystem netSystem,
                     ShadowNet selectedNet, ShadowNet lastSelectedNet)
            throws SyntaxException {
        switch (lintNr) {
        case 0:
            doubleNameCheck(netSystem);
            break;
        case 1:
            isolatedNodeCheck(netSystem);
            break;
        }
    }
    */
}