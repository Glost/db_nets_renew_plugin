package de.renew.dbnets.shadow.parser;

import de.renew.dbnets.shadow.ParsedDBNetDeclarationNode;
import de.renew.formalism.java.InscriptionParser;
import de.renew.formalism.java.ParseException;

public interface DBNetInscriptionParser extends InscriptionParser {

    @Override
    ParsedDBNetDeclarationNode DeclarationNode() throws ParseException;
}
