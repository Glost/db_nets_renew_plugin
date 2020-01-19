package de.renew.database.entitylayer;

import java.sql.SQLException;


public class PrimaryKeyNotUniqueException extends SQLException {
    public PrimaryKeyNotUniqueException() {
        super();
    }

    public PrimaryKeyNotUniqueException(String message) {
        super(message);
    }
}