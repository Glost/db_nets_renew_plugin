package de.renew.formalism.fs;

import de.renew.shadow.ShadowInscribable;
import de.renew.shadow.ShadowNet;

import java.util.List;


public class ShadowConcept extends ShadowInscribable {
    private String namespace;
    private String name;
    private String approp;
    private String conceptDocumentation;
    private List<String> slotDocumentation;

    public ShadowConcept(ShadowNet net, String name, String approp,
                         String conceptDocumentation,
                         List<String> slotDocumentation) {
        super(net);
        int end = name.indexOf("::");
        if (end == -1) {
            namespace = net.getName();
            this.name = name;
        } else {
            namespace = name.substring(0, end);
            this.name = name.substring(end + 2);
        }
        this.approp = approp;
        this.conceptDocumentation = conceptDocumentation;
        this.slotDocumentation = slotDocumentation;
    }

    //  public void setName(String name) {
    //    this.name=name;
    //  }
    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFullName() {
        return namespace + "::" + name;
    }

    public void setApprop(String approp) {
        this.approp = approp;
    }

    public String getApprop() {
        return approp;
    }

    public void discard() {
        // is there anything else to do here?
    }

    public String getConceptDocumentation() {
        return conceptDocumentation;
    }

    public List<String> getSlotDocumentation() {
        return slotDocumentation;
    }
}