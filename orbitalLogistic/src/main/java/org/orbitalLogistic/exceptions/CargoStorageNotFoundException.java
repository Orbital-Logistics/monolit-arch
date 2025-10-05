package org.orbitalLogistic.exceptions;

public class CargoStorageNotFoundException extends RuntimeException {
    public CargoStorageNotFoundException(String message) {
        super(message);
    }
}
