package org.orbitalLogistic.exceptions;

public class CargoNotFoundException extends RuntimeException {
    public CargoNotFoundException(String message) {
        super(message);
    }
}