package se.kth.csc.controller;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * General error handler for the application.
 */
@ControllerAdvice
class ExceptionHandlerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = Exception.class)
    public ModelAndView exception(Exception exception, WebRequest request) {
        ModelAndView modelAndView = new ModelAndView("error/general");
        Throwable rootCause = Throwables.getRootCause(exception);
        modelAndView.addObject("errorMessage", rootCause.getClass().getName() + (rootCause.getMessage() != null ? ": " + rootCause.getMessage() : ""));
        modelAndView.addObject("stackTrace", rootCause.getStackTrace());

        log.error("An exception was thrown while handling {}", request.getDescription(true), exception);

        return modelAndView;
    }

    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = NotFoundException.class)
    public String notFound(Exception exception, WebRequest request) {
        log.error("Not found: {}", request.getDescription(true), exception);

        return "error/not-found";
    }
}
