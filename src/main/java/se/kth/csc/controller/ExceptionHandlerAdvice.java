package se.kth.csc.controller;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * General error handler for the application.
 */
@ControllerAdvice
class ExceptionHandlerAdvice {
    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = NotFoundException.class)
    @ResponseBody
    public String notFound(Exception exception, WebRequest request, HttpServletResponse response) {
        response.setStatus(404); // Not found
        return String.format("%s: %s", request.getDescription(true), exception.getMessage());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    @ResponseBody
    public String dataIntegrityViolation(Exception exception, WebRequest request, HttpServletResponse response) {
        response.setStatus(409); // Conflict
        return String.format("%s: %s", request.getDescription(true), exception.getMessage());
    }
}
