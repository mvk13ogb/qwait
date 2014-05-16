package se.kth.csc.controller;

/*
 * #%L
 * QWait
 * %%
 * Copyright (C) 2013 - 2014 KTH School of Computer Science and Communication
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
