package edu.finalproject.hotproperty.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({ InvalidUserParameterException.class,
      InvalidMessageParameterException.class,
      InvalidFavoriteParameterException.class,
      InvalidPropertyParameterException.class,
      InvalidPropertyImageParameterException.class })
  public String handleValidation(Exception ex, HttpServletRequest request, Model model) {

    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
    String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

    String path = request.getRequestURI();
    String queryString = request.getQueryString();
    String httpMethod = request.getMethod();
    String userAgent = request.getHeader("User-Agent");

    String exceptionType = ex.getClass().getSimpleName();
    String exceptionMessage = ex.getMessage();

    model.addAttribute("status", statusCode != null ? statusCode : 500);
    model.addAttribute("error", errorMessage != null ? errorMessage : "Unexpected error");
    model.addAttribute("path", requestUri != null ? requestUri : path);
    model.addAttribute("query", queryString);
    model.addAttribute("method", httpMethod);
    model.addAttribute("userAgent", userAgent);
    model.addAttribute("exceptionType", exceptionType);
    model.addAttribute("exceptionMessage", exceptionMessage);

    return "error";
  }

  @ExceptionHandler(Exception.class)
  public String handleAnyException(Exception ex, HttpServletRequest request, Model model) {
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
    String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

    String path = request.getRequestURI();
    String queryString = request.getQueryString();
    String httpMethod = request.getMethod();
    String userAgent = request.getHeader("User-Agent");

    String exceptionType = ex.getClass().getSimpleName();
    String exceptionMessage = ex.getMessage();

    model.addAttribute("status", statusCode != null ? statusCode : 500);
    model.addAttribute("error", errorMessage != null ? errorMessage : "Unexpected error");
    model.addAttribute("path", requestUri != null ? requestUri : path);
    model.addAttribute("query", queryString);
    model.addAttribute("method", httpMethod);
    model.addAttribute("userAgent", userAgent);
    model.addAttribute("exceptionType", exceptionType);
    model.addAttribute("exceptionMessage", exceptionMessage);
    return "error";
  }
}
