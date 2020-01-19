package de.renew.formalism.fs;

import de.uni_hamburg.fs.OrderedTable;

import de.renew.formalism.java.InscriptionParser;


public interface FSInscriptionParser extends InscriptionParser {
    OrderedTable parseAppropDef() throws de.renew.formalism.java.ParseException;
}