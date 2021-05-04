package de.renew.dbnets.shadow.parser;

import de.renew.dbnets.shadow.ParsedDBNetDeclarationNode;
import de.renew.formalism.java.InscriptionParser;
import de.renew.formalism.java.ParseException;

/**
 * The interface for the db-net's inscription parser.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
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
