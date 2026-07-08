package com.codecool.tarokkgame.exceptionhandling.customexception;

import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.model.dto.ErrorMessage;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage usernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        return ErrorMessage.from(e.getLocalizedErrorMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage emailAddressAlreadyExistsException(EmailAddressAlreadyExistsException e) {
        return ErrorMessage.from(e.getLocalizedErrorMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage notAllowedOperationException(NotAllowedOperationException e) {
        return ErrorMessage.from(e.getLocalizedErrorMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage noSuchElementException(NoSuchElementException e) {
        return ErrorMessage.from(new LocalizedMessage(MessageKey.ERROR_NOT_FOUND));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleBadCredentialsException(BadCredentialsException ex) {
        return ErrorMessage.from(new LocalizedMessage(MessageKey.ERROR_WRONG_CREDENTIALS));
    }

    @ExceptionHandler(FeedbackDeliveryException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorMessage handleFeedbackDeliveryException(FeedbackDeliveryException e) {
        return ErrorMessage.from(e.getLocalizedErrorMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleGenericException(Exception e) {
        return ErrorMessage.from(new LocalizedMessage(MessageKey.ERROR_GENERIC));
    }
}
