package org.orbitalLogistic.exceptions;

public class SpacecraftAlreadyExistsException extends RuntimeException {
    public SpacecraftAlreadyExistsException(String message) {
        super(message);
    }
}