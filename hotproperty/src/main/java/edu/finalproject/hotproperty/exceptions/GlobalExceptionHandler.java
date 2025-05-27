package edu.finalproject.hotproperty.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidUserParameterException.class,
            InvalidMessageParameterException.class,
            InvalidFavoriteParameterException.class,
            InvalidPropertyParameterException.class,
            InvalidPropertyImageParameterException.class})
    public String handleValidation(Exception ex, HttpServletRequest request, Model model) {

        // Servlet error attributes (if present)
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

        // Request details
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String httpMethod = request.getMethod();
        String userAgent = request.getHeader("User-Agent");

        // Exception details
        String exceptionType = ex.getClass().getSimpleName();
        String exceptionMessage = ex.getMessage();

        // Populate model attributes for Thymeleaf
        model.addAttribute("status", statusCode != null ? statusCode : 500);
        model.addAttribute("error", errorMessage != null ? errorMessage : "Unexpected error");
        model.addAttribute("path", requestUri != null ? requestUri : path);
        model.addAttribute("query", queryString);
        model.addAttribute("method", httpMethod);
        model.addAttribute("userAgent", userAgent);
        model.addAttribute("exceptionType", exceptionType);
        model.addAttribute("exceptionMessage", exceptionMessage);

        return "error"; // return your Thymeleaf or JSP view
    }


    @ExceptionHandler(Exception.class)
    public String handleAnyException(Exception ex, HttpServletRequest request, Model model) {
        // Servlet error attributes (if present)
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

        // Request details
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String httpMethod = request.getMethod();
        String userAgent = request.getHeader("User-Agent");

        // Exception details
        String exceptionType = ex.getClass().getSimpleName();
        String exceptionMessage = ex.getMessage();

        // Populate model attributes for Thymeleaf
        model.addAttribute("status", statusCode != null ? statusCode : 500);
        model.addAttribute("error", errorMessage != null ? errorMessage : "Unexpected error");
        model.addAttribute("path", requestUri != null ? requestUri : path);
        model.addAttribute("query", queryString);
        model.addAttribute("method", httpMethod);
        model.addAttribute("userAgent", userAgent);
        model.addAttribute("exceptionType", exceptionType);
        model.addAttribute("exceptionMessage", exceptionMessage);

        return "error"; // renders error.html
    }
}

