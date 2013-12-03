package se.kth.csc.controller;

import com.google.common.base.Throwables;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * General error handler for the application.
 */
@ControllerAdvice
class ExceptionHandlerAdvice {

    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = Exception.class)
    public ModelAndView exception(Exception exception, WebRequest request) {
        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.addObject("errorMessage", Throwables.getRootCause(exception));
        return modelAndView;
    }
}
