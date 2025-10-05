package org.orbitalLogistic.exceptions;

public class CargoCategoryNotFoundException extends RuntimeException {
    public CargoCategoryNotFoundException(String message) {
        super(message);
    }
}
