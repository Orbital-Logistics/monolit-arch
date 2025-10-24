package org.orbitalLogistic.exceptions;

public class CargoManifestNotFoundException extends RuntimeException {
    public CargoManifestNotFoundException(String message) {
        super(message);
    }
}
