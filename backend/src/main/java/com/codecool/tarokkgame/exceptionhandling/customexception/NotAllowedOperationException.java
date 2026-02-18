package com.codecool.tarokkgame.exceptionhandling.customexception;

public class NotAllowedOperationException extends RuntimeException {
    public NotAllowedOperationException(String message) {
        super(message);
    }
}
