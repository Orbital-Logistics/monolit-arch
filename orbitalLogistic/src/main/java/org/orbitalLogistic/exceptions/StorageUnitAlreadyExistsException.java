package org.orbitalLogistic.exceptions;

public class StorageUnitAlreadyExistsException extends RuntimeException {
    public StorageUnitAlreadyExistsException(String message) {
        super(message);
    }
}