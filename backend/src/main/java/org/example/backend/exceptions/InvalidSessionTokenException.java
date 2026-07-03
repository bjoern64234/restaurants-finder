package org.example.backend.exceptions;

public class InvalidSessionTokenException extends RuntimeException {
    public InvalidSessionTokenException(String message) {
        super(message);
    }
}
