package se.kth.csc.controller;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * General error handler for the application.
 */
@ControllerAdvice
class ExceptionHandlerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = NotFoundException.class)
    public String notFound(Exception exception, WebRequest request, HttpServletResponse response) {
        log.error("Not found: {}", request.getDescription(true), exception);
        response.setStatus(404);

        return "error/not-found";
    }
}
