package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.dtos.UserProfileUpdateDto;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.entities.enums.RoleType;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.services.AuthService;
import edu.finalproject.hotproperty.services.FavoriteService;
import edu.finalproject.hotproperty.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;
  private final AuthService authService;
  private final FavoriteService favoriteService;

  @Autowired
  public UserController(
      UserService userService, AuthService authService, FavoriteService favoriteService) {
    this.userService = userService;
    this.authService = authService;
    this.favoriteService = favoriteService;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/dashboard")
  public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User user = userService.getCurrentUser(userDetails);
    model.addAttribute("user", user);

    if (user.getRole() == RoleType.BUYER) {
      int favoriteCount = favoriteService.getFavoritesByBuyer(user).size();
      model.addAttribute("favoriteCount", favoriteCount);
    }
    return "/shared/dashboard";
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/profile")
  public String userProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User user = userService.getCurrentUser(userDetails);
    model.addAttribute("user", user);
    return "/shared/profile";
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/edit_profile")
  public String showEditProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User currentUser = userService.getCurrentUser(userDetails);

    UserProfileUpdateDto dto = new UserProfileUpdateDto();
    dto.setFirstName(currentUser.getFirstName());
    dto.setLastName(currentUser.getLastName());
    dto.setEmail(currentUser.getEmail());

    model.addAttribute("userProfileUpdateDto", dto);
    return "shared/edit_profile";
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/edit_profile")
  public String processEditProfileForm(
      @ModelAttribute("userProfileUpdateDto") UserProfileUpdateDto userProfileUpdateDto,
      BindingResult bindingResult,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes,
      HttpServletResponse httpServletResponse,
      Model model) {
    if (bindingResult.hasErrors()) {
      log.warn("Binding errors in edit profile form for user: {}", userDetails.getUsername());
      model.addAttribute("errorMessage", "Please correct the errors below.");
      return "shared/edit_profile";
    }

    String currentEmail = userDetails.getUsername();
    try {
      boolean emailChanged = userService.updateUserProfile(currentEmail, userProfileUpdateDto);
      if (emailChanged) {
        authService.clearJwtCookie(httpServletResponse);
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Profile updated successfully. Your email was changed, please log in again with your"
                + " new email.");
        log.info(
            "User {} changed email to {}. Redirecting to login.",
            currentEmail,
            userProfileUpdateDto.getEmail());
        return "redirect:/login";
      } else {
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        log.info(
            "User {} updated profile (email not changed). Redirecting to profile.", currentEmail);
        return "redirect:/profile";
      }
    } catch (InvalidUserParameterException e) {
      log.warn("Profile update failed for {}: {}", currentEmail, e.getMessage());
      model.addAttribute("errorMessage", e.getMessage());
      model.addAttribute("userProfileUpdateDto", userProfileUpdateDto);
      return "shared/edit_profile";
    } catch (Exception e) {
      log.error(
          "Unexpected error during profile update for {}: {}", currentEmail, e.getMessage(), e);
      model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
      model.addAttribute("userProfileUpdateDto", userProfileUpdateDto);
      return "shared/edit_profile";
    }
  }
}
