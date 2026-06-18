package com.codecool.tarokkgame.exceptionhandling.customexception;

/** Thrown when a feedback message could not be delivered (e.g. SMTP failure). */
public class FeedbackDeliveryException extends RuntimeException {
    public FeedbackDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
