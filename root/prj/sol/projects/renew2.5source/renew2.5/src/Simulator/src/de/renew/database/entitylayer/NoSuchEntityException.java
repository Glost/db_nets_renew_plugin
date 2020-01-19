package de.renew.database.entitylayer;

import java.sql.SQLException;


public class NoSuchEntityException extends SQLException {
    public NoSuchEntityException() {
        super();
    }

    public NoSuchEntityException(String message) {
        super(message);
    }
}