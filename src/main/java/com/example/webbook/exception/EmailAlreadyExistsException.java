package com.example.webbook.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("This email address is already registered. Please use a different email.");
    }
}
