package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.repositories.PropertyImageRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AgentController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyImageRepository propertyImageRepository;

    @Autowired
    public AgentController(PropertyRepository propertyRepository, UserRepository userRepository, PropertyImageRepository propertyImageRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    @GetMapping("/properties/add")
    @PreAuthorize("hasRole('AGENT')")
    public String showAddPropertyForm() {
        return "agent/add_properties";
    }

    @GetMapping("/properties/manage")
    @PreAuthorize("hasRole('AGENT')")
    public String manageListings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            throw new RuntimeException("UserDetails is null — are you logged in?");
        }

        List<Property> properties = propertyRepository.findAll();
        model.addAttribute("properties", properties);
        return "agent/manage_properties";
    }
}
