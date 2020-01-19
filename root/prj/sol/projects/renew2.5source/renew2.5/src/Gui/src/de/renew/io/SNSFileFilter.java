/*
 * Created on Apr 13, 2003
 */
package de.renew.io;

import CH.ifa.draw.io.SimpleFileFilter;

import de.renew.util.StringUtil;

import java.io.File;


/**
 * @author Lawrence Cabac
 */
public class SNSFileFilter extends SimpleFileFilter {
    public SNSFileFilter() {
        this.setExtension("sns");
        this.setDescription("Renew Shadow Net System");
    }
}