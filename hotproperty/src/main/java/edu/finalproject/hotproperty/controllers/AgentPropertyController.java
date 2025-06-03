package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.services.AuthService;
import edu.finalproject.hotproperty.services.PropertyService;
import edu.finalproject.hotproperty.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/properties/manage")
public class AgentPropertyController {

    private final PropertyService propertyService;
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public AgentPropertyController(PropertyService propertyService, UserService userService, AuthService authService) {
        this.propertyService = propertyService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public String listAgentProperties(Model model) {
        String email = authService.getLoggedInUserEmail();
        User agent = userService.findByEmail(email);
        List<Property> properties = propertyService.getPropertiesByAgent(agent);
        model.addAttribute("properties", properties);
        return "agent/agent-properties";
    }

    @PostMapping("/delete/{propertyId}")
    public String deleteProperty(@PathVariable Long propertyId) {
        String email = authService.getLoggedInUserEmail();
        User agent = userService.findByEmail(email);
        propertyService.deleteProperty(propertyId, agent);
        return "redirect:/properties/manage";
    }
}
