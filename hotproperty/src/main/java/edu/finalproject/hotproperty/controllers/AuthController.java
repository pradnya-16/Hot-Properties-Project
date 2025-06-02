package edu.finalproject.hotproperty.controllers;


import edu.finalproject.hotproperty.dtos.LoginRequestDto;
import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.entities.enums.RoleType;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.services.AuthService;
import edu.finalproject.hotproperty.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model,
                            Authentication authentication) {
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        if (error != null) {
            if ("unauthorized".equals(error)) {
                model.addAttribute("errorMessage", "You must be logged in to access that page.");
            } else {
                model.addAttribute("errorMessage", "Invalid email or password.");
            }
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        model.addAttribute("loginRequestDto", new LoginRequestDto());
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "auth/register";
    }

    //Forgot - no DTO's on request, only responses. Need to refactor.
    @PostMapping("/login")
    public String loginUser(@ModelAttribute("loginRequestDto") LoginRequestDto loginRequestDto,
                            BindingResult bindingResult,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Login form has errors.");
            return "auth/login";
        }
        try {
            Cookie jwtCookie = authService.loginAndCreateJwtCookie(loginRequestDto);
            response.addCookie(jwtCookie);
            
            log.info("User {} logged in successfully");
            return "redirect:/dashboard";
        } catch (BadCredentialsException e) {
            log.warn("Login attempt failed for email: {}", loginRequestDto.getEmail());
            model.addAttribute("errorMessage", "Invalid email or password.");
            return "auth/login";
        }
    }
//Forgot - no DTO's on request, only responses. Need to refactor.
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userRegistrationDto") UserRegistrationDto registrationDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Registration form has errors.");
            return "auth/register";
        }
        try {
            userService.registerBuyer(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            log.info("New buyer registered: {}, redirecting to login.", registrationDto.getEmail());
            return "redirect:/login";
        } catch (InvalidUserParameterException e) {
            log.warn("Registration failed for email {}: {}", registrationDto.getEmail(), e.getMessage());
            model.addAttribute("userRegistrationDto", registrationDto); 
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    

    

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        authService.clearJwtCookie(response);
        return "redirect:/login?logout";
    }
}

