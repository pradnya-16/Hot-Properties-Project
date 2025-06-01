//package edu.finalproject.hotproperty.controllers;
//
//import edu.finalproject.hotproperty.entities.User;
//import edu.finalproject.hotproperty.services.AdminService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin")
//public class AdminController {
//
//    private final AdminService adminService;
//
//    @Autowired
//    public AdminController(AdminService adminService) {
//        this.adminService = adminService;
//    }
//
//    @GetMapping("/dashboard")
//    public String dashboard() {
//        return "shared/dashboard";
//    }
//
//    @GetMapping("/users")
//    public String listUsers(Model model) {
//        List<User> users = adminService.getAllUsers();
//        model.addAttribute("users", users);
//        return "admin/user-list";
//    }
//}















package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/users/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // handles /users/admin → Manage Users
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = adminService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list"; // you already have this template.
    }

    // handles /users/admin/create-agent → Create Agent
    @GetMapping("/create-agent")
    public String createAgentForm() {
        return "admin/agent_form";  // make sure agent_form.html exists inside templates/admin
    }
}
