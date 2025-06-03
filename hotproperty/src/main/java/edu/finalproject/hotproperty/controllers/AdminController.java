package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users/admin")
public class AdminController {

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
  public String createAgentForm() {
    return "admin/agent_form";
  }
}
