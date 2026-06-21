package com.codecool.tarokkgame.exceptionhandling.customexception;

public class FeedbackDeliveryException extends RuntimeException {
    public FeedbackDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
