package se.kth.csc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TextLimitExceededException extends Exception {
    public TextLimitExceededException() {
    }

    public TextLimitExceededException(String message) {
        super(message);
    }

    public TextLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public TextLimitExceededException(Throwable cause) {
        super(cause);
    }

    public TextLimitExceededException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
