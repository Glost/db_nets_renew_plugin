package de.renew.gui.pnml.converter;

import org.w3c.dom.Element;


public abstract class NetConverterAbstract implements NetConverter {
    public String convertAttributNameToRenewName(Element attribute) {
        String result = "";
        String parent = attribute.getParentNode().getNodeName();
        String type = attribute.getNodeName();
        if (parent.equals("place")) {
            result = convertPlaceAttributNameToRenewName(type);
        } else if (parent.equals("transition")) {
            result = convertTransitionAttributNameToRenewName(type);
        } else if (parent.equals("arc")) {
            result = convertArcAttributNameToRenewName(type);
        }
        return result;
    }

    public String convertAttributValueToRenewValue(String value, String type,
                                                   String figure) {
        String result = "";
        if (figure.equals("place")) {
            //NOTICEsignature
            result = convertPlaceAttributValueToRenewValue(value, type);
        } else if (figure.equals("transition")) {
            //NOTICEsignature
            result = convertTransitionAttributValueToRenewValue(value, type);
        } else if (figure.equals("arc")) {
            result = convertArcAttributValueToRenewValue(value, type);
        }
        return result;
    }

    public String convertRenewNameToAttributeName(String type, String figure) {
        String result = type;
        if (figure.equals("place")) {
            result = type;
        } else if (figure.equals("transition")) {
            result = type;
        } else if (figure.equals("arc")) {
            result = "type";
        }
        return result;
    }

    public String convertRenewValueToAttributeValue(String value, String type,
                                                    String figure) {
        String result = value;
        if (figure.equals("place")) {
            result = value;
        } else if (figure.equals("transition")) {
            result = value;
        } else if (figure.equals("arc")) {
            result = value;
        }
        return result;
    }

    public String convertPlaceAttributNameToRenewName(String name) {
        String result = name;
        return result;
    }

    public String convertTransitionAttributNameToRenewName(String name) {
        String result = name;
        return result;
    }

    public String convertArcAttributNameToRenewName(String name) {
        String result = name;
        if (name.equals("type")) {
            result = "ArcType";
        }
        return result;
    }

    //NOTICEsignature
    public String convertPlaceAttributValueToRenewValue(String value,
                                                        String attribut) {
        String result = value;
        return result;
    }

    //NOTICEsignature
    public String convertTransitionAttributValueToRenewValue(String value,
                                                             String attribut) {
        String result = value;
        return result;
    }

    public String convertArcAttributValueToRenewValue(String value,
                                                      String attribut) {
        String result = value;
        if (attribut.equals("ArcType")) {
            result = value;
        }
        return result;
    }

    /**
     * Is parser net parser.
     * @param netType [String]
     * @return false
     *
     * @author Eva Mueller
     * @date Dec 3, 2010
     * @version 0.1
     */
    public static boolean isNetParser(String netType) {
        return false;
    }

    public boolean isAttribute(Element label) {
        boolean result = false;
        String tagName = label.getNodeName();
        result = isAttribute(tagName);
        return result;
    }
}