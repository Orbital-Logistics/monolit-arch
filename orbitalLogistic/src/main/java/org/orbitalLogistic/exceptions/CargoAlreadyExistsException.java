package org.orbitalLogistic.exceptions;

public class CargoAlreadyExistsException extends RuntimeException {
    public CargoAlreadyExistsException(String message) {
        super(message);
    }
}
