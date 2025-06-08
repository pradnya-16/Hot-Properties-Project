package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/users/admin")
public class AdminController {

  private static final Logger log = LoggerFactory.getLogger(AdminController.class);
  private final AdminService adminService;

  @Autowired
  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public String listUsers(Model model) {
    List<User> users = adminService.getAllUsers();
    model.addAttribute("users", users);
    return "admin/user-list";
  }

  @PostMapping("/delete/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public String deleteUser(@PathVariable Long userId) {
    adminService.deleteUser(userId);
    return "redirect:/users/admin";
  }

  @GetMapping("/create-agent")
  @PreAuthorize("hasRole('ADMIN')")
  public String createAgentForm(Model model) {
    if (!model.containsAttribute("userRegistrationDto")) {
      model.addAttribute("userRegistrationDto", new UserRegistrationDto());
    }
    return "admin/agent_form";
  }

  @PostMapping("/create-agent")
  @PreAuthorize("hasRole('ADMIN')")
  public String processCreateAgentForm(@ModelAttribute("userRegistrationDto") UserRegistrationDto userRegistrationDto,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("userRegistrationDto", userRegistrationDto);
      redirectAttributes.addFlashAttribute("errorMessage", "Invalid form data. Please check your input.");
      log.warn("Binding errors occurred while admin creating agent: {}", bindingResult.getAllErrors());
      return "redirect:/users/admin/create-agent";
    }
    try {
      adminService.createAgent(userRegistrationDto);
      redirectAttributes.addFlashAttribute("successMessage", "Agent account created successfully!");
      log.info("Admin created new agent: {}", userRegistrationDto.getEmail());
      return "redirect:/users/admin";
    } catch (InvalidUserParameterException e) {
      log.warn("Failed to create agent {}: {}", userRegistrationDto.getEmail(), e.getMessage());
      redirectAttributes.addFlashAttribute("userRegistrationDto", userRegistrationDto);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/users/admin/create-agent";
    } catch (Exception e) {
      log.error("Unexpected error while creating agent {}: {}", userRegistrationDto.getEmail(), e.getMessage(), e);
      redirectAttributes.addFlashAttribute("userRegistrationDto", userRegistrationDto);
      redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
      return "redirect:/users/admin/create-agent";
    }
  }
}
