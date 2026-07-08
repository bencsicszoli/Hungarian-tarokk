package com.codecool.tarokkgame.exceptionhandling.customexception;

import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.Map;

public class UsernameAlreadyExistsException extends RuntimeException {

    private final LocalizedMessage localizedMessage;

    public UsernameAlreadyExistsException(String username) {
        super(String.format("Username %s already exists", username));
        this.localizedMessage = new LocalizedMessage(MessageKey.ERROR_USERNAME_EXISTS, Map.of("username", username));
    }

    public LocalizedMessage getLocalizedErrorMessage() {
        return localizedMessage;
    }
}
