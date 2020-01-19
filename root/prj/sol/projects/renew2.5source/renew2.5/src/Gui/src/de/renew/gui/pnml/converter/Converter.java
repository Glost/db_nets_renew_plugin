package de.renew.gui.pnml.converter;

import de.renew.formalism.FormalismPlugin;

import de.renew.gui.CPNDrawing;
import de.renew.gui.ModeReplacement;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetSystem;


public class Converter {
    private String _netType;
    private ShadowNet net;
    private static Converter _instance;

    protected Converter() {
    }

    public String getType() {
        return _netType;
    }

    public void setType(String type) {
        _netType = type;
    }

    public ShadowNet getShadowNet() {
        return net;
    }

    public void setShadowNet(CPNDrawing drawing) {
        if (PTNetConverter.isNetParser(getType())) {
            FormalismPlugin.getCurrent().setCompiler(FormalismPlugin.PT_COMPILER);
        } else if (RefNetConverter.isNetParser(getType())) {
            FormalismPlugin.getCurrent()
                           .setCompiler(FormalismPlugin.JAVA_COMPILER);
        }
        net = drawing.buildShadow(new ShadowNetSystem(ModeReplacement.getInstance()
                                                                     .getDefaultCompilerFactory()));

    }

    public NetConverter getNetConverter() {
        NetConverter result = new DefaultNetConverter();
        if (getType() != null) {
            if (PTNetConverter.isNetParser(getType())) {
                result = new PTNetConverter();
            } else if (RefNetConverter.isNetParser(getType())) {
                result = new RefNetConverter();
            }
        }
        return result;
    }

    public static Converter instance() {
        if (_instance == null) {
            _instance = new Converter();
        }
        return _instance;
    }
}