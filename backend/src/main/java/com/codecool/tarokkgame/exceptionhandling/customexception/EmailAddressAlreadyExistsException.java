package com.codecool.tarokkgame.exceptionhandling.customexception;

public class EmailAddressAlreadyExistsException extends RuntimeException {
    public EmailAddressAlreadyExistsException(String email) {
        super("Email address already exists: " + email);
    }
}
