package org.orbitalLogistic.exceptions;

public class SpacecraftNotFoundException extends RuntimeException {
    public SpacecraftNotFoundException(String message) {
        super(message);
    }
}
