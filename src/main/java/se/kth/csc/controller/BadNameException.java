package se.kth.csc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BadNameException extends Exception {
    public BadNameException() {
    }

    public BadNameException(String message) {
        super(message);
    }

    public BadNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadNameException(Throwable cause) {
        super(cause);
    }

    public BadNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
