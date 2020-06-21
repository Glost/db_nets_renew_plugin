package de.renew.dbnets.shadow.parser;

import de.renew.dbnets.shadow.ParsedDBNetDeclarationNode;
import de.renew.formalism.java.InscriptionParser;
import de.renew.formalism.java.ParseException;

/**
 * The interface for the db-net's inscription parser.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public interface DBNetInscriptionParser extends InscriptionParser {

    /**
     * Parses the db-net's declaration node.
     *
     * @return The parsed db-net's declaration node.
     * @throws ParseException If there are syntax and other errors during the parsing has occurred.
     */
    @Override
    ParsedDBNetDeclarationNode DeclarationNode() throws ParseException;
}
