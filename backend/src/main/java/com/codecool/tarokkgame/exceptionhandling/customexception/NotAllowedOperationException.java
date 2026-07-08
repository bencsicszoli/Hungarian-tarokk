package com.codecool.tarokkgame.exceptionhandling.customexception;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

public class NotAllowedOperationException extends RuntimeException {

    private final LocalizedMessage localizedMessage;

    public NotAllowedOperationException(LocalizedMessage localizedMessage) {
        super(localizedMessage.key());
        this.localizedMessage = localizedMessage;
    }

    public LocalizedMessage getLocalizedErrorMessage() {
        return localizedMessage;
    }
}
