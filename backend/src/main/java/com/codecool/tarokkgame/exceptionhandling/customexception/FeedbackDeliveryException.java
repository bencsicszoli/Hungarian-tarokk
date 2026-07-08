package com.codecool.tarokkgame.exceptionhandling.customexception;

import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;

public class FeedbackDeliveryException extends RuntimeException {

    private final LocalizedMessage localizedMessage;

    public FeedbackDeliveryException(String message, Throwable cause) {
        super(message, cause);
        this.localizedMessage = new LocalizedMessage(MessageKey.ERROR_FEEDBACK_DELIVERY);
    }

    public LocalizedMessage getLocalizedErrorMessage() {
        return localizedMessage;
    }
}
