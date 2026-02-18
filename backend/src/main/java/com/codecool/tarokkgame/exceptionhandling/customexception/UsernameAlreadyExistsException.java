package com.codecool.tarokkgame.exceptionhandling.customexception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Username " + username + " already exists");
    }
}
