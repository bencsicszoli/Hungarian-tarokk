package com.codecool.tarokkgame.exceptionhandling.customexception;

import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.Map;

public class EmailAddressAlreadyExistsException extends RuntimeException {

    private final LocalizedMessage localizedMessage;

    public EmailAddressAlreadyExistsException(String email) {
        super("Email address already exists: " + email);
        this.localizedMessage = new LocalizedMessage(MessageKey.ERROR_EMAIL_EXISTS, Map.of("email", email));
    }

    public LocalizedMessage getLocalizedErrorMessage() {
        return localizedMessage;
    }
}
