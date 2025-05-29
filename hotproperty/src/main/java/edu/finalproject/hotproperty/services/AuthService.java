package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.dtos.LoginRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

public interface AuthService {
    Cookie loginAndCreateJwtCookie(LoginRequestDto loginRequestDto) throws BadCredentialsException;
    void clearJwtCookie(HttpServletResponse response);
    String getLoggedInUserEmail();
}
